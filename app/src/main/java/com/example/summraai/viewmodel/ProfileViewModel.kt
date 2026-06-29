package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.UserProfile
import com.example.summraai.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<UiState<UserProfile>>(UiState.Idle)
    val profile: StateFlow<UiState<UserProfile>> = _profile.asStateFlow()
}
