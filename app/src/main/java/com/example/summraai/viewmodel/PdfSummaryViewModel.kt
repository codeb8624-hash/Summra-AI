package com.example.summraai.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.data.remote.ChatMessage
import com.example.summraai.data.repository.AISummaryRepository
import com.example.summraai.data.repository.AISummaryRepositoryImpl
import com.example.summraai.data.repository.PdfSummaryResult
import com.example.summraai.domain.model.SummaryStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiMessage(
    val role: String,
    val content: String,
    val sources: List<String> = emptyList()
)

class PdfSummaryViewModel(
    private val repository: AISummaryRepository = AISummaryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<PdfSummaryResult>>(UiState.Idle)
    val uiState: StateFlow<UiState<PdfSummaryResult>> = _uiState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatUiMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private var generateJob: Job? = null
    private var chatJob: Job? = null

    fun generatePdfSummary(uri: Uri, style: SummaryStyle, contentResolver: ContentResolver) {
        generateJob?.cancel()
        _chatMessages.value = emptyList()

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

    fun askQuestion(question: String) {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return
        val documentId = currentState.data.documentId ?: return

        chatJob?.cancel()
        
        // Add user message
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
