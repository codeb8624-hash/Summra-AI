package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.domain.model.SummaryType
import com.example.summraai.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SummaryViewModel(
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Summary>>(UiState.Idle)
    val uiState: StateFlow<UiState<Summary>> = _uiState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedStyle = MutableStateFlow(SummaryStyle.CONCISE)
    val selectedStyle: StateFlow<SummaryStyle> = _selectedStyle.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun updateStyle(style: SummaryStyle) {
        _selectedStyle.value = style
    }

    fun generateSummary(type: SummaryType) {
        _uiState.value = UiState.Loading
    }
}
