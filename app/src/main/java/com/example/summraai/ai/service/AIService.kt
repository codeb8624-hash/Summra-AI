package com.example.summraai.ai.service

import android.net.Uri

data class PdfSummaryData(
    val content: String,
    val fileName: String?,
    val pages: Int?,
    val wordCount: Int?
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
}
