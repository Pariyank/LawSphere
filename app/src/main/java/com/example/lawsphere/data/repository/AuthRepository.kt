package com.example.lawsphere.data.repository

import android.content.Context
import android.content.Intent
import com.example.lawsphere.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 1. Email/Password Login
    suspend fun login(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Login Successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Email/Password Signup
    suspend fun signup(email: String, pass: String, name: String, role: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("User creation failed")

            val userMap = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to role
            )
            db.collection("users").document(uid).set(userMap).await()

            Result.success("Signup Successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. GOOGLE SIGN-IN SETUP
    fun getGoogleSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    // 4. GOOGLE AUTHENTICATION (Step 1)
    // Returns Result<Boolean>: true if user exists in DB, false if new user (needs role)
    suspend fun signInWithGoogle(intent: Intent): Result<Boolean> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.await()
            val idToken = account.idToken ?: throw Exception("Google ID Token missing")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Firebase Auth failed")

            // Check if user exists in Firestore
            val docRef = db.collection("users").document(user.uid)
            val doc = docRef.get().await()

            if (doc.exists()) {
                // User already has a role, proceed to home
                Result.success(true)
            } else {
                // New user (or doc deleted), stop here and ask for role
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. FINALIZE GOOGLE USER (Step 2)
    // Called after user selects role in the Dialog
    suspend fun createGoogleUserFirestore(role: String): Result<String> {
        return try {
            val user = auth.currentUser ?: throw Exception("No authenticated user found")

            val newUser = hashMapOf(
                "uid" to user.uid,
                "name" to (user.displayName ?: "Google User"),
                "email" to (user.email ?: ""),
                "role" to role // 🟢 Save Selected Role
            )

            db.collection("users").document(user.uid).set(newUser).await()
            Result.success("Registration Complete")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 FIXED LOGOUT: Clears Google Session too
    fun logout() {
        auth.signOut()

        // This ensures the "Choose Account" popup appears next time
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso).signOut()
    }
}