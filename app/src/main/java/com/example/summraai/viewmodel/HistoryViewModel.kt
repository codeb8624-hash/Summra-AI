package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.HistoryItem
import com.example.summraai.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _historyItems = MutableStateFlow<UiState<List<HistoryItem>>>(UiState.Idle)
    val historyItems: StateFlow<UiState<List<HistoryItem>>> = _historyItems.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyItems.value = UiState.Loading
            try {
                val items = historyRepository.getHistory()
                _historyItems.value = if (items.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(items)
                }
            } catch (e: Exception) {
                _historyItems.value = UiState.Error(e.message ?: "Failed to load history")
            }
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch {
            historyRepository.removeFromHistory(id)
            loadHistory()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            loadHistory()
        }
    }
}
