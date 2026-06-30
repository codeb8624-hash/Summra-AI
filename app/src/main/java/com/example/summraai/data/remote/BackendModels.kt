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
    val message: String?,
    @param:Json(name = "file_name") val fileName: String?,
    val pages: Int?,
    @param:Json(name = "word_count") val wordCount: Int?
)
