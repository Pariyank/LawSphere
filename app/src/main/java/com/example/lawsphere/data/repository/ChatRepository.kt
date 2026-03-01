package com.example.lawsphere.data.repository

import com.example.lawsphere.domain.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getChatHistory(): Flow<List<ChatMessage>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref = db.collection("users").document(uid)
            .collection("chat_history")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(messages)
        }

        awaitClose { listener.remove() }
    }

    suspend fun saveMessage(message: ChatMessage) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("chat_history")
            .add(message)
            .await()
    }

    suspend fun clearChatHistory() {
        val uid = auth.currentUser?.uid ?: return
        val batch = db.batch()
        val ref = db.collection("users").document(uid).collection("chat_history")

        val snapshot = ref.get().await()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }
}