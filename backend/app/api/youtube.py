import logging

from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse

from app.models.schemas import (
    YoutubeSummarizeRequest,
    YoutubeSummarizeResponse,
    YoutubeMetadata,
    ChatRequest,
    ChatResponse,
    TaskRequest,
    TaskResponse,
)
from app.services.ai_service import call_openrouter
from app.services.prompt_templates import get_system_prompt, VALID_STYLES
from app.services.chat_service import chat_service
from app.services.question_service import question_service
from app.services.rag_service import rag_service
from app.services.youtube_service import (
    extract_youtube,
    YouTubeExtractionError,
    YouTubeMetadata as ServiceYouTubeMetadata,
)

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/youtube", tags=["youtube"])


def _format_duration(seconds: int | None) -> str | None:
    if seconds is None:
        return None
    minutes, secs = divmod(seconds, 60)
    hours, minutes = divmod(minutes, 60)
    if hours > 0:
        return f"{hours}h {minutes}m {secs}s"
    return f"{minutes}m {secs}s"


def _convert_metadata(md: ServiceYouTubeMetadata | None) -> YoutubeMetadata | None:
    if md is None:
        return None
    return YoutubeMetadata(
        title=md.title,
        channel=md.channel,
        channel_url=md.channel_url,
        duration_seconds=md.duration_seconds,
        thumbnail_url=md.thumbnail_url,
        view_count=md.view_count,
        publish_date=md.publish_date,
        description=md.description,
        language=md.language,
    )


@router.post("/summarize", response_model=YoutubeSummarizeResponse)
async def summarize_youtube(request: YoutubeSummarizeRequest):
    logger.info("=== Entering /api/youtube/summarize ===")
    logger.info("Request received: url=%s style=%s", request.url, request.style)

    if not request.url or not request.url.strip():
        raise HTTPException(status_code=400, detail="URL cannot be empty")

    style = request.style if request.style in VALID_STYLES else "CONCISE"

    try:
        result = await extract_youtube(request.url)
        logger.info("Transcript extracted: url=%s words=%d lang=%s", request.url, result.word_count, result.language)
    except YouTubeExtractionError as e:
        logger.warning("YouTube extraction failed: url=%s msg=%s", request.url, e.user_message)
        raise HTTPException(status_code=400, detail=e.user_message)

    try:
        document_id = await rag_service.process_document(result.transcript, request.url)
        logger.info("RAG processed: url=%s document_id=%s", request.url, document_id)
    except Exception:
        logger.exception("RAG processing failed: url=%s", request.url)
        document_id = None

    try:
        system_prompt = get_system_prompt(style)
        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": f"Please summarize the following video transcript:\n\n{result.transcript}"},
        ]
        logger.info("AI request sent: url=%s style=%s", request.url, style)
        ai_result = await call_openrouter(messages)
        logger.info("AI response received: url=%s success=%s", request.url, ai_result.get("success"))

        if not ai_result["success"]:
            logger.error("AI call failed: url=%s msg=%s", request.url, ai_result["message"])
            if ai_result.get("error") == "MODEL_UNAVAILABLE":
                return JSONResponse(
                    status_code=402,
                    content=YoutubeSummarizeResponse(
                        success=False,
                        message="AI credits required. Please add credits to your OpenRouter account to use this feature.",
                    ).model_dump(),
                )
            raise HTTPException(status_code=500, detail=ai_result["message"])

        summary = ai_result["content"]
        video_duration = _format_duration(result.metadata.duration_seconds) if result.metadata else None

        logger.info("=== Exiting /api/youtube/summarize (success) ===")
        return YoutubeSummarizeResponse(
            success=True,
            summary=summary,
            document_id=document_id,
            metadata=_convert_metadata(result.metadata),
            word_count=len(summary.split()),
            transcript_word_count=result.word_count,
            video_duration_formatted=video_duration,
            style=style,
        )
    except HTTPException:
        raise
    except Exception:
        logger.exception("YouTube summarization failed: url=%s", request.url)
        raise HTTPException(status_code=500, detail="An unexpected error occurred while summarizing the video.")


@router.post("/chat", response_model=ChatResponse)
async def chat_with_youtube(request: ChatRequest):
    try:
        result = await chat_service.get_answer(
            document_id=request.documentId,
            question=request.question,
            history=request.history,
        )
        return ChatResponse(**result)
    except Exception:
        logger.exception("Chat failed: document_id=%s", request.documentId)
        raise HTTPException(status_code=500, detail="An unexpected error occurred while processing your question.")


@router.post("/task", response_model=TaskResponse)
async def perform_youtube_task(request: TaskRequest):
    try:
        result = await question_service.generate_content(
            document_id=request.documentId,
            task_type=request.taskType,
            language=request.language,
        )
        if "error" in result:
            status = 402 if result.get("error_type") == "MODEL_UNAVAILABLE" else 500
            raise HTTPException(status_code=status, detail=result["error"])
        return TaskResponse(content=result["content"])
    except HTTPException:
        raise
    except Exception:
        logger.exception("Task failed: document_id=%s task_type=%s", request.documentId, request.taskType)
        raise HTTPException(status_code=500, detail="An unexpected error occurred.")
