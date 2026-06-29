package com.example.summraai.ai.service

import com.example.summraai.ai.config.AIConfig

interface AIService {
    suspend fun generateSummary(
        prompt: String,
        config: AIConfig
    ): Result<String>
}
