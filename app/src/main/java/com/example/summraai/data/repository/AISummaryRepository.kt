package com.example.summraai.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.summraai.domain.model.SummaryStyle

data class PdfSummaryResult(
    val content: String,
    val style: SummaryStyle,
    val fileName: String?,
    val pageCount: Int?,
    val wordCount: Int?
)

interface AISummaryRepository {
    suspend fun generateTextSummary(text: String, style: SummaryStyle): Result<String>

    suspend fun generatePdfSummary(
        uri: Uri,
        style: SummaryStyle,
        contentResolver: ContentResolver
    ): Result<PdfSummaryResult>
}
