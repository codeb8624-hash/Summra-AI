package com.example.summraai.domain.repository

import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.model.SummaryStyle
import kotlinx.coroutines.flow.Flow

interface SummaryRepository {
    suspend fun getSummaries(): List<Summary>
    fun getSummariesFlow(): Flow<List<Summary>>
    suspend fun getSummaryById(id: String): Summary?
    fun getSummaryByIdFlow(id: String): Flow<Summary?>
    suspend fun saveSummary(summary: Summary)
    suspend fun deleteSummary(id: String)
    suspend fun toggleBookmark(id: String)
    suspend fun getSummariesByStyle(style: SummaryStyle): List<Summary>
    fun searchSummariesFlow(query: String): Flow<List<Summary>>
}
