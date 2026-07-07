package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summraai.SummraApplication
import com.example.summraai.data.local.SummraDatabase
import com.example.summraai.data.repository.RoomSummaryRepository
import com.example.summraai.domain.model.Summary
import com.example.summraai.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SummaryDetailViewModel(
    private val summaryRepository: SummaryRepository,
    summaryId: String
) : ViewModel() {

    val summary: StateFlow<Summary?> =
        summaryRepository.getSummaryByIdFlow(summaryId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun factory(summaryId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = SummraDatabase.getInstance(SummraApplication.instance)
                val repo = RoomSummaryRepository(db.summaryDao(), db.collectionDao())
                return SummaryDetailViewModel(repo, summaryId) as T
            }
        }
    }
}
