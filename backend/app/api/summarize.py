import logging

from fastapi import APIRouter, HTTPException, UploadFile, File, Form
from fastapi.responses import JSONResponse

from app.models.schemas import SummarizeRequest, SummarizeResponse, PdfSummarizeResponse
from app.services.ai_service import call_openrouter, SYSTEM_PROMPTS
from app.services.document_service import validate_pdf, chunk_text, count_words, DocumentValidationError
from app.services.pdf_service import extract_text, get_page_count, PDFExtractionException

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/summarize", tags=["summarize"])


@router.post("", response_model=SummarizeResponse)
async def summarize_text(request: SummarizeRequest):
    if not request.text or not request.text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty")
    if request.style not in SYSTEM_PROMPTS:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid style '{request.style}'. Must be one of: {', '.join(SYSTEM_PROMPTS.keys())}",
        )
    try:
        summary = await call_openrouter(request.text.strip(), request.style)
        return SummarizeResponse(success=True, summary=summary)
    except Exception as e:
        logger.error("Summarization failed: %s", e)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/pdf")
async def summarize_pdf(
    file: UploadFile = File(...),
    style: str = Form("CONCISE"),
):
    if not file.filename:
        return JSONResponse(
            status_code=400,
            content=PdfSummarizeResponse(success=False, message="No file provided").model_dump(),
        )

    content = await file.read()
    try:
        validate_pdf(len(content), file.filename)
    except DocumentValidationError as e:
        return JSONResponse(
            status_code=400,
            content=PdfSummarizeResponse(success=False, message=str(e)).model_dump(),
        )

    if style not in SYSTEM_PROMPTS:
        return JSONResponse(
            status_code=400,
            content=PdfSummarizeResponse(
                success=False,
                message=f"Invalid style '{style}'. Must be one of: {', '.join(SYSTEM_PROMPTS.keys())}",
            ).model_dump(),
        )

    try:
        page_count = get_page_count(content)
        extracted = extract_text(content)
    except PDFExtractionException as e:
        return JSONResponse(
            status_code=500,
            content=PdfSummarizeResponse(success=False, message=str(e)).model_dump(),
        )

    if not extracted or not extracted.strip():
        return JSONResponse(
            status_code=400,
            content=PdfSummarizeResponse(
                success=False,
                message="Unable to extract text from this PDF. Please verify that the PDF is not corrupted or contains selectable text.",
            ).model_dump(),
        )

    total_words = count_words(extracted)
    chunks = chunk_text(extracted)
    try:
        if len(chunks) == 1:
            summary = await call_openrouter(chunks[0], style)
        else:
            all_summaries: list[str] = []
            for i, chunk in enumerate(chunks):
                logger.info("Summarizing chunk %d/%d (%d chars)", i + 1, len(chunks), len(chunk))
                part = await call_openrouter(chunk, style)
                all_summaries.append(part)
            combined = "\n\n".join(all_summaries)
            if len(all_summaries) > 1:
                summary = await call_openrouter(
                    f"The following are summaries of different sections of a document. "
                    f"Please combine them into one coherent overall summary:\n\n{combined}",
                    style,
                )
            else:
                summary = combined

        return PdfSummarizeResponse(
            success=True,
            summary=summary,
            file_name=file.filename,
            pages=page_count,
            word_count=total_words,
        )
    except Exception as e:
        logger.error("PDF summarization failed: %s", e)
        raise HTTPException(status_code=500, detail=str(e))
