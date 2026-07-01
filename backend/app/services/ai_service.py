import logging
import time

import httpx

from app.config.ai_config import MODEL_NAME, TEMPERATURE, MAX_TOKENS, BASE_URL, TIMEOUT_SECONDS, API_KEY
from app.utils.markdown_cleaner import clean_markdown

logger = logging.getLogger(__name__)

SYSTEM_PROMPTS: dict[str, str] = {
    "CONCISE": (
        "You are a professional summarizer. Provide a concise, clear summary of the given text. "
        "Focus on the main points and key takeaways. Keep it brief and well-structured."
    ),
    "DETAILED": (
        "You are a professional summarizer. Provide a detailed, comprehensive summary of the given text. "
        "Include all important points, supporting details, and maintain the original context. "
        "Structure the summary with clear paragraphs."
    ),
    "BULLET_POINTS": (
        "You are a professional summarizer. Summarize the given text using clear bullet points. "
        "Each bullet should capture one key idea or important fact. "
        "Organize them logically for easy reading."
    ),
    "KEY_FACTS": (
        "You are a professional summarizer. Extract and list the key facts from the given text. "
        "Focus on statistics, dates, names, and verifiable information. "
        "Present each fact as a separate bullet point with a bold label."
    ),
}


async def call_openrouter(
    messages: list[dict],
    model: str | None = None,
    max_tokens: int | None = None,
    temperature: float | None = None,
) -> dict:
    if not API_KEY:
        return {
            "success": False,
            "error": "CONFIGURATION_ERROR",
            "message": "OpenRouter API key is not configured.",
        }

    payload = {
        "model": model or MODEL_NAME,
        "messages": messages,
        "max_tokens": max_tokens or MAX_TOKENS,
        "temperature": temperature or TEMPERATURE,
    }

    start = time.monotonic()

    try:
        async with httpx.AsyncClient(timeout=TIMEOUT_SECONDS) as client:
            response = await client.post(
                BASE_URL,
                headers={
                    "Authorization": f"Bearer {API_KEY}",
                    "Content-Type": "application/json",
                },
                json=payload,
            )
    except httpx.TimeoutException:
        logger.debug("OpenRouter request timed out after %ds", TIMEOUT_SECONDS)
        return {
            "success": False,
            "error": "TIMEOUT",
            "message": "The AI service took too long to respond.",
        }
    except httpx.RequestError:
        logger.debug("OpenRouter network error", exc_info=True)
        return {
            "success": False,
            "error": "NETWORK_ERROR",
            "message": "Could not reach the AI service.",
        }

    elapsed = time.monotonic() - start
    logger.debug(
        "OpenRouter | model=%s | status=%d | %.2fs",
        model or MODEL_NAME,
        response.status_code,
        elapsed,
    )

    if response.status_code == 401:
        return {"success": False, "error": "UNAUTHORIZED", "message": "Invalid API key."}
    if response.status_code == 402:
        return {"success": False, "error": "MODEL_UNAVAILABLE", "message": "The configured AI model is unavailable or requires credits."}
    if response.status_code == 403:
        return {"success": False, "error": "FORBIDDEN", "message": "Access denied by the AI service."}
    if response.status_code == 404:
        return {"success": False, "error": "MODEL_NOT_FOUND", "message": "The requested AI model was not found."}
    if response.status_code == 408:
        return {"success": False, "error": "TIMEOUT", "message": "The AI service took too long to respond."}
    if response.status_code == 429:
        return {"success": False, "error": "RATE_LIMIT", "message": "Please wait a few moments before trying again."}
    if response.status_code in (500, 502, 503):
        return {"success": False, "error": "SERVICE_UNAVAILABLE", "message": "The AI service is temporarily unavailable."}
    if response.status_code != 200:
        return {"success": False, "error": "UNKNOWN_ERROR", "message": f"AI service returned status {response.status_code}."}

    try:
        data = response.json()
    except Exception:
        return {"success": False, "error": "PARSE_ERROR", "message": "Invalid response from AI service."}

    content = (
        data.get("choices", [{}])[0]
        .get("message", {})
        .get("content")
    )
    if not content:
        content = data.get("text", "")

    if not content:
        return {"success": False, "error": "EMPTY_RESPONSE", "message": "AI service returned empty content."}

    return {"success": True, "content": clean_markdown(content.strip())}
