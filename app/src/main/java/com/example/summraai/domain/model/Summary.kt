package com.example.summraai.domain.model

data class Summary(
    val id: String,
    val title: String,
    val content: String,
    val type: SummaryType,
    val source: String,
    val createdAt: Long,
    val wordCount: Int,
    val tags: List<String>,
    val isBookmarked: Boolean,
    val style: SummaryStyle
)
