package com.example.summraai.data.repository

import com.example.summraai.domain.model.SummaryStyle

interface AISummaryRepository {
    suspend fun generateTextSummary(text: String, style: SummaryStyle): Result<String>
}
