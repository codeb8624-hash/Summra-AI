package com.example.summraai.viewmodel

data class ChatUiMessage(
    val role: String,
    val content: String,
    val sources: List<String> = emptyList()
)
