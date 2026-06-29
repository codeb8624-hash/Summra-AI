package com.example.summraai.ai.model

data class SummaryUiState(
    val isLoading: Boolean = false,
    val summaryText: String = "",
    val error: String? = null,
    val isGenerated: Boolean = false
)
