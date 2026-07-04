package com.example.summraai.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.summraai.data.remote.ChatMessage
import com.example.summraai.data.remote.YoutubeMetadata
import com.example.summraai.ai.service.ChatResult
import com.example.summraai.domain.model.SummaryStyle

data class PdfSummaryResult(
    val content: String,
    val style: SummaryStyle,
    val documentId: String? = null,
    val fileName: String?,
    val pageCount: Int?,
    val wordCount: Int?
)

data class WebsiteSummaryResult(
    val content: String,
    val style: SummaryStyle,
    val documentId: String? = null,
    val title: String? = null,
    val domain: String? = null,
    val author: String? = null,
    val publishedDate: String? = null,
    val language: String? = null,
    val description: String? = null,
    val ogImage: String? = null,
    val favicon: String? = null,
    val canonicalUrl: String? = null,
    val wordCount: Int? = null,
    val originalWordCount: Int? = null,
    val compressionRatio: Double? = null,
    val readingTimeSeconds: Int? = null
)

data class YoutubeSummaryResult(
    val content: String,
    val style: SummaryStyle,
    val documentId: String? = null,
    val title: String? = null,
    val channel: String? = null,
    val channelUrl: String? = null,
    val durationSeconds: Int? = null,
    val thumbnailUrl: String? = null,
    val viewCount: Int? = null,
    val publishDate: String? = null,
    val description: String? = null,
    val language: String? = null,
    val wordCount: Int? = null,
    val transcriptWordCount: Int? = null,
    val videoDurationFormatted: String? = null
)

interface AISummaryRepository {
    suspend fun generateTextSummary(
        text: String,
        style: SummaryStyle
    ): Result<String>

    suspend fun generateWebsiteSummary(
        url: String,
        style: SummaryStyle
    ): Result<WebsiteSummaryResult>

    suspend fun generateYoutubeSummary(
        url: String,
        style: SummaryStyle
    ): Result<YoutubeSummaryResult>

    suspend fun generatePdfSummary(
        uri: Uri,
        style: SummaryStyle,
        contentResolver: ContentResolver
    ): Result<PdfSummaryResult>

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
