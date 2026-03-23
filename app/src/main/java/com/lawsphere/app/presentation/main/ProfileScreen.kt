package com.lawsphere.app.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lawsphere.app.core.sensors.CourtModeHandler
import com.lawsphere.app.data.utils.AppPreferences
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface
import com.lawsphere.app.presentation.explorer.OfflineLiteScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var userName by remember { mutableStateOf("Loading...") }
    var userRole by remember { mutableStateOf("Loading...") }
    var isHindiMode by remember { mutableStateOf(AppPreferences.isHindiMode) }
    var showOfflineMode by remember { mutableStateOf(false) }

    var isCourtModeActive by remember { mutableStateOf(false) }

    val courtHandler = remember { CourtModeHandler(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            courtHandler.getCurrentLocation { location ->
                if (location != null) {
                    Toast.makeText(context, "🏛️ Near Court: Switching to Court Etiquette Mode", Toast.LENGTH_LONG).show()
                    isCourtModeActive = true
                }
            }
        } else {
            Toast.makeText(context, "Permission Denied. Cannot detect location.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: "User"
                    userRole = doc.getString("role")?.uppercase() ?: "CITIZEN"
                }
        }
    }

    if (showOfflineMode) {
        OfflineLiteScreen(onBack = { showOfflineMode = false })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassDark)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = isCourtModeActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)), // Court Green
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Courtroom Etiquette Guide", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { isCourtModeActive = false }) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        EtiquetteItem("• Switch off mobile phones inside the courtroom.")
                        EtiquetteItem("• Maintain a professional dress code.")
                        EtiquetteItem("• No photography or recording is allowed.")
                    }
                }
            }

            Box(
                modifier = Modifier.size(100.dp).background(GlassSurface, CircleShape).border(2.dp, AccentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(50.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(userName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(userRole, color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showOfflineMode = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.WifiOff, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Offline Lite Mode", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APP SETTINGS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hindi Responses", color = Color.White)
                        Switch(
                            checked = isHindiMode,
                            onCheckedChange = {
                                isHindiMode = it
                                AppPreferences.isHindiMode = it
                            },
                            colors = SwitchDefaults.colors(AccentGold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    OutlinedButton(
                        onClick = {
                            val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

                            if (fineLoc == PackageManager.PERMISSION_GRANTED && coarseLoc == PackageManager.PERMISSION_GRANTED) {
                                courtHandler.getCurrentLocation {
                                    Toast.makeText(context, "🏛️ Near Court: Switching to Court Etiquette Mode", Toast.LENGTH_LONG).show()
                                    isCourtModeActive = true
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.5f))
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test GPS (Scan Location)", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.5f))
            ) {
                Icon(Icons.Default.Logout, null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun EtiquetteItem(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}