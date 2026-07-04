import logging
import time

import httpx

from app.config.ai_config import PRIMARY_MODEL, FALLBACK_MODEL, TEMPERATURE, MAX_TOKENS, BASE_URL, TIMEOUT_SECONDS, API_KEY
from app.utils.markdown_cleaner import clean_markdown

logger = logging.getLogger(__name__)


async def _do_openrouter_call(
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
        "model": model or PRIMARY_MODEL,
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
        model or PRIMARY_MODEL,
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
        logger.exception("Failed to parse AI service response: status=%d", response.status_code)
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


_fallback_404_warned: bool = False


async def call_openrouter(
    messages: list[dict],
    model: str | None = None,
    max_tokens: int | None = None,
    temperature: float | None = None,
) -> dict:
    global _fallback_404_warned

    actual_model = model or PRIMARY_MODEL
    result = await _do_openrouter_call(messages, actual_model, max_tokens, temperature)

    if result.get("error") == "MODEL_NOT_FOUND" and not _fallback_404_warned:
        logger.warning("Configured model '%s' not found (404). This may indicate an invalid model name.", actual_model)
        _fallback_404_warned = True

    if not result["success"] and result.get("error") == "MODEL_UNAVAILABLE":
        logger.warning("Primary model '%s' unavailable (402), falling back to '%s'", actual_model, FALLBACK_MODEL)
        fallback_result = await _do_openrouter_call(messages, FALLBACK_MODEL, max_tokens, temperature)
        if fallback_result["success"]:
            logger.info("Fallback model '%s' succeeded", FALLBACK_MODEL)
            return fallback_result
        logger.error("Fallback model '%s' also failed: %s", FALLBACK_MODEL, fallback_result.get("error"))

    return result
