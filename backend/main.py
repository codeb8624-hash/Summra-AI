import logging
from contextlib import asynccontextmanager

import uvicorn
from dotenv import load_dotenv

load_dotenv()

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.summarize import router as summarize_router
from app.api.chat import router as chat_router
from app.services.pdf_service import check_dependencies

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    deps = check_dependencies()
    for name, available in deps.items():
        if available:
            logger.info("%s: available", name)
        else:
            logger.warning("%s not installed.", name)

    if not any(deps.values()):
        logger.warning("No PDF extraction libraries available.")
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


@app.get("/")
async def root():
    return {"message": "Summra AI Backend is running", "version": "1.0.0"}


@app.get("/health")
async def health():
    return {"status": "healthy"}


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
