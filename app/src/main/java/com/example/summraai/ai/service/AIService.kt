package com.example.summraai.ai.service

import android.net.Uri
import com.example.summraai.data.remote.ChatMessage

data class PdfSummaryData(
    val content: String,
    val documentId: String?,
    val fileName: String?,
    val pages: Int?,
    val wordCount: Int?
)

data class ChatResult(
    val answer: String,
    val sources: List<String>
)

interface AIService {
    suspend fun generateSummary(
        text: String,
        style: String
    ): Result<String>

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
