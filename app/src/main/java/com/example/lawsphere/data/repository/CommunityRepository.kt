package com.example.lawsphere.data.repository

import com.example.lawsphere.domain.model.ForumAnswer
import com.example.lawsphere.domain.model.ForumPost
import com.example.lawsphere.domain.model.LawyerProfile
import com.example.lawsphere.domain.model.NewsArticle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommunityRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getLawyers(): Result<List<LawyerProfile>> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", "lawyer")
                .get().await()
            val lawyers = snapshot.toObjects(LawyerProfile::class.java)
            Result.success(lawyers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPosts(): Result<List<ForumPost>> {
        return try {
            val snapshot = db.collection("forum_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            val posts = snapshot.toObjects(ForumPost::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(title: String, desc: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No User"))
            val ref = db.collection("forum_posts").document()
            val post = ForumPost(
                id = ref.id,
                title = title,
                description = desc,
                authorId = uid,
                timestamp = System.currentTimeMillis()
            )
            ref.set(post).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAnswer(postId: String, answer: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No User"))

            val userDoc = db.collection("users").document(uid).get().await()
            val name = userDoc.getString("name") ?: "Legal Expert"

            val newAnswer = ForumAnswer(name, answer, System.currentTimeMillis())

            db.collection("forum_posts").document(postId)
                .update("answers", com.google.firebase.firestore.FieldValue.arrayUnion(newAnswer))
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLegalNews(): List<NewsArticle> {
        return listOf(
            NewsArticle("Supreme Court on BNS", "SC clarifies retrospective applicability of new criminal laws in pending cases.", "LiveLaw", "2 Hours ago"),
            NewsArticle("High Court Digitization", "Delhi High Court launches new e-filing portal for faster processing.", "Bar & Bench", "5 Hours ago"),
            NewsArticle("Data Privacy Act", "New amendments proposed to the Digital Personal Data Protection Act.", "The Hindu", "1 Day ago"),
            NewsArticle("Bail Reforms", "Govt issues new guidelines for bail in economic offenses under BNS.", "Times of India", "2 Days ago")
        )
    }
}