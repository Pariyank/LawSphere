package com.lawsphere.app.domain.model

import com.google.firebase.firestore.PropertyName

data class ChatMessage(
    val id: String = "",
    val text: String = "",

    @get:PropertyName("isUser") @set:PropertyName("isUser")
    var isUser: Boolean = false,

    val sources: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)