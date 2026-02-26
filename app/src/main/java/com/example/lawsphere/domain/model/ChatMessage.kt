package com.example.lawsphere.domain.model

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val isUser: Boolean = false,
    val sources: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)