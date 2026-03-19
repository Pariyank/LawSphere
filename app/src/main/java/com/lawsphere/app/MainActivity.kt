package com.lawsphere.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lawsphere.app.core.notifications.GlobalNotificationManager
import com.lawsphere.app.core.notifications.NotificationType
import com.lawsphere.app.core.sensors.CourtModeHandler
import com.lawsphere.app.presentation.auth.LoginScreen
import com.lawsphere.app.presentation.main.MainScreen
import com.lawsphere.app.presentation.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.sqrt

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

    // 🟢 SYLLABUS: Motion Sensor Variables
    private lateinit var sensorManager: SensorManager
    private var acceleration = 0f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🟢 SYLLABUS: Initialize Accelerometer
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        setContent {
            val auth = FirebaseAuth.getInstance()
            var showSplash by remember { mutableStateOf(true) }
            var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
            var userRole by remember { mutableStateOf("citizen") }

            // 🟢 SYLLABUS: Position Sensor (GPS) Check on Launch
            val courtHandler = remember { CourtModeHandler(this@MainActivity) }
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    courtHandler.startRealCheck { isNear, _ ->
                        if (isNear) {
                            GlobalNotificationManager.show(
                                title = "Court Mode Active",
                                message = "Position sensors detected proximity to Court. Rules applied.",
                                type = NotificationType.SUCCESS
                            )
                        }
                    }
                }
            }

            // Firebase Auth Listener
            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    isLoggedIn = firebaseAuth.currentUser != null
                }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }

            // Fetch User Role
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                userRole = document.getString("role") ?: "citizen"
                            }
                    }
                }
            }

            if (showSplash) {
                SplashScreen(onSplashFinished = { showSplash = false })
            } else {
                if (isLoggedIn) {
                    MainScreen(
                        userRole = userRole,
                        onLogout = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                            val googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
                            googleSignInClient.signOut().addOnCompleteListener {
                                auth.signOut()
                                isLoggedIn = false
                            }
                        }
                    )
                } else {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                }
            }
        }
    }

    // 🟢 SYLLABUS: Sensor Event Handling (Shake Detection)
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]; val y = it.values[1]; val z = it.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt(x * x + y * y + z * z)
            val delta = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 15) { // Vigorous shake detected
                GlobalNotificationManager.show(
                    title = "SOS TRIGGERED",
                    message = "Motion sensors detected an emergency shake. Sending location...",
                    type = NotificationType.EMERGENCY
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}