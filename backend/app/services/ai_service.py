import os
import logging

import httpx

logger = logging.getLogger(__name__)

OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "")
OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
DEFAULT_MODEL = "gpt-4o-mini"
TIMEOUT_SECONDS = 30

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


async def call_openrouter(text: str, style: str) -> str:
    system_prompt = SYSTEM_PROMPTS.get(style, SYSTEM_PROMPTS["CONCISE"])
    user_prompt = f"Please summarize the following text:\n\n{text}"

    async with httpx.AsyncClient(timeout=TIMEOUT_SECONDS) as client:
        response = await client.post(
            OPENROUTER_URL,
            headers={
                "Authorization": f"Bearer {OPENROUTER_API_KEY}",
                "Content-Type": "application/json",
            },
            json={
                "model": DEFAULT_MODEL,
                "max_tokens": 1024,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
            },
        )

    if response.status_code != 200:
        logger.error("OpenRouter API error: %s %s", response.status_code, response.text)
        raise httpx.HTTPStatusError(
            f"OpenRouter returned {response.status_code}",
            request=response.request,
            response=response,
        )

    data = response.json()
    summary = (
        data.get("choices", [{}])[0]
        .get("message", {})
        .get("content")
    )
    if not summary:
        summary = data.get("text", "")

    return summary.strip()
