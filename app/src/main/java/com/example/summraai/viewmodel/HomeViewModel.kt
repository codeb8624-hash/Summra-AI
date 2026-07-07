package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summraai.SummraApplication
import com.example.summraai.core.common.UiState
import com.example.summraai.data.local.SummraDatabase
import com.example.summraai.data.repository.RoomSummaryRepository
import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.model.SummaryCollection
import com.example.summraai.domain.repository.SummaryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
class HomeViewModel(
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val roomRepo: RoomSummaryRepository
        get() = summaryRepository as RoomSummaryRepository

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = SummraDatabase.getInstance(SummraApplication.instance)
                val repo = RoomSummaryRepository(db.summaryDao(), db.collectionDao())
                return HomeViewModel(repo) as T
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<UiState<List<Summary>>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                summaryRepository.searchSummariesFlow(query)
            }
        }
        .map { summaries ->
            if (summaries.isEmpty()) UiState.Empty else UiState.Success(summaries)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Idle)

    val recentSummaries: StateFlow<UiState<List<Summary>>> =
        summaryRepository.getSummariesFlow().map { summaries ->
            val recent = summaries.take(5)
            if (recent.isEmpty()) UiState.Empty else UiState.Success(recent)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Idle)

    val collections: StateFlow<List<SummaryCollection>> =
        roomRepo.getAllCollectionsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteSummary(id: String) {
        viewModelScope.launch {
            summaryRepository.deleteSummary(id)
        }
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            summaryRepository.toggleBookmark(id)
        }
    }

    fun addToCollection(summaryId: String, collectionId: String) {
        viewModelScope.launch {
            roomRepo.addSummaryToCollection(collectionId, summaryId)
        }
    }
}
