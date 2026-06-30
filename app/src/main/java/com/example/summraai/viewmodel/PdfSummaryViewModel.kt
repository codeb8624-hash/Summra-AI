package com.example.summraai.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.data.repository.AISummaryRepository
import com.example.summraai.data.repository.AISummaryRepositoryImpl
import com.example.summraai.data.repository.PdfSummaryResult
import com.example.summraai.domain.model.SummaryStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PdfSummaryViewModel(
    private val repository: AISummaryRepository = AISummaryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<PdfSummaryResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<PdfSummaryResult>> = _uiState.asStateFlow()

    private var generateJob: Job? = null

    fun generatePdfSummary(uri: Uri, style: SummaryStyle, contentResolver: ContentResolver) {
        generateJob?.cancel()

        _uiState.value = UiState.Loading
        generateJob = viewModelScope.launch {
            repository.generatePdfSummary(uri, style, contentResolver)
                .onSuccess { result ->
                    _uiState.value = UiState.Success(result)
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
