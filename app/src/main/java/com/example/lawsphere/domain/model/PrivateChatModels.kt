package com.example.lawsphere.domain.model

import com.google.firebase.firestore.DocumentId

data class PrivateMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deletedBy: List<String> = emptyList()
)

data class InboxItem(
    val roomId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
)