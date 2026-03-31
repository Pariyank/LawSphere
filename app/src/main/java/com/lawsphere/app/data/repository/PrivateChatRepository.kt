package com.lawsphere.app.data.repository

import com.lawsphere.app.domain.model.PrivateMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.lawsphere.app.domain.model.InboxItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PrivateChatRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun getChatRoomId(otherUserId: String): String {
        val myId = currentUserId ?: return ""
        return if (myId < otherUserId) "${myId}_${otherUserId}"
        else "${otherUserId}_${myId}"
    }

    fun getInbox(): Flow<List<InboxItem>> = callbackFlow {

        val myId = currentUserId

        if (myId == null) {
            trySend(emptyList()).isSuccess
            close()
            return@callbackFlow
        }

        val listener = db.collection("private_chats")
            .whereArrayContains("participants", myId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->

                    val participants = doc.get("participants") as? List<String> ?: emptyList()
                    val names = doc.get("names") as? Map<String, String> ?: emptyMap()

                    val otherId = participants.firstOrNull { it != myId } ?: ""
                    val otherName = names[otherId] ?: "User"

                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    InboxItem(
                        roomId = doc.id,
                        otherUserId = otherId,
                        otherUserName = otherName,
                        lastMessage = lastMessage,
                        timestamp = timestamp
                    )
                }?.sortedByDescending { it.timestamp } ?: emptyList()

                trySend(items).isSuccess
            }

        awaitClose { listener.remove() }
    }

    private fun markDelivered(chatRoomId: String, messageId: String) {
        val myId = currentUserId ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("private_chats")
                    .document(chatRoomId)
                    .collection("messages")
                    .document(messageId)
                    .update("deliveredTo", FieldValue.arrayUnion(myId))
                    .await()
            } catch (_: Exception) {}
        }
    }

    suspend fun markSeen(chatRoomId: String, messageId: String) {
        val myId = currentUserId ?: return

        db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .document(messageId)
            .update("seenBy", FieldValue.arrayUnion(myId))
            .await()
    }

    fun getMessages(chatRoomId: String): Flow<List<PrivateMessage>> = callbackFlow {

        val myId = currentUserId

        if (myId == null) {
            trySend(emptyList()).isSuccess
            close()
            return@callbackFlow
        }

        val ref = db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = ref.addSnapshotListener { snapshot, error ->

            if (error != null) {
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }

            val msgs = snapshot?.documents?.mapNotNull { doc ->
                val msg = doc.toObject(PrivateMessage::class.java)?.copy(id = doc.id)

                if (msg != null && msg.senderId != myId && !msg.deliveredTo.contains(myId)) {
                    markDelivered(chatRoomId, doc.id)
                }

                msg
            }?.filter { msg ->
                !msg.deletedBy.contains(myId)
            } ?: emptyList()

            trySend(msgs).isSuccess
        }

        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(otherUserId: String, otherUserName: String, text: String) {

        val myId = currentUserId ?: return
        val roomId = getChatRoomId(otherUserId)

        val myDoc = db.collection("users").document(myId).get().await()
        val myName = myDoc.getString("name") ?: "User"

        val message = PrivateMessage(
            senderId = myId,
            text = text,
            timestamp = System.currentTimeMillis(),
            deliveredTo = listOf(myId),
            seenBy = listOf(myId)
        )

        db.collection("private_chats")
            .document(roomId)
            .collection("messages")
            .add(message)
            .await()

        val roomData = hashMapOf(
            "participants" to listOf(myId, otherUserId),
            "names" to mapOf(myId to myName, otherUserId to otherUserName),
            "lastMessage" to text,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("private_chats")
            .document(roomId)
            .set(roomData, SetOptions.merge())
            .await()
    }


    suspend fun deleteForEveryone(chatRoomId: String, messageId: String) {
        db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .document(messageId)
            .delete()
            .await()
    }

    suspend fun deleteForMe(chatRoomId: String, messageId: String) {
        val myId = currentUserId ?: return

        db.collection("private_chats")
            .document(chatRoomId)
            .collection("messages")
            .document(messageId)
            .update("deletedBy", FieldValue.arrayUnion(myId))
            .await()
    }
}