package com.example.summraai.domain.model

data class Settings(
    val defaultStyle: SummaryStyle,
    val notificationsEnabled: Boolean,
    val darkMode: Boolean
)
