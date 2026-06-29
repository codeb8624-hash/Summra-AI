package com.example.summraai.domain.repository

import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.model.SummaryStyle

interface SummaryRepository {
    suspend fun getSummaries(): List<Summary>
    suspend fun getSummaryById(id: String): Summary?
    suspend fun saveSummary(summary: Summary)
    suspend fun deleteSummary(id: String)
    suspend fun toggleBookmark(id: String)
    suspend fun getSummariesByStyle(style: SummaryStyle): List<Summary>
}
