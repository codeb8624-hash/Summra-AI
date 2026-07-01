import os

MODEL_NAME = os.getenv("OPENROUTER_MODEL", "gpt-4o-mini")
TEMPERATURE = float(os.getenv("OPENROUTER_TEMPERATURE", "0.7"))
MAX_TOKENS = int(os.getenv("OPENROUTER_MAX_TOKENS", "1024"))
BASE_URL = os.getenv("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1/chat/completions")
TIMEOUT_SECONDS = int(os.getenv("OPENROUTER_TIMEOUT", "60"))
API_KEY = os.getenv("OPENROUTER_API_KEY", "")
