package com.example.lawsphere.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Login Function
    suspend fun login(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Login Successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Signup Function with Role
    suspend fun signup(email: String, pass: String, name: String, role: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("User creation failed")

            // Save User Data to Firestore
            val userMap = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to role // "lawyer" or "citizen"
            )
            db.collection("users").document(uid).set(userMap).await()

            Result.success("Signup Successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getUserRole(onResult: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "citizen"
                onResult(role)
            }
    }

    fun logout() {
        auth.signOut()
    }
}