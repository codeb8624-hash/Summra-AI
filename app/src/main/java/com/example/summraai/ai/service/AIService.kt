package com.example.summraai.ai.service

import android.net.Uri
import com.example.summraai.data.remote.ChatMessage
import com.example.summraai.data.remote.YoutubeMetadata

data class PdfSummaryData(
    val content: String,
    val documentId: String?,
    val fileName: String?,
    val pages: Int?,
    val wordCount: Int?
)

data class WebsiteSummaryData(
    val content: String,
    val documentId: String?,
    val title: String?,
    val domain: String?,
    val author: String?,
    val publishedDate: String?,
    val language: String?,
    val description: String?,
    val ogImage: String?,
    val favicon: String?,
    val canonicalUrl: String?,
    val wordCount: Int?,
    val originalWordCount: Int?,
    val compressionRatio: Double?,
    val readingTimeSeconds: Int?,
    val style: String?
)

data class ChatResult(
    val answer: String,
    val sources: List<String>
)

data class YoutubeSummaryData(
    val content: String,
    val documentId: String?,
    val title: String?,
    val channel: String?,
    val channelUrl: String?,
    val durationSeconds: Int?,
    val thumbnailUrl: String?,
    val viewCount: Int?,
    val publishDate: String?,
    val description: String?,
    val language: String?,
    val wordCount: Int?,
    val transcriptWordCount: Int?,
    val videoDurationFormatted: String?,
    val style: String?
)

interface AIService {
    suspend fun generateSummary(
        text: String,
        style: String
    ): Result<String>

    suspend fun generateYoutubeSummary(
        url: String,
        style: String
    ): Result<YoutubeSummaryData>

    suspend fun generateWebsiteSummary(
        url: String,
        style: String
    ): Result<WebsiteSummaryData>

    suspend fun generatePdfSummary(
        uri: Uri,
        style: String,
        contentResolver: android.content.ContentResolver
    ): Result<PdfSummaryData>

    suspend fun chat(
        documentId: String,
        question: String,
        history: List<ChatMessage>? = null
    ): Result<ChatResult>

    suspend fun performTask(
        documentId: String,
        taskType: String,
        language: String? = "English"
    ): Result<String>
}
