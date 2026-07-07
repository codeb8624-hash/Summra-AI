package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summraai.SummraApplication
import com.example.summraai.core.common.UiState
import com.example.summraai.data.local.SummraDatabase
import com.example.summraai.data.repository.RoomSummaryRepository
import com.example.summraai.domain.model.SummaryCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CollectionsViewModel(
    private val repository: RoomSummaryRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = SummraDatabase.getInstance(SummraApplication.instance)
                val repo = RoomSummaryRepository(db.summaryDao(), db.collectionDao())
                return CollectionsViewModel(repo) as T
            }
        }
    }

    private val _collections = MutableStateFlow<UiState<List<SummaryCollection>>>(UiState.Loading)
    val collections: StateFlow<UiState<List<SummaryCollection>>> = _collections.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getAllCollectionsFlow().collect { list ->
                    _collections.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
            } catch (e: Exception) {
                _collections.value = UiState.Error(e.message ?: "Failed to load collections")
            }
        }
    }
}
