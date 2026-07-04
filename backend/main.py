import logging
import subprocess
from contextlib import asynccontextmanager

import uvicorn
from dotenv import load_dotenv

load_dotenv()

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.summarize import router as summarize_router
from app.api.chat import router as chat_router
from app.api.youtube import router as youtube_router
from app.services.pdf_service import check_dependencies

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def check_youtube_dependencies() -> dict[str, bool]:
    deps = {}
    try:
        import youtube_transcript_api  # noqa: F401
        deps["youtube_transcript_api"] = True
    except ImportError:
        deps["youtube_transcript_api"] = False

    try:
        result = subprocess.run(["yt-dlp", "--version"], capture_output=True, text=True, timeout=10)
        deps["yt-dlp"] = result.returncode == 0
    except Exception:
        deps["yt-dlp"] = False

    return deps


@asynccontextmanager
async def lifespan(app: FastAPI):
    pdf_deps = check_dependencies()
    for name, available in pdf_deps.items():
        if available:
            logger.info("%s: available", name)
        else:
            logger.warning("%s not installed.", name)

    if not any(pdf_deps.values()):
        logger.warning("No PDF extraction libraries available.")

    yt_deps = check_youtube_dependencies()
    for name, available in yt_deps.items():
        if available:
            logger.info("%s: available", name)
        else:
            logger.warning("%s not installed.", name)

    yield


app = FastAPI(
    title="Summra AI Backend",
    description="AI-powered summarization service",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(summarize_router)
app.include_router(chat_router)
app.include_router(youtube_router)


@app.get("/")
async def root():
    return {"message": "Summra AI Backend is running", "version": "1.0.0"}


@app.get("/health")
async def health():
    return {"status": "healthy"}


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
