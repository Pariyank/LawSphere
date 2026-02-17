package com.example.lawsphere.data.repository

import com.example.lawsphere.domain.model.CaseFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CaseRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get current user's ID
    private val userId = auth.currentUser?.uid

    // 1. Add New Case
    suspend fun addCase(caseFile: CaseFile): Result<Boolean> {
        return try {
            if (userId == null) throw Exception("User not logged in")

            val newDocRef = db.collection("users").document(userId)
                .collection("cases").document()

            // Save with ID
            val caseWithId = caseFile.copy(id = newDocRef.id)
            newDocRef.set(caseWithId).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Get All Cases (Real-time not strictly needed, fetch once is fine for now)
    suspend fun getCases(): Result<List<CaseFile>> {
        return try {
            if (userId == null) throw Exception("User not logged in")

            val snapshot = db.collection("users").document(userId)
                .collection("cases")
                .orderBy("nextHearingDate", Query.Direction.ASCENDING)
                .get().await()

            val cases = snapshot.toObjects(CaseFile::class.java)
            Result.success(cases)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Delete Case
    suspend fun deleteCase(caseId: String): Result<Boolean> {
        return try {
            if (userId == null) throw Exception("User not logged in")

            db.collection("users").document(userId)
                .collection("cases").document(caseId)
                .delete().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}