package com.example.lawsphere.domain.model

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<String> = emptyList(), // UI expects simple Strings
    val timestamp: Long = System.currentTimeMillis()
)