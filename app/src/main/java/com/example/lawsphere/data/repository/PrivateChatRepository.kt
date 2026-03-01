package com.example.lawsphere.data.repository

import com.example.lawsphere.domain.model.PrivateMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PrivateChatRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String? get() = auth.currentUser?.uid

    fun getChatRoomId(otherUserId: String): String {
        val myId = currentUserId ?: return ""
        return if (myId < otherUserId) "${myId}_${otherUserId}" else "${otherUserId}_${myId}"
    }

    fun getMessages(chatRoomId: String): Flow<List<PrivateMessage>> = callbackFlow {
        val ref = db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val msgs = snapshot?.toObjects(PrivateMessage::class.java) ?: emptyList()
            trySend(msgs)
        }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatRoomId: String, text: String) {
        val myId = currentUserId ?: return
        val message = PrivateMessage(
            senderId = myId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .add(message)
            .await()

        // Optional: Update last message in outer document for a "Recent Chats" list feature later
        db.collection("private_chats").document(chatRoomId).set(
            mapOf("lastMessage" to text, "timestamp" to System.currentTimeMillis())
        )
    }
}