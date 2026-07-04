import logging
from urllib.parse import urlparse

from fastapi import APIRouter, HTTPException, UploadFile, File, Form
from fastapi.responses import JSONResponse

from app.models.schemas import (
    SummarizeRequest,
    SummarizeResponse,
    PdfSummarizeResponse,
    WebsiteSummarizeRequest,
    WebsiteSummarizeResponse,
    WebsiteMetadata,
)
from app.services.ai_service import call_openrouter
from app.services.prompt_templates import get_system_prompt, VALID_STYLES
from app.services.document_service import validate_pdf, chunk_text, count_words, DocumentValidationError
from app.services.pdf_service import extract_text, get_page_count, PDFExtractionException
from app.services.rag_service import rag_service
from app.services.website_service import extract_article_text, ExtractionError, ArticleMetadata

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/summarize", tags=["summarize"])


@router.post("", response_model=SummarizeResponse)
async def summarize_text(request: SummarizeRequest):
    if not request.text or not request.text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty")
    if request.style not in VALID_STYLES:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid style '{request.style}'. Must be one of: {', '.join(VALID_STYLES)}",
        )
    try:
        system_prompt = get_system_prompt(request.style)
        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": f"Please summarize the following text:\n\n{request.text.strip()}"},
        ]
        result = await call_openrouter(messages)
        if not result["success"]:
            raise HTTPException(status_code=500, detail=result["message"])
        return SummarizeResponse(success=True, summary=result["content"])
    except HTTPException:
        raise
    except Exception:
        logger.exception("Summarization failed")
        raise HTTPException(status_code=500, detail="An unexpected error occurred.")


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

    if style not in VALID_STYLES:
        return JSONResponse(
            status_code=400,
            content=PdfSummarizeResponse(
                success=False,
                message=f"Invalid style '{style}'. Must be one of: {', '.join(VALID_STYLES)}",
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

    # Process for RAG
    try:
        document_id = await rag_service.process_document(extracted, file.filename)
    except Exception:
        logger.exception("RAG processing failed: file=%s", file.filename)
        document_id = None

    chunks = chunk_text(extracted)
    system_prompt = get_system_prompt(style)
    try:
        if len(chunks) == 1:
            messages = [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"Please summarize the following text:\n\n{chunks[0]}"},
            ]
            result = await call_openrouter(messages)
            if not result["success"]:
                status = 402 if result.get("error") == "MODEL_UNAVAILABLE" else 500
                raise HTTPException(status_code=status, detail=result["message"])
            summary = result["content"]
        else:
            all_summaries: list[str] = []
            for i, chunk in enumerate(chunks):
                logger.info("Summarizing chunk %d/%d (%d chars)", i + 1, len(chunks), len(chunk))
                messages = [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": f"Please summarize the following text:\n\n{chunk}"},
                ]
                part_result = await call_openrouter(messages)
                if not part_result["success"]:
                    status = 402 if part_result.get("error") == "MODEL_UNAVAILABLE" else 500
                    raise HTTPException(status_code=status, detail=part_result["message"])
                all_summaries.append(part_result["content"])
            combined = "\n\n".join(all_summaries)
            if len(all_summaries) > 1:
                combine_messages = [
                    {"role": "system", "content": system_prompt},
                    {
                        "role": "user",
                        "content": (
                            "The following are summaries of different sections of a document. "
                            "Please combine them into one coherent overall summary:\n\n"
                            f"{combined}"
                        ),
                    },
                ]
                combine_result = await call_openrouter(combine_messages)
                if not combine_result["success"]:
                    status = 402 if combine_result.get("error") == "MODEL_UNAVAILABLE" else 500
                    raise HTTPException(status_code=status, detail=combine_result["message"])
                summary = combine_result["content"]
            else:
                summary = combined

        return PdfSummarizeResponse(
            success=True,
            summary=summary,
            document_id=document_id,
            file_name=file.filename,
            pages=page_count,
            word_count=total_words,
        )
    except HTTPException:
        raise
    except Exception:
        logger.exception("PDF summarization failed: file=%s", file.filename)
        raise HTTPException(status_code=500, detail="An unexpected error occurred while summarizing the PDF.")


def _convert_metadata(am: ArticleMetadata | None) -> WebsiteMetadata | None:
    if am is None:
        return None
    return WebsiteMetadata(
        title=am.title,
        domain=am.domain,
        author=am.author,
        published_date=am.published_date,
        language=am.language,
        description=am.description,
        og_image=am.og_image,
        favicon=am.favicon,
        canonical_url=am.canonical_url,
    )


@router.post("/website", response_model=WebsiteSummarizeResponse)
async def summarize_website(request: WebsiteSummarizeRequest):
    logger.info("=== Entering /api/summarize/website ===")
    logger.info("Request received: url=%s style=%s", request.url, request.style)

    parsed = urlparse(request.url)
    if not parsed.scheme or not parsed.netloc:
        raise HTTPException(status_code=400, detail="Invalid URL. Must be a valid http or https URL.")
    if parsed.scheme not in ("http", "https"):
        raise HTTPException(status_code=400, detail="Only http and https URLs are supported.")
    logger.info("Request validated: scheme=%s host=%s", parsed.scheme, parsed.hostname)

    style = request.style if request.style in VALID_STYLES else "CONCISE"
    logger.info("Style chosen: %s", style)

    try:
        result = await extract_article_text(request.url)
        logger.info("Article extracted: url=%s method=%s words=%d", request.url, result.method, result.word_count)
    except ExtractionError as e:
        logger.warning("Extraction failed: url=%s msg=%s", request.url, e.user_message)
        raise HTTPException(status_code=400, detail=e.user_message)

    try:
        document_id = await rag_service.process_document(result.text, request.url)
        logger.info("RAG processed: url=%s document_id=%s", request.url, document_id)
    except Exception:
        logger.exception("RAG processing failed: url=%s", request.url)
        document_id = None

    try:
        messages = [
            {"role": "system", "content": get_system_prompt(style)},
            {"role": "user", "content": f"Please summarize the following web page content:\n\n{result.text}"},
        ]
        logger.info("AI request sent: url=%s style=%s", request.url, style)
        ai_result = await call_openrouter(messages)
        logger.info("AI response received: url=%s success=%s", request.url, ai_result.get("success"))

        if not ai_result["success"]:
            logger.error("AI call failed: url=%s msg=%s", request.url, ai_result["message"])
            if ai_result.get("error") == "MODEL_UNAVAILABLE":
                return JSONResponse(
                    status_code=402,
                    content=WebsiteSummarizeResponse(
                        success=False,
                        message="AI credits required. Please add credits to your OpenRouter account to use this feature.",
                    ).model_dump(),
                )
            raise HTTPException(status_code=500, detail=ai_result["message"])

        summary = ai_result["content"]
        summary_word_count = len(summary.split())
        reading_time_seconds = int((summary_word_count / 200) * 60)
        compression_ratio = round(summary_word_count / max(result.word_count, 1), 4)
        logger.info("Response prepared: url=%s words=%d ratio=%.4f", request.url, summary_word_count, compression_ratio)

        logger.info("=== Exiting /api/summarize/website (success) ===")
        return WebsiteSummarizeResponse(
            success=True,
            summary=summary,
            document_id=document_id,
            metadata=_convert_metadata(result.metadata),
            word_count=summary_word_count,
            original_word_count=result.word_count,
            compression_ratio=compression_ratio,
            reading_time_seconds=reading_time_seconds,
            style=style,
        )
    except HTTPException:
        raise
    except Exception:
        logger.exception("Website summarization failed: url=%s", request.url)
        raise HTTPException(status_code=500, detail="An unexpected error occurred while summarizing the website.")
