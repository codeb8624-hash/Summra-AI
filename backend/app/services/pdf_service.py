import logging

logger = logging.getLogger(__name__)


class PDFExtractionException(Exception):
    pass


def extract_text_pymupdf(content: bytes) -> str | None:
    try:
        import fitz
    except ImportError:
        logger.warning("PyMuPDF not installed.")
        return None

    try:
        doc = fitz.open(stream=content, filetype="pdf")
        pages_text: list[str] = []
        for page in doc:
            t = page.get_text().strip()
            if t:
                pages_text.append(t)
        doc.close()
        if pages_text:
            result = "\n\n".join(pages_text)
            logger.info("PyMuPDF extracted %d chars from %d pages", len(result), len(pages_text))
            return result
        return None
    except Exception:
        logger.warning("PyMuPDF extraction failed", exc_info=True)
        return None


def extract_text_pdfplumber(content: bytes) -> str | None:
    try:
        import pdfplumber
    except ImportError:
        logger.warning("pdfplumber not installed.")
        return None

    try:
        import io

        pages_text: list[str] = []
        with pdfplumber.open(io.BytesIO(content)) as pdf:
            for page in pdf.pages:
                t = page.extract_text()
                if t and t.strip():
                    pages_text.append(t.strip())
        if pages_text:
            result = "\n\n".join(pages_text)
            logger.info("pdfplumber extracted %d chars from %d pages", len(result), len(pages_text))
            return result
        return None
    except Exception:
        logger.warning("pdfplumber extraction failed", exc_info=True)
        return None


def extract_text(content: bytes) -> str:
    pymupdf_available = _is_module_available("fitz")
    pdfplumber_available = _is_module_available("pdfplumber")

    if not pymupdf_available and not pdfplumber_available:
        logger.warning("No PDF extraction libraries available.")
        raise PDFExtractionException(
            "Unable to extract text from this PDF. Please verify that the PDF is not corrupted or install the required PDF extraction libraries."
        )

    if pymupdf_available:
        logger.info("Trying PyMuPDF...")
        text = extract_text_pymupdf(content)
        if text:
            return text

    if pdfplumber_available:
        logger.info("Trying pdfplumber...")
        text = extract_text_pdfplumber(content)
        if text:
            return text

    logger.warning("All PDF extractors returned no text.")
    raise PDFExtractionException(
        "Unable to extract text from this PDF. Please verify that the PDF is not corrupted or contains selectable text."
    )


def get_page_count(content: bytes) -> int:
    try:
        import fitz
    except ImportError:
        return 0

    try:
        doc = fitz.open(stream=content, filetype="pdf")
        count = doc.page_count
        doc.close()
        return count
    except Exception:
        logger.warning("Failed to get PDF page count", exc_info=True)
        return 0


def check_dependencies() -> dict[str, bool]:
    return {
        "PyMuPDF": _is_module_available("fitz"),
        "pdfplumber": _is_module_available("pdfplumber"),
    }


def _is_module_available(name: str) -> bool:
    try:
        __import__(name)
        return True
    except ImportError:
        return False
