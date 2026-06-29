package com.example.summraai.domain.model

data class HistoryItem(
    val id: String,
    val title: String,
    val summary: String,
    val type: SummaryType,
    val date: String,
    val timestamp: Long
)
