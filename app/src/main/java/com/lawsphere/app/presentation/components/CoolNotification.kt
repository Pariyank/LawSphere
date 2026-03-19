package com.lawsphere.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lawsphere.app.core.notifications.*
import com.lawsphere.app.presentation.chat.AccentGold
import kotlinx.coroutines.delay

@Composable
fun CoolNotificationPopup() {
    val notification by GlobalNotificationManager.notification.collectAsState()

    LaunchedEffect(notification) {
        if (notification != null) {
            delay(5000)
            GlobalNotificationManager.dismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 500)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 500)
        ) + fadeOut()
    ) {
        notification?.let { data ->
            val color = when (data.type) {
                NotificationType.EMERGENCY -> Color(0xFFFF5252)
                NotificationType.WARNING -> AccentGold
                NotificationType.SUCCESS -> Color(0xFF4CAF50)
                else -> Color.White
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when(data.type) {
                                NotificationType.EMERGENCY -> Icons.Default.GppBad
                                NotificationType.WARNING -> Icons.Default.Warning
                                NotificationType.SUCCESS -> Icons.Default.CheckCircle
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = data.title,
                                color = color,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = data.message,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}