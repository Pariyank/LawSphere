package com.example.lawsphere.presentation.explorer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface

data class CareerStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun RoadmapScreen(onBack: () -> Unit = {}) { // Added onBack callback
    val steps = listOf(
        CareerStep(
            "1. CLAT / Entrance Exam",
            "After 12th Grade",
            "Clear Common Law Admission Test (CLAT) or AILET to get into top NLUs.",
            Icons.Default.School,
            Color(0xFF64B5F6) // Blue
        ),
        CareerStep(
            "2. LLB Degree",
            "5 Years (Integrated) or 3 Years",
            "Complete your Bachelor of Legislative Law. Focus on Constitutional Law & IPC.",
            Icons.Default.School,
            Color(0xFF81C784) // Green
        ),
        CareerStep(
            "3. AIBE Exam",
            "Post Graduation",
            "Pass the All India Bar Examination to get your 'Certificate of Practice'.",
            Icons.Default.Star,
            Color(0xFFFFD54F) // Yellow
        ),
        CareerStep(
            "4. Junior Advocate",
            "2-5 Years Experience",
            "Join a District Court or High Court under a senior lawyer. Learn drafting & arguments.",
            Icons.Default.Work,
            Color(0xFFFF8A65) // Orange
        ),
        CareerStep(
            "5. Judiciary (PCS-J)",
            "Optional Path",
            "Crack the State Judicial Services exam to become a Civil Judge/Magistrate.",
            Icons.Default.AccountBalance, // Note: Use available icon
            Color(0xFFBA68C8) // Purple
        ),
        CareerStep(
            "6. Senior Advocate",
            "10+ Years Experience",
            "Designated by the High Court or Supreme Court based on merit and knowledge.",
            Icons.Default.Star,
            AccentGold // Gold
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "Career Roadmap",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Path to becoming a Legal Expert",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timeline List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(steps) { step ->
                TimelineItem(step)
            }
        }
    }
}

@Composable
fun TimelineItem(step: CareerStep) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // 1. The Timeline Line & Dot
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The Dot
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(GlassSurface, CircleShape)
                    .border(2.dp, step.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = step.color,
                    modifier = Modifier.size(12.dp)
                )
            }

            // The Line
            Canvas(modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp)
            ) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
        }

        // 2. The Content Card
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = step.subtitle,
                        color = step.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = step.description,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}