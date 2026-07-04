from pydantic import BaseModel
from typing import List, Optional, Dict


class SummarizeRequest(BaseModel):
    text: str
    style: str = "CONCISE"


class WebsiteSummarizeRequest(BaseModel):
    url: str
    style: str = "CONCISE"


class SummarizeResponse(BaseModel):
    success: bool
    summary: str | None = None
    message: str | None = None


class WebsiteMetadata(BaseModel):
    title: str | None = None
    domain: str | None = None
    author: str | None = None
    published_date: str | None = None
    language: str | None = None
    description: str | None = None
    og_image: str | None = None
    favicon: str | None = None
    canonical_url: str | None = None


class WebsiteSummarizeResponse(BaseModel):
    success: bool
    summary: str | None = None
    document_id: str | None = None
    message: str | None = None
    metadata: WebsiteMetadata | None = None
    word_count: int | None = None
    original_word_count: int | None = None
    compression_ratio: float | None = None
    reading_time_seconds: int | None = None
    style: str | None = None


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


class YoutubeSummarizeRequest(BaseModel):
    url: str
    style: str = "CONCISE"


class YoutubeMetadata(BaseModel):
    title: str | None = None
    channel: str | None = None
    channel_url: str | None = None
    duration_seconds: int | None = None
    thumbnail_url: str | None = None
    view_count: int | None = None
    publish_date: str | None = None
    description: str | None = None
    language: str | None = None


class YoutubeSummarizeResponse(BaseModel):
    success: bool
    summary: str | None = None
    document_id: str | None = None
    message: str | None = None
    metadata: YoutubeMetadata | None = None
    word_count: int | None = None
    transcript_word_count: int | None = None
    video_duration_formatted: str | None = None
    style: str | None = None


class ErrorResponse(BaseModel):
    detail: str
