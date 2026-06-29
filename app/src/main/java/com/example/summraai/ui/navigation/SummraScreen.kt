package com.example.summraai.ui.navigation

sealed class SummraScreen(val route: String) {
    data object Splash : SummraScreen("splash")
    data object Home : SummraScreen("home")
    data object History : SummraScreen("history")
    data object Collections : SummraScreen("collections")
    data object Settings : SummraScreen("settings")
    data object Profile : SummraScreen("profile")
    data object SummaryResult : SummraScreen("summary_result/{summaryId}") {
        fun createRoute(summaryId: String) = "summary_result/$summaryId"
    }
    data object TextSummary : SummraScreen("text_summary")
    data object PdfSummary : SummraScreen("pdf_summary")
    data object WebsiteSummary : SummraScreen("website_summary")
    data object YoutubeSummary : SummraScreen("youtube_summary")
    data object About : SummraScreen("about")
}
