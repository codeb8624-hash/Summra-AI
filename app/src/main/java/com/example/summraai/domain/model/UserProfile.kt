package com.example.summraai.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val totalSummaries: Int,
    val joinDate: Long
)
