package com.example.summraai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.Settings
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow<UiState<Settings>>(UiState.Idle)
    val settings: StateFlow<UiState<Settings>> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _settings.value = UiState.Loading
            try {
                val result = settingsRepository.getSettings()
                _settings.value = UiState.Success(result)
            } catch (e: Exception) {
                _settings.value = UiState.Error(e.message ?: "Failed to load settings")
            }
        }
    }

    fun updateDefaultStyle(style: SummaryStyle) {
        viewModelScope.launch {
            settingsRepository.updateDefaultStyle(style)
            loadSettings()
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleNotifications(enabled)
            loadSettings()
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleDarkMode(enabled)
            loadSettings()
        }
    }
}
