package com.example.summraai.ai.config

data class AIConfig(
    val modelName: String = "gemini-2.0-flash",
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 4096,
    val apiKey: String = ""
)
