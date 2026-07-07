package com.example.summraai.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summraai.SummraApplication
import com.example.summraai.core.common.UiState
import com.example.summraai.data.local.SummraDatabase
import com.example.summraai.data.repository.AISummaryRepository
import com.example.summraai.data.repository.AISummaryRepositoryImpl
import com.example.summraai.data.repository.RoomSummaryRepository
import com.example.summraai.domain.model.HistoryItem
import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.domain.model.SummaryType
import com.example.summraai.domain.repository.HistoryRepository
import com.example.summraai.domain.repository.SummaryRepository
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
    private val repository: AISummaryRepository = AISummaryRepositoryImpl(),
    private val summaryRepository: SummaryRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = SummraDatabase.getInstance(SummraApplication.instance)
                val repo = RoomSummaryRepository(db.summaryDao(), db.collectionDao())
                return TextSummaryViewModel(
                    summaryRepository = repo,
                    historyRepository = repo
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow<UiState<SummaryResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<SummaryResult>> = _uiState.asStateFlow()

    private var generateJob: Job? = null

    fun generateSummary(text: String, style: SummaryStyle) {
        generateJob?.cancel()

        _uiState.value = UiState.Loading
        generateJob = viewModelScope.launch {
            repository.generateTextSummary(text, style)
                .onSuccess { content ->
                    val wordCount = content.split("\\s+".toRegex()).size
                    val id = java.util.UUID.randomUUID().toString()
                    val title = text.take(50).trimEnd() + if (text.length > 50) "..." else ""

                    Log.d("PIPELINE", "TextSummaryViewModel.generateSummary: title=$title, wordCount=$wordCount")

                    val summary = Summary(
                        id = id,
                        title = title,
                        content = content,
                        type = SummaryType.TEXT,
                        source = "",
                        createdAt = System.currentTimeMillis(),
                        wordCount = wordCount,
                        tags = emptyList(),
                        isBookmarked = false,
                        style = style
                    )
                    summaryRepository.saveSummary(summary)
                    historyRepository.addToHistory(
                        HistoryItem(
                            id = id,
                            title = title,
                            summary = content,
                            type = SummaryType.TEXT,
                            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    Log.d("PIPELINE", "TextSummaryViewModel: save & history complete, setting Success state")

                    _uiState.value = UiState.Success(
                        SummaryResult(
                            content = content,
                            style = style,
                            wordCount = wordCount
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
