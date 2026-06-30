MAX_INPUT_CHARS = 100_000
MAX_FILE_SIZE = 100 * 1024 * 1024
ALLOWED_EXTENSIONS = {".pdf"}
MAX_PAGES = 500


class DocumentValidationError(Exception):
    pass


def validate_pdf(file_size: int, filename: str) -> None:
    ext = "." + filename.rsplit(".", 1)[-1].lower() if "." in filename else ""
    if ext not in ALLOWED_EXTENSIONS:
        raise DocumentValidationError(f"Unsupported file type: {ext}. Only PDF files are allowed.")
    if file_size > MAX_FILE_SIZE:
        raise DocumentValidationError(
            f"File too large ({file_size / 1024 / 1024:.1f} MB). Maximum size is {MAX_FILE_SIZE / 1024 / 1024:.0f} MB."
        )


def chunk_text(text: str, max_chars: int = MAX_INPUT_CHARS) -> list[str]:
    if len(text) <= max_chars:
        return [text]
    paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
    chunks: list[str] = []
    current = ""
    for para in paragraphs:
        if len(current) + len(para) + 2 > max_chars:
            if current:
                chunks.append(current.strip())
            current = para
        else:
            current = (current + "\n\n" + para) if current else para
    if current:
        chunks.append(current.strip())
    return chunks


def count_words(text: str) -> int:
    return len(text.split())


def estimate_pages(text: str) -> int:
    return max(1, len(text) // 3000)
