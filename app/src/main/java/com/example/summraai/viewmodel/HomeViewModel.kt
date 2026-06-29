package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _recentSummaries = MutableStateFlow<UiState<List<Summary>>>(UiState.Idle)
    val recentSummaries: StateFlow<UiState<List<Summary>>> = _recentSummaries.asStateFlow()

    init {
        loadRecentSummaries()
    }

    fun loadRecentSummaries() {
        viewModelScope.launch {
            _recentSummaries.value = UiState.Loading
            try {
                val summaries = summaryRepository.getSummaries()
                _recentSummaries.value = if (summaries.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(summaries.take(5))
                }
            } catch (e: Exception) {
                _recentSummaries.value = UiState.Error(e.message ?: "Failed to load summaries")
            }
        }
    }
}
