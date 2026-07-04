import ipaddress
import logging
import re
import time
from dataclasses import dataclass, field
from datetime import datetime, timedelta
from typing import Optional
from urllib.parse import urlparse, urlunparse, urlencode, parse_qs

import httpx
import trafilatura
from bs4 import BeautifulSoup
from readability import Document as ReadabilityDocument

logger = logging.getLogger(__name__)

# --- Constants ---

MIN_WORDS = 200
MAX_CONTENT_CHARS = 100_000
MAX_REDIRECTS = 5
MAX_RETRIES = 2
CONNECT_TIMEOUT = 10.0
READ_TIMEOUT = 15.0
MAX_PAGE_BYTES = 10 * 1024 * 1024
CACHE_TTL_MINUTES = 30
SSL_TIMEOUT = 15.0
MAX_URL_LENGTH = 2048

TRACKING_PARAMS = frozenset({
    "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
    "fbclid", "gclid", "gclsrc", "dclid", "mc_cid", "mc_eid",
    "ref", "spm", "scm", "yclid", "igshid",
})

NON_RETRYABLE_STATUSES = frozenset({400, 401, 403, 404, 405, 406, 410, 422})

BROWSER_HEADERS: dict[str, str] = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/138.0.0.0 Safari/537.36"
    ),
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
    "Accept-Encoding": "gzip, deflate",
    "DNT": "1",
    "Upgrade-Insecure-Requests": "1",
}

# --- Data Classes ---


class ExtractionError(Exception):
    def __init__(self, message: str, user_message: str) -> None:
        self.message = message
        self.user_message = user_message
        super().__init__(self.message)


@dataclass
class ArticleMetadata:
    title: str | None = None
    domain: str | None = None
    author: str | None = None
    published_date: str | None = None
    language: str | None = None
    description: str | None = None
    og_image: str | None = None
    favicon: str | None = None
    canonical_url: str | None = None


@dataclass
class ExtractionScore:
    word_count: int = 0
    paragraph_count: int = 0
    heading_count: int = 0
    content_density: float = 0.0
    noise_ratio: float = 1.0

    @property
    def composite(self) -> float:
        if self.word_count < 50:
            return 0.0
        score = min(self.word_count / 1000, 1.0) * 40
        score += min(self.paragraph_count / 20, 1.0) * 20
        score += min(self.heading_count / 5, 1.0) * 10
        score += min(self.content_density * 10, 1.0) * 20
        score += max(1.0 - self.noise_ratio, 0.0) * 10
        return score


@dataclass
class ExtractResult:
    text: str
    method: str
    word_count: int
    score: float
    metadata: ArticleMetadata | None = None


@dataclass
class CacheEntry:
    html: str
    timestamp: datetime


_CACHE: dict[str, CacheEntry] = {}
_CLIENT: httpx.AsyncClient | None = None


def _get_client() -> httpx.AsyncClient:
    global _CLIENT
    if _CLIENT is None:
        _CLIENT = httpx.AsyncClient(
            timeout=httpx.Timeout(connect=CONNECT_TIMEOUT, read=READ_TIMEOUT, write=SSL_TIMEOUT, pool=CONNECT_TIMEOUT),
            follow_redirects=True,
            max_redirects=MAX_REDIRECTS,
            headers=BROWSER_HEADERS,
            limits=httpx.Limits(max_keepalive_connections=10, max_connections=50),
        )
    return _CLIENT


# --- URL Normalization (Task 1) ---


def _is_private_ip(host: str) -> bool:
    try:
        addr = ipaddress.ip_address(host)
        return addr.is_private or addr.is_loopback or addr.is_unspecified
    except ValueError:
        return False


def normalize_url(raw: str) -> str:
    raw = raw.strip()
    if not raw:
        raise ExtractionError("Empty URL", "Invalid URL.")

    if len(raw) > MAX_URL_LENGTH:
        raise ExtractionError("URL too long", "Invalid URL.")

    if re.search(r"[\x00-\x1f\x7f]", raw):
        raise ExtractionError("URL contains control characters", "Invalid URL.")

    if re.match(r"^[a-zA-Z][a-zA-Z0-9+.-]*://", raw):
        scheme = raw.split("://")[0].lower()
        if scheme not in ("http", "https"):
            raise ExtractionError(f"Unsupported scheme: {scheme}", "Invalid URL.")
    else:
        raw = "https://" + raw

    parsed = urlparse(raw)

    if not parsed.netloc:
        raise ExtractionError("No hostname in URL", "Invalid URL.")

    host = parsed.hostname or ""
    host = host.lower()
    host = host.strip(".")

    if _is_private_ip(host):
        raise ExtractionError(f"Private IP blocked: {host}", "Invalid URL.")

    if host in ("localhost", "0.0.0.0"):
        raise ExtractionError(f"Host blocked: {host}", "Invalid URL.")

    if not re.match(r"^[a-z0-9]([a-z0-9.-]*[a-z0-9])?$", host):
        raise ExtractionError(f"Malformed hostname: {host}", "Invalid URL.")

    path = parsed.path.rstrip("/") or "/"

    query_params = parse_qs(parsed.query, keep_blank_values=True)
    filtered = {k: v for k, v in query_params.items() if k.lower() not in TRACKING_PARAMS}
    new_query = urlencode(filtered, doseq=True) if filtered else ""

    scheme = parsed.scheme.lower()
    normalized = urlunparse((scheme, host, path, parsed.params, new_query, parsed.fragment))
    normalized = re.sub(r"(?<!:)//+", "/", normalized)
    return normalized


# --- SSRF / Security (Task 9) ---


def _check_ssrf(url: str) -> None:
    parsed = urlparse(url)
    host = parsed.hostname or ""
    if host in ("localhost", "0.0.0.0"):
        raise ExtractionError(f"SSRF blocked: {host}", "Website unavailable.")
    if _is_private_ip(host):
        raise ExtractionError(f"SSRF blocked private IP: {host}", "Website unavailable.")


def _reject_non_http(url: str) -> None:
    parsed = urlparse(url)
    if parsed.scheme not in ("http", "https"):
        raise ExtractionError(f"Blocked scheme: {parsed.scheme}", "Invalid URL.")
    if url.startswith("file://") or url.startswith("ftp://") or url.startswith("javascript:"):
        raise ExtractionError("Blocked URI scheme", "Invalid URL.")


# --- Cache (Task 5) ---


def _cache_get(normalized_url: str) -> str | None:
    entry = _CACHE.get(normalized_url)
    if entry is None:
        return None
    if datetime.now() - entry.timestamp > timedelta(minutes=CACHE_TTL_MINUTES):
        del _CACHE[normalized_url]
        return None
    return entry.html


def _cache_set(normalized_url: str, html: str) -> None:
    _CACHE[normalized_url] = CacheEntry(html=html, timestamp=datetime.now())
    if len(_CACHE) > 1000:
        stale = [k for k, v in _CACHE.items() if datetime.now() - v.timestamp > timedelta(minutes=CACHE_TTL_MINUTES)]
        for k in stale:
            del _CACHE[k]


# --- Download (Tasks 2, 6, 7) ---


async def _do_download(url: str) -> tuple[str, str]:
    client = _get_client()
    resp = await client.get(url)
    final_url = str(resp.url)

    if resp.status_code == 403:
        raise ExtractionError("HTTP 403", "Website blocked automated access.")
    if resp.status_code == 404:
        raise ExtractionError("HTTP 404", "Website unavailable.")
    if resp.status_code >= 500:
        raise ExtractionError(f"HTTP {resp.status_code}", "Server unavailable.")

    if resp.status_code == 429:
        raise ExtractionError("HTTP 429", "Website unavailable.")

    if resp.status_code != 200:
        raise ExtractionError(f"HTTP {resp.status_code}", "Website unavailable.")

    content_length = resp.headers.get("content-length")
    if content_length and int(content_length) > MAX_PAGE_BYTES:
        raise ExtractionError("Page too large", "Website unavailable.")

    body = resp.text
    if len(body.encode("utf-8")) > MAX_PAGE_BYTES:
        raise ExtractionError("Page exceeds size limit", "Website unavailable.")

    content_type = resp.headers.get("content-type", "")
    if "text/html" not in content_type and "text/plain" not in content_type:
        raise ExtractionError(f"Unexpected content-type: {content_type}", "Website unavailable.")

    return body, final_url


async def download_page(url: str) -> tuple[str, str]:
    _reject_non_http(url)
    _check_ssrf(url)

    last_exc: Exception | None = None
    for attempt in range(1 + MAX_RETRIES):
        try:
            return await _do_download(url)
        except httpx.ConnectTimeout:
            last_exc = ExtractionError("Connect timeout", "Timeout occurred.")
            if attempt < MAX_RETRIES:
                wait = 2 ** attempt
                logger.warning("Retry %d/%d for %s after connect timeout (waiting %ds)", attempt + 1, MAX_RETRIES, url, wait)
                await _async_sleep(wait)
        except httpx.ReadTimeout:
            last_exc = ExtractionError("Read timeout", "Timeout occurred.")
            if attempt < MAX_RETRIES:
                wait = 2 ** attempt
                logger.warning("Retry %d/%d for %s after read timeout (waiting %ds)", attempt + 1, MAX_RETRIES, url, wait)
                await _async_sleep(wait)
        except (httpx.ConnectError, httpx.RemoteProtocolError, httpx.ReadError) as e:
            last_exc = e
            if attempt < MAX_RETRIES:
                wait = 2 ** attempt
                logger.warning("Retry %d/%d for %s after %s (waiting %ds)", attempt + 1, MAX_RETRIES, url, type(e).__name__, wait)
                await _async_sleep(wait)
        except ExtractionError:
            raise
        except httpx.RequestError as e:
            last_exc = e
            if attempt < MAX_RETRIES:
                wait = 2 ** attempt
                logger.warning("Retry %d/%d for %s after %s", attempt + 1, MAX_RETRIES, url, type(e).__name__)
                await _async_sleep(wait)
            else:
                raise ExtractionError(f"Request failed after retries: {e}", "Website unavailable.")
        except Exception as e:
            logger.exception("Unexpected error downloading URL: %s", url)
            last_exc = e
            raise ExtractionError(f"Unexpected error: {e}", "Website unavailable.")

    raise ExtractionError("Download failed after retries", "Website unavailable.")


# --- Extraction (Task 4 — Scoring) ---


def _score_extraction(text: str, html: str) -> ExtractionScore:
    words = text.split()
    word_count = len(words)
    paragraphs = [p for p in text.split("\n\n") if p.strip()]
    paragraph_count = len(paragraphs)
    headings = sum(1 for line in text.split("\n") if re.match(r"^#{1,3}\s", line.strip()))
    content_chars = len(text)
    total_chars = len(html)
    content_density = content_chars / max(total_chars, 1)
    total_tags = len(re.findall(r"<[^>]+>", html))
    text_chars = len(re.sub(r"<[^>]+>", "", html))
    noise_ratio = 1.0 - (text_chars / max(total_chars, 1))
    return ExtractionScore(
        word_count=word_count,
        paragraph_count=paragraph_count,
        heading_count=headings,
        content_density=content_density,
        noise_ratio=noise_ratio,
    )


def _clean_text(text: str) -> str:
    text = re.sub(r"\n{3,}", "\n\n", text)
    text = re.sub(r"[ \t]{2,}", " ", text)
    return text.strip()


def _extract_trafilatura(html: str) -> Optional[str]:
    text = trafilatura.extract(
        html,
        include_images=False,
        include_tables=False,
        include_links=False,
        no_fallback=True,
        favor_precision=True,
    )
    if text:
        text = _clean_text(text)
    return text


def _extract_readability(html: str) -> Optional[str]:
    doc = ReadabilityDocument(html)
    summary_html = doc.summary()
    if not summary_html:
        return None
    soup = BeautifulSoup(summary_html, "lxml")
    for tag in soup(["script", "style", "nav", "footer", "header", "aside"]):
        tag.decompose()
    text = soup.get_text(separator="\n", strip=True)
    if text:
        text = _clean_text(text)
    return text


def _extract_beautifulsoup(html: str) -> Optional[str]:
    soup = BeautifulSoup(html, "lxml")
    for tag in soup(
        ["script", "style", "nav", "footer", "header", "aside", "noscript"]
    ):
        tag.decompose()
    for tag in soup.find_all(["div", "section"], class_=re.compile(
        r"(nav|footer|header|sidebar|menu|cookie|banner|modal)", re.I
    )):
        tag.decompose()
    text = soup.get_text(separator="\n", strip=True)
    if text:
        text = _clean_text(text)
    return text


# --- Metadata Extraction (Task 3) ---


def _extract_domain(url: str) -> str:
    parsed = urlparse(url)
    return parsed.hostname or ""


def _extract_metadata(html: str, url: str) -> ArticleMetadata:
    soup = BeautifulSoup(html, "lxml")
    og = lambda name: soup.find("meta", property=name)
    name_meta = lambda n: soup.find("meta", attrs={"name": n})

    title: str | None = None
    og_title = og("og:title")
    if og_title and og_title.get("content"):
        title = og_title["content"]
    else:
        t = soup.find("title")
        if t:
            title = t.get_text(strip=True)

    domain = _extract_domain(url)

    author: str | None = None
    author_meta = name_meta("author")
    if author_meta and author_meta.get("content"):
        author = author_meta["content"]
    if not author:
        og_author = og("article:author")
        if og_author and og_author.get("content"):
            author = og_author["content"]

    published_date: str | None = None
    for prop in ("article:published_time", "article:modified_time"):
        date_meta = og(prop)
        if date_meta and date_meta.get("content"):
            published_date = date_meta["content"]
            break
    if not published_date:
        date_meta = name_meta("date")
        if date_meta and date_meta.get("content"):
            published_date = date_meta["content"]

    language: str | None = None
    html_tag = soup.find("html")
    if html_tag:
        lang = html_tag.get("lang") or html_tag.get("xml:lang")
        if lang:
            language = lang

    description: str | None = None
    desc_meta = name_meta("description") or og("og:description")
    if desc_meta and desc_meta.get("content"):
        description = desc_meta["content"]

    og_image: str | None = None
    img_meta = og("og:image")
    if img_meta and img_meta.get("content"):
        og_image = img_meta["content"]

    favicon: str | None = None
    icon_link = soup.find("link", rel=re.compile(r"icon", re.I))
    if icon_link and icon_link.get("href"):
        favicon = icon_link["href"]

    canonical_url: str | None = None
    canonical_link = soup.find("link", rel="canonical")
    if canonical_link and canonical_link.get("href"):
        canonical_url = canonical_link["href"]

    return ArticleMetadata(
        title=title,
        domain=domain,
        author=author,
        published_date=published_date,
        language=language,
        description=description,
        og_image=og_image,
        favicon=favicon,
        canonical_url=canonical_url,
    )


# --- Helpers ---


def _count_words(text: str) -> int:
    return len(text.split())


def _count_paragraphs(text: str) -> int:
    return len([p for p in text.split("\n\n") if p.strip()])


async def _async_sleep(seconds: float) -> None:
    import asyncio
    await asyncio.sleep(seconds)


# --- Main Extraction (Tasks 4, 8, 10, 11, 12) ---


async def extract_article_text(url: str) -> ExtractResult:
    start = time.monotonic()
    normalized_url = normalize_url(url)

    # Check cache
    cached_html = _cache_get(normalized_url)
    cache_hit = cached_html is not None
    if cache_hit:
        logger.info("Cache hit: %s", normalized_url)
        html = cached_html
        final_url = normalized_url
    else:
        logger.info("Cache miss: %s", normalized_url)
        retry_count = 0
        html, final_url = await download_page(normalized_url)
        _cache_set(normalized_url, html)

    extractors = [
        ("trafilatura", _extract_trafilatura),
        ("readability", _extract_readability),
        ("beautifulsoup", _extract_beautifulsoup),
    ]

    best_text: str | None = None
    best_method: str = ""
    best_score_val: float = 0.0
    best_score: ExtractionScore | None = None

    for name, extractor in extractors:
        extracted = extractor(html)
        if not extracted:
            continue
        score = _score_extraction(extracted, html)
        composite = score.composite
        if score.word_count >= MIN_WORDS and composite > best_score_val:
            best_text = extracted
            best_method = name
            best_score_val = composite
            best_score = score

    if best_text is None:
        for name, extractor in extractors:
            extracted = extractor(html)
            if not extracted:
                continue
            score = _score_extraction(extracted, html)
            if score.word_count > 0 and score.composite > best_score_val:
                best_text = extracted
                best_method = name
                best_score_val = score.composite
                best_score = score

    if best_text is None:
        elapsed = time.monotonic() - start
        logger.warning("Extraction failed: %s | %.2fs", url, elapsed)
        raise ExtractionError(
            "All extraction methods returned no text",
            "No readable content found.",
        )

    word_count = _count_words(best_text)

    metadata = _extract_metadata(html, final_url)

    elapsed = time.monotonic() - start
    logger.info(
        "Extracted: %s | method=%s | score=%.2f | words=%d | paragraphs=%d | elapsed=%.2fs | cache=%s",
        url, best_method, best_score_val, word_count, best_score.paragraph_count if best_score else 0, elapsed, "hit" if cache_hit else "miss",
    )

    if word_count < MIN_WORDS:
        raise ExtractionError(
            f"Extracted only {word_count} words (minimum {MIN_WORDS})",
            f"The page is too short ({word_count} words). Try a different URL.",
        )

    if len(best_text) > MAX_CONTENT_CHARS:
        best_text = best_text[:MAX_CONTENT_CHARS] + "\n\n[Content truncated due to length.]"

    return ExtractResult(
        text=best_text,
        method=best_method,
        word_count=word_count,
        score=best_score_val,
        metadata=metadata,
    )
