import logging
import re
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path

logger = logging.getLogger(__name__)

# --- Constants ---

SUPPORTED_LANGUAGES = ["en", "hi", "gu"]  # English, Hindi, Gujarati

YOUTUBE_URL_PATTERNS = [
    re.compile(r"(?:https?://)?(?:www\.)?youtube\.com/watch\?v=([\w-]{11})"),
    re.compile(r"(?:https?://)?(?:www\.)?youtube\.com/embed/([\w-]{11})"),
    re.compile(r"(?:https?://)?(?:www\.)?youtube\.com/shorts/([\w-]{11})"),
    re.compile(r"(?:https?://)?youtu\.be/([\w-]{11})"),
    re.compile(r"(?:https?://)?(?:www\.)?youtube\.com/v/([\w-]{11})"),
]

# --- Data Classes ---


class YouTubeExtractionError(Exception):
    def __init__(self, message: str, user_message: str) -> None:
        self.message = message
        self.user_message = user_message
        super().__init__(self.message)


@dataclass
class YouTubeMetadata:
    title: str | None = None
    channel: str | None = None
    channel_url: str | None = None
    duration_seconds: int | None = None
    thumbnail_url: str | None = None
    view_count: int | None = None
    publish_date: str | None = None
    description: str | None = None
    language: str | None = None


@dataclass
class YouTubeExtractResult:
    transcript: str
    metadata: YouTubeMetadata
    language: str
    word_count: int


# --- URL Extraction ---


def extract_video_id(url: str) -> str:
    url = url.strip()
    for pattern in YOUTUBE_URL_PATTERNS:
        match = pattern.search(url)
        if match:
            return match.group(1)
    raise YouTubeExtractionError(
        f"Could not extract video ID from URL: {url}",
        "Invalid YouTube URL. Please provide a valid YouTube video link.",
    )


# --- Transcript Extraction ---


def _classify_api_error(error_msg: str, video_id: str) -> None:
    """Translate low-level API errors into YouTubeExtractionError."""
    if "disabled" in error_msg:
        raise YouTubeExtractionError(
            "Transcripts disabled for this video",
            "Transcripts are disabled for this video.",
        )
    if "private" in error_msg:
        raise YouTubeExtractionError(
            "Video is private",
            "This video is private.",
        )
    if "not found" in error_msg or "unable" in error_msg:
        raise YouTubeExtractionError(
            "Video not found",
            "Video not found. It may have been deleted or the URL is incorrect.",
        )
    if "429" in error_msg or "too many" in error_msg:
        raise YouTubeExtractionError(
            "YouTube rate limit hit",
            "Service temporarily unavailable. Please try again later.",
        )
    logger.exception("Failed to list transcripts for video %s", video_id)


def _ytdlp_fetch_transcript(video_id: str, langs: list[str]) -> tuple[str, str] | None:
    """Fallback: use yt-dlp to download subtitles and extract text."""
    for lang in langs:
        tmp = tempfile.TemporaryDirectory()
        try:
            tmpdir = Path(tmp.name)
            result = subprocess.run(
                [
                    "yt-dlp",
                    "--write-auto-sub",
                    "--sub-format", "vtt",
                    "--sub-lang", lang,
                    "--skip-download",
                    "-o", str(tmpdir / "%(id)s"),
                    f"https://www.youtube.com/watch?v={video_id}",
                ],
                capture_output=True,
                text=True,
                timeout=120,
            )
            if result.returncode != 0:
                logger.debug("yt-dlp failed for lang=%s: %s", lang, result.stderr.strip())
                continue

            vtt_path = tmpdir / f"{video_id}.{lang}.vtt"
            if not vtt_path.exists():
                vtt_path = tmpdir / f"{video_id}.{lang}.en.vtt"
            if not vtt_path.exists():
                for f in tmpdir.iterdir():
                    if f.suffix == ".vtt":
                        vtt_path = f
                        break

            if vtt_path.exists():
                text = vtt_path.read_text(encoding="utf-8")
                lines = []
                for line in text.splitlines():
                    stripped = line.strip()
                    if (not stripped or stripped.startswith("WEBVTT")
                            or stripped.startswith("NOTE")
                            or "-->" in stripped
                            or stripped.startswith("Kind:")
                            or stripped.startswith("Language:")):
                        continue
                    lines.append(stripped)
                joined = " ".join(lines).strip()
                if joined:
                    logger.info("yt-dlp fallback succeeded for video %s lang=%s", video_id, lang)
                    return joined, lang
        except Exception:
            logger.debug("yt-dlp fallback error for video %s lang=%s", video_id, lang, exc_info=True)
        finally:
            try:
                tmp.cleanup()
            except Exception:
                pass
    return None


async def extract_transcript(video_id: str) -> tuple[str, str]:
    languages = SUPPORTED_LANGUAGES
    try:
        from youtube_transcript_api import YouTubeTranscriptApi
    except ImportError:
        raise YouTubeExtractionError(
            "youtube-transcript-api not installed",
            "Service temporarily unavailable.",
        )

    transcript_text = None
    selected_lang = "en"

    try:
        api = YouTubeTranscriptApi()
    except TypeError:
        api = YouTubeTranscriptApi

    try:
        transcript_list = api.list(video_id)
    except Exception as e:
        error_msg = str(e).lower()
        _classify_api_error(error_msg, video_id)
        logger.warning("list() failed for %s, trying yt-dlp fallback", video_id)
        fallback = _ytdlp_fetch_transcript(video_id, languages)
        if fallback:
            return fallback
        raise YouTubeExtractionError(
            f"Failed to retrieve transcript: {e}",
            "Unable to retrieve video transcript.",
        )

    try:
        manual = transcript_list.find_manually_created_transcript(languages)
        selected_lang = manual.language_code
        data = manual.fetch()
        transcript_text = " ".join(item.text for item in data)
        logger.info("Using manual transcript for %s lang=%s", video_id, selected_lang)
    except Exception:
        logger.debug("No manual transcript for %s in %s", video_id, languages)

    if transcript_text is None:
        try:
            auto = transcript_list.find_generated_transcript(languages)
            selected_lang = auto.language_code
            data = auto.fetch()
            transcript_text = " ".join(item.text for item in data)
            logger.info("Using auto-generated transcript for %s lang=%s", video_id, selected_lang)
        except Exception:
            logger.debug("No auto-generated transcript for %s in %s", video_id, languages)

    if transcript_text is None:
        logger.warning("API returned no transcript for %s, trying yt-dlp fallback", video_id)
        fallback = _ytdlp_fetch_transcript(video_id, languages)
        if fallback:
            return fallback
        raise YouTubeExtractionError(
            f"No transcript available in {', '.join(languages)}",
            "No transcript available in English, Hindi, or Gujarati for this video.",
        )

    return transcript_text, selected_lang


# --- Metadata Extraction ---


async def extract_metadata(video_id: str) -> YouTubeMetadata:
    try:
        import json
        import subprocess
        result = subprocess.run(
            [
                "yt-dlp",
                "--dump-json",
                "--no-download",
                f"https://www.youtube.com/watch?v={video_id}",
            ],
            capture_output=True,
            text=True,
            timeout=30,
        )
        if result.returncode != 0:
            stderr = result.stderr.lower()
            if "private video" in stderr:
                raise YouTubeExtractionError(
                    "Video is private",
                    "This video is private.",
                )
            if "video unavailable" in stderr:
                raise YouTubeExtractionError(
                    "Video unavailable",
                    "This video is unavailable. It may have been removed.",
                )
            if "age" in stderr and "restrict" in stderr:
                raise YouTubeExtractionError(
                    "Age-restricted video",
                    "This video is age-restricted and cannot be processed.",
                )
            if "live" in stderr:
                raise YouTubeExtractionError(
                    "Live stream",
                    "Live streams are not supported.",
                )
            logger.warning("yt-dlp stderr for %s: %s", video_id, result.stderr)
            raise YouTubeExtractionError(
                f"yt-dlp failed: {result.stderr.strip()}",
                "Unable to retrieve video information.",
            )

        data = json.loads(result.stdout)

        return YouTubeMetadata(
            title=data.get("title"),
            channel=data.get("channel"),
            channel_url=data.get("channel_url"),
            duration_seconds=data.get("duration"),
            thumbnail_url=data.get("thumbnail"),
            view_count=data.get("view_count"),
            publish_date=data.get("upload_date"),
            description=data.get("description"),
            language=data.get("language"),
        )
    except subprocess.TimeoutExpired:
        raise YouTubeExtractionError(
            "yt-dlp timed out",
            "Service temporarily unavailable.",
        )
    except YouTubeExtractionError:
        raise
    except FileNotFoundError:
        raise YouTubeExtractionError(
            "yt-dlp not installed",
            "Service temporarily unavailable.",
        )
    except Exception:
        logger.exception("Failed to extract metadata for video %s", video_id)
        raise YouTubeExtractionError(
            "Failed to extract video metadata",
            "Unable to retrieve video information.",
        )


# --- Main Extraction ---


async def extract_youtube(url: str) -> YouTubeExtractResult:
    video_id = extract_video_id(url)

    transcript, language = await extract_transcript(video_id)

    try:
        metadata = await extract_metadata(video_id)
    except YouTubeExtractionError:
        logger.warning("Failed to extract metadata for %s, continuing without it", video_id)
        metadata = YouTubeMetadata()

    word_count = len(transcript.split())

    if word_count < 20:
        raise YouTubeExtractionError(
            f"Transcript too short: {word_count} words",
            "This video is too short to summarize.",
        )

    return YouTubeExtractResult(
        transcript=transcript,
        metadata=metadata,
        language=language,
        word_count=word_count,
    )
