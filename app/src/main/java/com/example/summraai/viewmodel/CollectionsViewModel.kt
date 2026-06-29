package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.SummaryCollection
import com.example.summraai.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CollectionsViewModel(
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _collections = MutableStateFlow<UiState<List<SummaryCollection>>>(UiState.Idle)
    val collections: StateFlow<UiState<List<SummaryCollection>>> = _collections.asStateFlow()
}
