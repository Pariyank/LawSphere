package com.example.lawsphere.presentation.awareness

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface

data class GuideTopic(
    val title: String,
    val icon: ImageVector,
    val shortDesc: String,
    val steps: List<String>
)

@Composable
fun CitizenGuideScreen(
    onOpenMap: () -> Unit
) {
    val topics = listOf(
        GuideTopic(
            "Arrested by Police?",
            Icons.Default.LocalPolice,
            "Know your rights immediately upon arrest.",
            listOf(
                "1. Ask for the grounds/reason of arrest (Section 50 CrPC/BNSS).",
                "2. You have the right to remain silent to avoid self-incrimination.",
                "3. Demand to contact a lawyer or a family member immediately.",
                "4. Police CANNOT hold you for more than 24 hours without a Magistrate's order.",
                "5. If you are a woman, you cannot be arrested after sunset and before sunrise."
            )
        ),
        GuideTopic(
            "Police Refuse FIR?",
            Icons.Default.Warning,
            "Steps to take if the station officer denies you.",
            listOf(
                "1. Note down the name and designation of the officer refusing.",
                "2. Send the substance of information in writing by post to the Superintendent of Police (SP).",
                "3. File an online complaint on your state's police portal.",
                "4. Approach the Magistrate under Section 156(3) BNSS/CrPC."
            )
        ),
        GuideTopic(
            "Domestic Violence?",
            Icons.Default.Woman,
            "Immediate protection for women.",
            listOf(
                "1. Dial 100 (Police) or 1091 (Women Helpline).",
                "2. File a 'Domestic Incident Report' (DIR) with the Protection Officer.",
                "3. You can approach the Magistrate directly for Protection Orders.",
                "4. You are entitled to free legal aid."
            )
        ),
        GuideTopic(
            "Cyber Crime / Fraud?",
            Icons.Default.Computer,
            "Online banking fraud or harassment.",
            listOf(
                "1. Immediately call 1930 (National Cyber Crime Helpline).",
                "2. Register a complaint at cybercrime.gov.in.",
                "3. Take screenshots of chats/transactions as evidence.",
                "4. Contact your bank to freeze the account immediately."
            )
        ),
        GuideTopic(
            "Road Accident?",
            Icons.Default.CarCrash,
            "Good Samaritan laws protect you.",
            listOf(
                "1. Do not fear police harassment; Good Samaritans are protected by law.",
                "2. Take the victim to the nearest hospital immediately.",
                "3. Note down the vehicle number of the offender.",
                "4. Call 100/108 for help."
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Legal Awareness",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Practical guidance for every Indian citizen.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🟢 NEW BUTTON: To Open Maps
        Button(
            onClick = onOpenMap, // Triggers the callback
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Red for Emergency feel
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.NearMe, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Find Police / Courts Nearby", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(topics) { topic ->
                GuideCard(topic)
            }
        }
    }
}

@Composable
fun GuideCard(topic: GuideTopic) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(topic.icon, contentDescription = null, tint = AccentGold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = topic.shortDesc,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = if (expanded) 2 else 1
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.White.copy(0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "STEPS TO TAKE:",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    topic.steps.forEach { step ->
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .background(Color.Gray, RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = step,
                                color = Color.White.copy(0.9f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}