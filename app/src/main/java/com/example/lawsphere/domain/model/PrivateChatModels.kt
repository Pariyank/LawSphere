package com.example.lawsphere.domain.model

data class PrivateMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class InboxItem(
    val roomId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
)