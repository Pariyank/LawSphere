package com.lawsphere.app.data.repository

import com.lawsphere.app.domain.model.LawyerProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
}