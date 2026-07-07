package com.example.summraai.domain.repository

import com.example.summraai.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(): Flow<List<HistoryItem>>
    suspend fun addToHistory(item: HistoryItem)
    suspend fun removeFromHistory(id: String)
    suspend fun clearHistory()
}
