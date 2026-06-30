# Summra AI

An AI-powered Android summarizer app that instantly summarizes text, PDFs, websites, and YouTube videos using OpenRouter API (with Gemini models) and a modern Material 3 UI.

**Author:** Yug Sathavara  
**License:** MIT (c) 2026

---

## Features

- **Text Summarization** — Paste or type text (up to 10,000 chars) and generate summaries in 4 styles
- **Website Summarization** — Enter a URL to summarize web page content (WIP)
- **YouTube Summarization** — Summarize video transcripts from YouTube links (WIP)
- **PDF Summarization** — Extract and summarize PDF content (WIP)
- **4 Summary Styles** — Concise, Detailed, Bullet Points, Key Facts
- **Summary History** — Browse and manage past summaries
- **Collections** — Group related summaries into collections
- **Bookmarking** — Save important summaries for quick access
- **Light / Dark Theme** — System-aware theming with dynamic color (Android 12+)
- **User Profile** — Track total summaries and usage stats

---

## Screens

| Screen | Status | Description |
|--------|--------|-------------|
| Splash | Complete | Animated logo with auto-navigate |
| Home | Complete | Search bar, 4 feature cards, recent summaries |
| Text Summary | Functional | Multi-line input, style chips, generate/regenerate/copy |
| PDF Summary | Placeholder | Input field for PDF URI |
| Website Summary | Placeholder | Input field for URL |
| YouTube Summary | Placeholder | Input field for video URL |
| Summary Detail | Skeleton | View summary with bookmark/share/copy/delete |
| History | Skeleton | List of past summaries with empty state |
| Collections | Skeleton | Group summaries into collections |
| Settings | Skeleton | Style, language, max length, theme, notifications |
| Profile | Skeleton | Avatar, usage stats, account info |
| About | Complete | App info, version, credits |

---

## Architecture

Clean Architecture with MVVM:

```
app/src/main/java/com/example/summraai/
├── ai/                    # AI integration layer
│   ├── config/            # Model configuration (model, temperature, max tokens)
│   ├── model/             # SummaryStyle enum, SummaryUiState
│   ├── prompt/            # Prompt builders (4 styles × 4 content types)
│   └── service/           # AIService interface + implementation
├── core/
│   └── common/            # UiState sealed interface (Idle/Loading/Success/Error/Empty)
├── data/                  # Data layer
│   ├── remote/            # Retrofit API (OpenRouter), Moshi models, HTTP client
│   └── repository/        # Repository interface + implementation
├── domain/                # Domain layer
│   ├── model/             # Summary, HistoryItem, Settings, UserProfile, etc.
│   └── repository/        # Repository interfaces (History, Settings, Summary)
├── ui/                    # Presentation layer
│   ├── common/            # EmptyState, ErrorState composables
│   ├── components/        # Reusable: AppBars, Buttons, Cards, Chips, Dialogs, etc.
│   ├── navigation/        # NavGraph, Screen sealed class (12 routes)
│   ├── screens/           # 12 screen composables
│   ├── theme/             # Color, Typography, Shapes, Dimensions, Animation, AppTheme
│   └── viewmodel/         # 7 ViewModels
└── MainActivity.kt        # Entry point, NavHost setup
```

---

## Tech Stack

| Component              | Technology                                 |
|------------------------|--------------------------------------------|
| Language               | Kotlin 2.1.0                               |
| UI Framework           | Jetpack Compose (BOM 2024.12.01)           |
| Minimum SDK            | API 24 (Android 7.0)                       |
| Target SDK             | API 36 (Android 16)                        |
| Architecture           | MVVM + Clean Architecture                  |
| Navigation             | Navigation Compose 2.8.5                   |
| HTTP Client            | OkHttp 4.12 + Retrofit 2.11.0             |
| JSON Serialization     | Moshi 1.15.2 (KotlinJsonAdapterFactory)    |
| Async                  | Kotlinx Coroutines 1.9.0                  |
| Build System           | Gradle 9.4.1 + AGP 9.2.1                  |
| AI Provider            | OpenRouter API (Gemini models via proxy)   |

---

## Build & Run

1. **Clone the repo**

2. **Add your API key**  
   Create or edit `local.properties` in the project root:
   ```
   OPENROUTER_API_KEY=sk-or-v1-your-key-here
   ```
   > ⚠️ This file is git-ignored. Never commit your API key.

3. **Build and run** with Android Studio or:
   ```bash
   ./gradlew assembleDebug
   ```

---

## API Configuration

The app uses **OpenRouter** as the AI provider. Configuration is in `AIConfig.kt`:

- **Default model:** `openrouter/free`
- **Temperature:** 0.7
- **Max tokens:** 1024

The API key is injected at compile time from `local.properties` via `BuildConfig`.

---

## Project Structure

```
Summra AI/
├── build.gradle.kts             # Root build script
├── settings.gradle.kts          # Project settings
├── gradle.properties            # JVM config
├── gradle/
│   ├── libs.versions.toml       # Version catalog
│   └── wrapper/                 # Gradle wrapper (9.4.1)
├── app/
│   ├── build.gradle.kts         # App module build script
│   ├── proguard-rules.pro       # ProGuard rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/summraai/  # All source code
│       │   └── res/                         # Resources (drawables, themes, strings)
│       ├── test/                 # Unit tests
│       └── androidTest/          # Instrumented tests
└── README.md
```
