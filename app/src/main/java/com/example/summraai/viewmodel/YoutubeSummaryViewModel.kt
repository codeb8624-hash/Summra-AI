package com.example.summraai.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summraai.SummraApplication
import com.example.summraai.core.common.UiState
import com.example.summraai.data.local.SummraDatabase
import com.example.summraai.data.remote.ChatMessage
import com.example.summraai.data.repository.AISummaryRepository
import com.example.summraai.data.repository.AISummaryRepositoryImpl
import com.example.summraai.data.repository.RoomSummaryRepository
import com.example.summraai.data.repository.YoutubeSummaryResult
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

class YoutubeSummaryViewModel(
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
                return YoutubeSummaryViewModel(
                    summaryRepository = repo,
                    historyRepository = repo
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow<UiState<YoutubeSummaryResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<YoutubeSummaryResult>> = _uiState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatUiMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private var generateJob: Job? = null
    private var chatJob: Job? = null

    fun generateSummary(url: String, style: SummaryStyle) {
        generateJob?.cancel()
        _chatMessages.value = emptyList()

        _uiState.value = UiState.Loading
        generateJob = viewModelScope.launch {
            repository.generateYoutubeSummary(url, style)
                .onSuccess { result ->
                    val id = java.util.UUID.randomUUID().toString()
                    val summary = Summary(
                        id = id,
                        title = result.title ?: result.channel ?: "YouTube Summary",
                        content = result.content,
                        type = SummaryType.YOUTUBE,
                        source = result.channel ?: url,
                        createdAt = System.currentTimeMillis(),
                        wordCount = result.content.split("\\s+".toRegex()).size,
                        tags = emptyList(),
                        isBookmarked = false,
                        style = style
                    )
                    Log.d("PIPELINE", "YoutubeSummaryViewModel: saving summary id=$id title=${result.title}")
                    summaryRepository.saveSummary(summary)
                    historyRepository.addToHistory(
                        HistoryItem(
                            id = id,
                            title = result.title ?: result.channel ?: "YouTube Summary",
                            summary = result.content,
                            type = SummaryType.YOUTUBE,
                            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    Log.d("PIPELINE", "YoutubeSummaryViewModel: save+history done for id=$id")

                    _uiState.value = UiState.Success(result)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(
                        error.message ?: "Something went wrong. Please try again later."
                    )
                }
        }
    }

    fun askQuestion(question: String) {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return
        val documentId = currentState.data.documentId ?: return

        chatJob?.cancel()

        val userMessage = ChatUiMessage(role = "user", content = question)
        _chatMessages.value = _chatMessages.value + userMessage

        _isChatLoading.value = true
        chatJob = viewModelScope.launch {
            val history = _chatMessages.value.map { ChatMessage(it.role, it.content) }
            repository.chat(documentId, question, history)
                .onSuccess { result ->
                    val aiMessage = ChatUiMessage(
                        role = "assistant",
                        content = result.answer,
                        sources = result.sources
                    )
                    _chatMessages.value = _chatMessages.value + aiMessage
                    _isChatLoading.value = false
                }
                .onFailure { error ->
                    val errorMessage = ChatUiMessage(
                        role = "assistant",
                        content = "Error: ${error.message ?: "Failed to get answer"}"
                    )
                    _chatMessages.value = _chatMessages.value + errorMessage
                    _isChatLoading.value = false
                }
        }
    }

    fun performTask(taskType: String, language: String = "English") {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return
        val documentId = currentState.data.documentId ?: return

        _isChatLoading.value = true
        viewModelScope.launch {
            repository.performTask(documentId, taskType, language)
                .onSuccess { content ->
                    val aiMessage = ChatUiMessage(role = "assistant", content = content)
                    _chatMessages.value = _chatMessages.value + aiMessage
                    _isChatLoading.value = false
                }
                .onFailure { error ->
                    val errorMessage = ChatUiMessage(
                        role = "assistant",
                        content = "Error: ${error.message ?: "Failed to perform task"}"
                    )
                    _chatMessages.value = _chatMessages.value + errorMessage
                    _isChatLoading.value = false
                }
        }
    }

    fun resetState() {
        generateJob?.cancel()
        chatJob?.cancel()
        _uiState.value = UiState.Idle
        _chatMessages.value = emptyList()
        _isChatLoading.value = false
    }
}
