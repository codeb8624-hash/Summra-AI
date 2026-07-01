import re
import logging

logger = logging.getLogger(__name__)


def clean_markdown(text: str) -> str:
    if not text:
        return text

    try:
        text = re.sub(r"```[\s\S]*?```", "", text)

        text = re.sub(r"`([^`]+)`", r"\1", text)

        text = re.sub(r"\*\*(.+?)\*\*", r"\1", text)

        text = re.sub(r"\*(.+?)\*", r"\1", text)

        text = re.sub(r"(?<!\w)_(.+?)_(?!\w)", r"\1", text)

        text = re.sub(r"^#{1,5}\s+", "", text, flags=re.MULTILINE)

        text = re.sub(r"^---\s*$", "", text, flags=re.MULTILINE)

        text = re.sub(r"^-\s+", "• ", text, flags=re.MULTILINE)

        text = re.sub(r"\n{3,}", "\n\n", text)

        text = "\n".join(line.rstrip() for line in text.split("\n"))

        result = text.strip()
        logger.debug("Markdown cleaned successfully.")
        return result

    except Exception:
        logger.exception("Markdown cleaner failed.")
        return text
