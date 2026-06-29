package com.example.summraai.domain.repository

import com.example.summraai.domain.model.Settings
import com.example.summraai.domain.model.SummaryStyle

interface SettingsRepository {
    suspend fun getSettings(): Settings
    suspend fun updateDefaultStyle(style: SummaryStyle)
    suspend fun toggleNotifications(enabled: Boolean)
    suspend fun toggleDarkMode(enabled: Boolean)
}
