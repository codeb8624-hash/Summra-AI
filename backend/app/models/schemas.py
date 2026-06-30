from pydantic import BaseModel


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
    message: str | None = None
    file_name: str | None = None
    pages: int | None = None
    word_count: int | None = None


class ErrorResponse(BaseModel):
    detail: str
