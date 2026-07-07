package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summraai.SummraApplication
import com.example.summraai.core.common.UiState
import com.example.summraai.data.local.SummraDatabase
import com.example.summraai.data.repository.RoomSummaryRepository
import com.example.summraai.domain.model.HistoryItem
import com.example.summraai.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = SummraDatabase.getInstance(SummraApplication.instance)
                val repo = RoomSummaryRepository(db.summaryDao(), db.collectionDao())
                return HistoryViewModel(repo) as T
            }
        }
    }

    val historyItems: StateFlow<UiState<List<HistoryItem>>> =
        historyRepository.getHistory().map { items ->
            if (items.isEmpty()) UiState.Empty else UiState.Success(items)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Idle)

    fun removeItem(id: String) {
        viewModelScope.launch {
            historyRepository.removeFromHistory(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
