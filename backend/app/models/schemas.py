from pydantic import BaseModel
from typing import List, Optional, Dict


class SummarizeRequest(BaseModel):
    text: str
    style: str = "CONCISE"


class SummarizeResponse(BaseModel):
    success: bool
    summary: str | None = None
    message: str | None = None


class PdfSummarizeResponse(BaseModel):
    success: bool
    summary: str | None = None
    document_id: str | None = None
    message: str | None = None
    file_name: str | None = None
    pages: int | None = None
    word_count: int | None = None


class ChatRequest(BaseModel):
    documentId: str
    question: str
    history: Optional[List[Dict[str, str]]] = None


class ChatResponse(BaseModel):
    answer: str
    sources: List[str]


class TaskRequest(BaseModel):
    documentId: str
    taskType: str
    language: Optional[str] = "English"


class TaskResponse(BaseModel):
    content: str


class ErrorResponse(BaseModel):
    detail: str
