package com.example.summraai.data.remote

import com.squareup.moshi.Json

data class SummarizeRequest(
    val text: String,
    val style: String
)

data class WebsiteSummarizeRequest(
    val url: String,
    val style: String
)

data class SummarizeResponse(
    val success: Boolean,
    val summary: String?,
    val message: String?
)

data class WebsiteMetadata(
    val title: String?,
    val domain: String?,
    val author: String?,
    @param:Json(name = "published_date") val publishedDate: String?,
    val language: String?,
    val description: String?,
    @param:Json(name = "og_image") val ogImage: String?,
    val favicon: String?,
    @param:Json(name = "canonical_url") val canonicalUrl: String?
)

data class WebsiteSummarizeResponse(
    val success: Boolean,
    val summary: String?,
    @param:Json(name = "document_id") val documentId: String?,
    val message: String?,
    val metadata: WebsiteMetadata?,
    @param:Json(name = "word_count") val wordCount: Int?,
    @param:Json(name = "original_word_count") val originalWordCount: Int?,
    @param:Json(name = "compression_ratio") val compressionRatio: Double?,
    @param:Json(name = "reading_time_seconds") val readingTimeSeconds: Int?,
    val style: String?
)

data class PdfSummarizeResponse(
    val success: Boolean,
    val summary: String?,
    @param:Json(name = "document_id") val documentId: String?,
    val message: String?,
    @param:Json(name = "file_name") val fileName: String?,
    val pages: Int?,
    @param:Json(name = "word_count") val wordCount: Int?
)

data class ChatRequest(
    val documentId: String,
    val question: String,
    val history: List<ChatMessage>? = null
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatResponse(
    val answer: String,
    val sources: List<String>
)

data class TaskRequest(
    val documentId: String,
    val taskType: String,
    val language: String? = "English"
)

data class YoutubeSummarizeRequest(
    val url: String,
    val style: String
)

data class YoutubeMetadata(
    val title: String?,
    val channel: String?,
    @param:Json(name = "channel_url") val channelUrl: String?,
    @param:Json(name = "duration_seconds") val durationSeconds: Int?,
    @param:Json(name = "thumbnail_url") val thumbnailUrl: String?,
    @param:Json(name = "view_count") val viewCount: Int?,
    @param:Json(name = "publish_date") val publishDate: String?,
    val description: String?,
    val language: String?
)

data class YoutubeSummarizeResponse(
    val success: Boolean,
    val summary: String?,
    @param:Json(name = "document_id") val documentId: String?,
    val message: String?,
    val metadata: YoutubeMetadata?,
    @param:Json(name = "word_count") val wordCount: Int?,
    @param:Json(name = "transcript_word_count") val transcriptWordCount: Int?,
    @param:Json(name = "video_duration_formatted") val videoDurationFormatted: String?,
    val style: String?
)

data class TaskResponse(
    val content: String
)
