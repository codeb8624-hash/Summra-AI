import logging

from fastapi import APIRouter, HTTPException

from app.models.schemas import ChatRequest, ChatResponse, TaskRequest, TaskResponse
from app.services.chat_service import chat_service
from app.services.question_service import question_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/pdf", tags=["chat"])


@router.post("/chat", response_model=ChatResponse)
async def chat_with_pdf(request: ChatRequest):
    try:
        result = await chat_service.get_answer(
            document_id=request.documentId,
            question=request.question,
            history=request.history,
        )
        return ChatResponse(**result)
    except Exception as e:
        logger.error("Chat failed: %s", e)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/task", response_model=TaskResponse)
async def perform_task(request: TaskRequest):
    try:
        result = await question_service.generate_content(
            document_id=request.documentId,
            task_type=request.taskType,
            language=request.language,
        )
        if "error" in result:
            raise HTTPException(status_code=500, detail=result["error"])
        return TaskResponse(content=result["content"])
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Task failed: %s", e)
        raise HTTPException(status_code=500, detail="An unexpected error occurred.")
