package com.lawsphere.app

import android.content.Context
import android.hardware.*
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lawsphere.app.presentation.auth.LoginScreen
import com.lawsphere.app.presentation.main.MainScreen
import com.lawsphere.app.presentation.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.sqrt

@AndroidEntryPoint
class MainActivity : FragmentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        setContent {
            val auth = FirebaseAuth.getInstance()

            var showSplash by remember { mutableStateOf(true) }
            var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
            var userRole by remember { mutableStateOf("citizen") }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener {
                    isLoggedIn = it.currentUser != null
                }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    val uid = auth.currentUser?.uid ?: return@LaunchedEffect

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener {
                            userRole = it.getString("role") ?: "citizen"
                        }
                }
            }

            when {
                showSplash -> {
                    SplashScreen { showSplash = false }
                }

                isLoggedIn -> {
                    MainScreen(
                        userRole = userRole,
                        onLogout = {
                            auth.signOut()
                        }
                    )
                }

                else -> {
                    LoginScreen(onLoginSuccess = { })
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt(x * x + y * y + z * z)

        if (acceleration > 15) {
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}