package com.example.summraai.ai.service

interface AIService {
    suspend fun generateSummary(
        text: String,
        style: String
    ): Result<String>
}
