package com.example.lawsphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.lawsphere.presentation.auth.LoginScreen
import com.example.lawsphere.presentation.main.MainScreen
import com.example.lawsphere.presentation.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val auth = FirebaseAuth.getInstance()

            var showSplash by remember { mutableStateOf(true) }
            var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
            var userRole by remember { mutableStateOf("citizen") }

            // ✅ Listen to Firebase auth state changes
            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    isLoggedIn = firebaseAuth.currentUser != null
                }

                auth.addAuthStateListener(listener)

                onDispose {
                    auth.removeAuthStateListener(listener)
                }
            }

            // ✅ Fetch role when user logs in
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                userRole = document.getString("role") ?: "citizen"
                            }
                            .addOnFailureListener {
                                userRole = "citizen"
                            }
                    }
                }
            }

            // ✅ UI Logic
            if (showSplash) {

                SplashScreen(
                    onSplashFinished = { showSplash = false }
                )

            } else {

                if (isLoggedIn) {

                    MainScreen(
                        userRole = userRole,
                        onLogout = {
                            // ✅ Just sign out
                            auth.signOut()
                        }
                    )

                } else {

                    LoginScreen(
                        onLoginSuccess = {

                        }
                    )
                }
            }
        }
    }
}
