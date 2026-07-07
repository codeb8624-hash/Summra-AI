# Summra AI — Project Summary

## Goal
Refactor the history system from mock data to real Room DB persistence with auto-save on summary generation, wired through existing ViewModels.

## Constraints & Preferences
- Kotlin/Compose, Room 2.8.4, Navigation Compose 2.8.5, Coroutines/Flow.
- **No DI** — ViewModels and repo wiring happen manually via default params or factories in NavGraph.
- Project structure: `data/local/` (entity, dao, database, repository), `viewmodel/`, `ui/screens/`, `ui/components/`, `ui/navigation/`.
- `RoomSummaryRepository` implements **both** `SummaryRepository` and `HistoryRepository` — it's the single backend for both interfaces.
- No `HistoryDao` — history read/write goes through `RoomSummaryRepository` using `SummaryDao`.
- All 4 generation screens (Text, PDF, Website, Youtube) have dedicated ViewModels using `AISummaryRepositoryImpl` — **zero Room integration yet**.

## What Exists (Working)
- **`SummraDatabase`** — Room DB singleton with `summaryDao()`, `collectionDao()`, `getInstance(context)`.
- **`SummaryEntity`** — fields: `id`, `title`, `content`, `type`, `source`, `createdAt`, `wordCount`, `tags`, `isBookmarked`, `style`, `lastViewedAt`. Includes `toDomain()`.
- **`SummaryDao`** — all needed queries: `getAllSummaries()`, `getFavoriteSummaries()`, `getSummaryById()`, `searchSummaries()`, `insert()`, `update()`, `deleteById()`, `toggleBookmark()`, `updateTitle()`. All return `Flow` except `getSummaryById(id)` which is suspend.
- **`CollectionDao`** — `getAllCollections()`, `getSummariesInCollection()`, cross-ref management.
- **`RoomSummaryRepository`** — 124 lines, wraps both DAOs, implements `SummaryRepository` + `HistoryRepository`. Has `Flow`-returning methods and suspend methods.
- **`HistoryViewModel`** — takes `HistoryRepository`, has `loadHistory()`, `removeItem(id)`, `clearHistory()`. Structured but **never instantiated**.
- **`HomeViewModel`** — takes `SummaryRepository`, has `loadRecentSummaries()`. Structured but **never instantiated**.
- **Generation ViewModels** — `TextSummaryViewModel`, `WebsiteSummaryViewModel`, `PdfSummaryViewModel`, `YoutubeSummaryViewModel` — all accept `AISummaryRepository` (default impl), generate summary, expose `UiState<SummaryResult>`. **No `saveSummary()` call.**
- **`SummaryScreen`** — stateless composable using local `remember { mutableStateOf(false) }` for bookmark, placeholder text for title/content/key points.

## Key Gaps
1. `RoomSummaryRepository` is never instantiated — `SummraDatabase` exists but is unused at runtime.
2. No ViewModel is provided to `SummaryScreen`, `HistoryScreen`, or `HomeScreen` — they use mock/placeholder data.
3. `HistoryScreen` defines its own private `HistoryItem` (with `category`) instead of using domain `HistoryItem` (with `type`, `timestamp`).
4. `HomeScreen` renders 3 hardcoded "Sample Summary" cards.
5. Generation screens show result inline but never persist to Room or navigate to `SummaryScreen` with a real DB `summaryId`.
6. `SummaryScreen` renders placeholder content instead of fetching from Room by `summaryId`.
7. `HistoryViewModel` and `HomeViewModel` exist but have no call site — NavGraph uses bare `viewModel()` default constructor pattern for generation VMs only.

## Current Flow
```
User input → Generation Screen's ViewModel → AISummaryRepositoryImpl (API) → UiState.Success(SummaryResult(content, style, wordCount))
                                                                              ↳ shows inline result card
                                                                              ↳ NO save to Room
                                                                              ↳ NO auto-navigate to SummaryScreen
```

## Required Flow
```
User input → Generation Screen's ViewModel → AISummaryRepositoryImpl (API)
                                            → UiState.Success(SummaryResult)
                                            → saveSummary() to Room
                                            → navigate to SummaryScreen(realId)
```

## Next Steps (Ordered)
1. In NavGraph, instantiate `SummraDatabase` and `RoomSummaryRepository`, then wire into screens that need persistence.
2. Add `SummaryRepository` param to generation ViewModels; call `saveSummary()` on successful API response.
3. Update generation screens to navigate to `SummaryScreen` after save (using the returned `id`).
4. Wire `HomeViewModel` into `HomeScreen` — replace mock recent items with real `RoomSummaryRepository.getSummaries()`.
5. Rewire `HistoryScreen` to use domain `HistoryItem` from `HistoryViewModel` with delete/clear support.
6. Wire `SummaryScreen` with data from Room (via `SummaryRepository.getSummaryById()`) and real bookmark/lastViewedAt tracking.
7. Add `lastViewedAt` update to `SummaryScreen` on appear.
