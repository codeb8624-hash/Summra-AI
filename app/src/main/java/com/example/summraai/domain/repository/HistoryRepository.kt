package com.example.summraai.domain.repository

import com.example.summraai.domain.model.HistoryItem

interface HistoryRepository {
    suspend fun getHistory(): List<HistoryItem>
    suspend fun addToHistory(item: HistoryItem)
    suspend fun removeFromHistory(id: String)
    suspend fun clearHistory()
}
