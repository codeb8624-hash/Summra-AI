import os
import logging
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import httpx

load_dotenv()
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Summra AI Backend")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY")
if not OPENROUTER_API_KEY:
    logger.warning("OPENROUTER_API_KEY not set in environment")

SYSTEM_PROMPTS = {
    "CONCISE": "You are an expert text summarizer. Keep it very brief, only the most essential points in 2-3 sentences.",
    "DETAILED": "You are an expert text summarizer. Provide a thorough, well-structured summary covering all important points and nuances.",
    "BULLET_POINTS": "You are an expert text summarizer. Use bullet points to list the key takeaways. Be clear and scannable.",
    "KEY_FACTS": "You are an expert text summarizer. Extract only the most important facts, data, and statistics. Be precise.",
}

class SummarizeRequest(BaseModel):
    text: str
    style: str = "CONCISE"

class SummarizeResponse(BaseModel):
    success: bool
    summary: str | None = None
    message: str | None = None

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.post("/api/summarize", response_model=SummarizeResponse)
async def summarize(request: SummarizeRequest):
    if not request.text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty")

    system_prompt = SYSTEM_PROMPTS.get(request.style, SYSTEM_PROMPTS["CONCISE"])

    if not OPENROUTER_API_KEY:
        raise HTTPException(status_code=500, detail="Server configuration error: API key not configured")

    try:
        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                "https://openrouter.ai/api/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {OPENROUTER_API_KEY}",
                    "Content-Type": "application/json",
                    "HTTP-Referer": "https://github.com",
                    "X-Title": "Summra AI Backend",
                },
                json={
                    "model": "openrouter/free",
                    "messages": [
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": request.text},
                    ],
                    "temperature": 0.7,
                    "max_tokens": 4096,
                },
            )

            logger.info(f"OpenRouter status: {response.status_code}")

            if response.status_code == 401:
                raise HTTPException(status_code=502, detail="Invalid API key. Please check server configuration.")
            if response.status_code == 429:
                raise HTTPException(status_code=429, detail="Rate limited. Please try again later.")
            if response.status_code == 402:
                raise HTTPException(status_code=503, detail="Insufficient credits. Please try again later.")
            if response.status_code != 200:
                logger.error(f"OpenRouter error: {response.status_code} {response.text}")
                raise HTTPException(status_code=502, detail="AI service temporarily unavailable.")

            data = response.json()
            choices = data.get("choices", [])
            if not choices:
                raise HTTPException(status_code=502, detail="AI service returned empty response.")

            content = choices[0].get("message", {}).get("content", "")
            if not content:
                raise HTTPException(status_code=502, detail="AI service returned empty content.")

            return SummarizeResponse(success=True, summary=content.strip())

    except httpx.TimeoutException:
        raise HTTPException(status_code=504, detail="AI service timed out. Please try again.")
    except httpx.RequestError as e:
        logger.error(f"Request error: {e}")
        raise HTTPException(status_code=502, detail="AI service unreachable. Please try again later.")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
