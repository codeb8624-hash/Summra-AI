package com.example.summraai.ai.config

data class AIConfig(
    val backendBaseUrl: String = "http://10.221.173.32:8000"
) {
    companion object {
        const val DEFAULT_BACKEND_URL = "http://10.221.173.32:8000"
    }
}
