package com.example.summraai.domain.model

data class SummaryCollection(
    val id: String,
    val name: String,
    val description: String,
    val summaryCount: Int,
    val createdAt: Long
)
