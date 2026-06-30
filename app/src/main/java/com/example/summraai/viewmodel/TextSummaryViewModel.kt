package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.data.repository.AISummaryRepository
import com.example.summraai.data.repository.AISummaryRepositoryImpl
import com.example.summraai.domain.model.SummaryStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryResult(
    val content: String,
    val style: SummaryStyle,
    val wordCount: Int
)

class TextSummaryViewModel(
    private val repository: AISummaryRepository = AISummaryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SummaryResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<SummaryResult>> = _uiState.asStateFlow()

    private var generateJob: Job? = null

    fun generateSummary(text: String, style: SummaryStyle) {
        generateJob?.cancel()

        _uiState.value = UiState.Loading
        generateJob = viewModelScope.launch {
            repository.generateTextSummary(text, style)
                .onSuccess { content ->
                    _uiState.value = UiState.Success(
                        SummaryResult(
                            content = content,
                            style = style,
                            wordCount = content.split("\\s+".toRegex()).size
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(
                        error.message ?: "Something went wrong. Please try again later."
                    )
                }
        }
    }

    fun resetState() {
        generateJob?.cancel()
        _uiState.value = UiState.Idle
    }
}
