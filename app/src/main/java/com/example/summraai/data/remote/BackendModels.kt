package com.example.summraai.data.remote

import com.squareup.moshi.Json

data class SummarizeRequest(
    val text: String,
    val style: String
)

data class SummarizeResponse(
    val success: Boolean,
    val summary: String?,
    val message: String?
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

data class TaskResponse(
    val content: String
)
