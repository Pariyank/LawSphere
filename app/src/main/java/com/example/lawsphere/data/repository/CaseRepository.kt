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


    private val userId = auth.currentUser?.uid


    suspend fun addCase(caseFile: CaseFile): Result<Boolean> {
        return try {
            if (userId == null) throw Exception("User not logged in")

            val newDocRef = db.collection("users").document(userId)
                .collection("cases").document()


            val caseWithId = caseFile.copy(id = newDocRef.id)
            newDocRef.set(caseWithId).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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