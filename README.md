# Summra AI

An AI-powered Android summarization app with a FastAPI backend. Summarize text, PDFs, websites, and YouTube videos using OpenRouter AI (GPT-4o-mini) with RAG-based Q&A over PDF documents.

**Author:** Yug Sathavara  
**License:** MIT (c) 2026

---

## Features

- **Text Summarization** — Paste or type text (up to 10,000 chars) in 4 styles
- **PDF Summarization** — Upload PDFs, get AI summaries, and chat with documents (RAG-based Q&A)
- **PDF Task Generation** — Notes, quizzes, flashcards, exam questions, explanations from PDFs
- **Website Summarization** — Summarize web page content via URL (WIP)
- **YouTube Summarization** — Summarize video transcripts (WIP)
- **4 Summary Styles** — Concise, Detailed, Bullet Points, Key Facts
- **Summary History** — Browse, search, bookmark, and delete past summaries
- **Collections** — Group related summaries (many-to-many)
- **Light/Dark Theme** — System-aware with Android 12+ dynamic colors
- **User Profile** — Track total summaries and usage stats

---

## Screens

| Screen | Status | Description |
|--------|--------|-------------|
| Splash | Complete | Animated logo with auto-navigate to Home |
| Home | Complete | Search bar, 4 feature cards, recent summaries, bottom nav |
| Text Summary | Complete | Multi-line input (10k char limit), style chips, generate/regenerate/copy |
| PDF Summary | Complete | PDF picker, style selection, summary with RAG chat & task generation |
| Website Summary | Placeholder | URL input (disabled generate) |
| YouTube Summary | Placeholder | Video URL input (disabled generate) |
| Summary Detail | Skeleton | View, bookmark, share, copy, delete |
| History | Skeleton | List of past summaries with empty state |
| Collections | Skeleton | Group summaries into collections |
| Settings | Skeleton | Style, language, length, theme, notifications |
| Profile | Skeleton | Avatar, usage stats, account info |
| About | Complete | App info, version, credits |

---

## Architecture

### Overall System

```
[Android App (Kotlin/Compose)]  <--HTTP-->  [FastAPI Backend (Python)]  <--API-->  [OpenRouter AI (GPT-4o-mini)]
         |                                              |
    [Room DB (SQLite)]                            [ChromaDB (Vector)]
         |                                              |
    [Local summaries,                           [Document embeddings,
     collections, bookmarks]                    RAG context retrieval]
```

### Android: Clean Architecture + MVVM

```
app/src/main/java/com/example/summraai/
├── ai/                      # AI service layer (AIService, AIConfig, models)
├── core/common/             # UiState sealed interface
├── data/
│   ├── local/               # Room database, DAOs, entities, converters
│   ├── remote/              # Retrofit API, Moshi models, HTTP client
│   └── repository/          # AI and Room repository implementations
├── domain/
│   ├── model/               # Domain models (Summary, Collection, Settings, etc.)
│   └── repository/          # Repository interfaces
├── ui/
│   ├── common/              # EmptyState, ErrorState composables
│   ├── components/          # 11 reusable components (AppBars, Cards, FAB, etc.)
│   ├── navigation/          # 12-route NavGraph
│   ├── screens/             # 12 screen composables
│   ├── theme/               # Color, Typography, Shapes, Dimensions, Animation
│   └── viewmodel/           # 7 ViewModels
└── MainActivity.kt          # Entry point, NavHost
```

### Backend: FastAPI

```
backend/
├── main.py                  # FastAPI app, CORS, router inclusion, lifespan
├── app/
│   ├── api/
│   │   ├── summarize.py     # POST /api/summarize (text), /api/summarize/pdf
│   │   └── chat.py          # POST /api/pdf/chat, /api/pdf/task
│   ├── models/schemas.py    # Pydantic request/response models
│   ├── services/
│   │   ├── ai_service.py    # OpenRouter API calls (GPT-4o-mini)
│   │   ├── pdf_service.py   # PDF text extraction (PyMuPDF, pdfplumber)
│   │   ├── document_service.py  # Validation, chunking, word count
│   │   ├── rag_service.py   # RAG pipeline orchestration
│   │   ├── embedding_service.py # SentenceTransformer embeddings
│   │   ├── vector_service.py    # ChromaDB storage/retrieval
│   │   ├── chat_service.py  # RAG Q&A with citations
│   │   └── question_service.py  # Notes, quiz, flashcards generation
│   └── utils/markdown_cleaner.py
├── .env                     # OPENROUTER_API_KEY (git-ignored)
└── requirements.txt
```

---

## Tech Stack

### Android

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose (BOM 2024.12.01) + Material 3 |
| Min/Target SDK | API 24 (Android 7.0) / API 36 (Android 16) |
| Architecture | MVVM + Clean Architecture |
| Navigation | Navigation Compose 2.8.5 |
| HTTP | OkHttp 4.12 + Retrofit 2.11.0 |
| JSON | Moshi 1.15.2 (codegen via KSP) |
| Database | Room 2.8.4 (SQLite) |
| Async | Kotlinx Coroutines 1.9.0 |
| Build | Gradle 9.4.1 + AGP 9.2.1 |

### Backend

| Component | Technology |
|-----------|------------|
| Framework | FastAPI + Uvicorn |
| AI Provider | OpenRouter (GPT-4o-mini) |
| PDF | PyMuPDF, pdfplumber |
| Vector DB | ChromaDB (persistent) |
| Embeddings | Sentence-Transformers (all-MiniLM-L6-v2) |
| HTTP | httpx, requests |

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Health check with version |
| GET | `/health` | Simple health check |
| POST | `/api/summarize` | Summarize text (body: `text`, `style`) |
| POST | `/api/summarize/pdf` | Summarize PDF (multipart: `file` + `style`) |
| POST | `/api/pdf/chat` | Chat with PDF (body: `documentId`, `question`, `history?`) |
| POST | `/api/pdf/task` | Generate content from PDF (body: `documentId`, `taskType`, `language?`) |

Styles: `CONCISE`, `DETAILED`, `BULLET_POINTS`, `KEY_FACTS`  
Task types: `notes`, `quiz`, `flashcards`, `important_questions`, `explain`, `explain_10`, `translate`

---

## Database

### Room (Local SQLite)

| Table | Purpose |
|-------|---------|
| `summaries` | Summary content, title, type, style, bookmark, tags, timestamps |
| `collections` | Collection name, description |
| `collection_summary_cross_ref` | Many-to-many junction with cascade delete |

### ChromaDB (Backend Vector Store)

- Collection: `summra_documents`
- Embeddings: 384-dim (all-MiniLM-L6-v2)
- Chunks: 1000 chars with 200-char overlap
- Metadata: `documentId`, `documentName`, `chunkId`, `pageNumber`

---

## Fixes Applied

### Jul 3, 2026 — YoutubeSummaryScreen brace imbalance

- **Root cause:** `formatViewCount()` was accidentally placed inside `ChatBubble`, displacing closing braces
- **Fix:** Extracted `formatViewCount()` to top level, added missing `}` for `if (sources.isNotEmpty())` block and `ChatBubble` function body
- **Verification:** `./gradlew assembleDebug` passes

---

## Build & Run

### Prerequisites

- **Android Studio** (with JDK 21+ bundled JBR)
- **Python 3.10+**

### Backend Setup

```bash
cd backend
pip install -r requirements.txt
# Set OPENROUTER_API_KEY in .env
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### Android Build

```bash
# Ensure JAVA_HOME is set to Android Studio JBR
# (e.g., C:\Program Files\Android\Android Studio\jbr)

# Add your OpenRouter API key in local.properties:
# OPENROUTER_API_KEY=sk-or-v1-your-key-here

./gradlew assembleDebug
```

> ⚠️ `local.properties` is git-ignored. Never commit your API key.

---

## Configuration

- **Android:** Backend URL in `AIConfig.kt` and `RetrofitClient.kt` (default: `http://10.221.173.32:8000`)
- **Backend:** API key in `.env`, model = `gpt-4o-mini`, temperature = `0.7`
- **Backend IP** can be changed in both files above to match your server
