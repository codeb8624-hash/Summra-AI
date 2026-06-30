package com.example.summraai.data.remote

data class SummarizeRequest(
    val text: String,
    val style: String
)

data class SummarizeResponse(
    val success: Boolean,
    val summary: String?,
    val message: String?
)
