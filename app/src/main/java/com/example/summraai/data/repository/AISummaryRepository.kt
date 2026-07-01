package com.example.summraai.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.summraai.data.remote.ChatMessage
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

interface AISummaryRepository {
    suspend fun generateTextSummary(
        text: String,
        style: SummaryStyle
    ): Result<String>

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
