package com.lawsphere.app.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lawsphere.app.data.utils.AppPreferences
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassSurface
import com.lawsphere.app.presentation.explorer.OfflineLiteScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var userName by remember { mutableStateOf("Loading...") }
    var userRole by remember { mutableStateOf("Loading...") }
    var isHindiMode by remember { mutableStateOf(AppPreferences.isHindiMode) }
    var showOfflineMode by remember { mutableStateOf(false) }

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
        return
    }

    val backgroundGradient = Brush.verticalGradient(
        listOf(Color(0xFF0F0F0F), Color(0xFF1A1A1A), Color(0xFF121212))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(700)) + slideInVertically { it / 2 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(12.dp, CircleShape)
                        .background(GlassSurface, CircleShape)
                        .border(2.dp, AccentGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(50.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(userName ?: "User", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Text(userRole ?: "CITIZEN", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(900)) + slideInVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WifiOff, null, tint = AccentGold)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Offline Lite Mode", color = Color.White, fontWeight = FontWeight.Medium)
                        Text("Access saved legal data without internet", color = Color.Gray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showOfflineMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Open", color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                Text("APP SETTINGS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hindi Responses", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Enable AI replies in Hindi", color = Color.Gray, fontSize = 12.sp)
                    }

                    AnimatedContent(targetState = isHindiMode) { state ->
                        Switch(
                            checked = state,
                            onCheckedChange = {
                                isHindiMode = it
                                AppPreferences.isHindiMode = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AccentGold,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.DarkGray,
                                checkedBorderColor = AccentGold,
                                uncheckedBorderColor = Color.Gray
                            )
                        )
                    }
                }

                Divider(
                    color = Color.White.copy(alpha = 0.08f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
        ) {
            Icon(Icons.Default.Logout, null, tint = Color.Red)

            Spacer(modifier = Modifier.width(8.dp))

            Text("Sign Out", color = Color.Red, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}