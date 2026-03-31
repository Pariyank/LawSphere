package com.lawsphere.app.data.repository

import android.content.Context
import android.content.Intent
import com.lawsphere.app.R
import com.google.android.gms.auth.api.signin.*
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

    suspend fun login(email: String, pass: String, selectedRole: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Authentication failed")

            val doc = db.collection("users").document(uid).get().await()
            val storedRole = doc.getString("role")

            if (storedRole != null && !storedRole.equals(selectedRole, true)) {
                auth.signOut()
                return Result.failure(
                    Exception("Account registered as ${storedRole.uppercase()}. Select correct role.")
                )
            }

            Result.success("Login Successful")

        } catch (e: Exception) {
            auth.signOut()
            Result.failure(e)
        }
    }

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

    fun getGoogleSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.web_client_id))
            .build()

        val client = GoogleSignIn.getClient(context, gso)

        client.signOut()

        return client.signInIntent
    }

    suspend fun signInWithGoogle(intent: Intent, selectedRole: String): Result<String> {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
            val idToken = account.idToken ?: throw Exception("Google ID Token missing")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Auth failed")

            val docRef = db.collection("users").document(user.uid)
            val doc = docRef.get().await()

            if (!doc.exists()) {
                val newUser = hashMapOf(
                    "uid" to user.uid,
                    "name" to (user.displayName ?: "User"),
                    "email" to (user.email ?: ""),
                    "role" to selectedRole
                )
                docRef.set(newUser).await()
                Result.success("Account created")
            } else {
                val storedRole = doc.getString("role")
                if (storedRole != null && !storedRole.equals(selectedRole, true)) {
                    auth.signOut()
                    return Result.failure(
                        Exception("Account registered as ${storedRole.uppercase()}")
                    )
                }
                Result.success("Welcome back")
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.web_client_id))
            .build()

        val client = GoogleSignIn.getClient(context, gso)

        client.signOut()
        client.revokeAccess()
    }
}