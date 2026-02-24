package com.example.lawsphere.domain.model

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)