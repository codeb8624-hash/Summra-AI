import os

PRIMARY_MODEL = os.getenv("OPENROUTER_PRIMARY_MODEL", "gpt-4o-mini")
FALLBACK_MODEL = os.getenv("OPENROUTER_FALLBACK_MODEL", "openrouter/free")
MODEL_NAME = PRIMARY_MODEL

TEMPERATURE = float(os.getenv("OPENROUTER_TEMPERATURE", "0.7"))
MAX_TOKENS = int(os.getenv("OPENROUTER_MAX_TOKENS", "2048"))
BASE_URL = os.getenv("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1/chat/completions")
TIMEOUT_SECONDS = int(os.getenv("OPENROUTER_TIMEOUT", "60"))
API_KEY = os.getenv("OPENROUTER_API_KEY", "")
