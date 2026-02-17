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
                "1. Ask for the grounds of arrest.",
                "2. You have the right to remain silent.",
                "3. Contact a lawyer or family member.",
                "4. Cannot be held more than 24 hours without court order.",
                "5. Women cannot be arrested after sunset (exceptions apply)."
            )
        ),
        GuideTopic(
            "Police Refuse FIR?",
            Icons.Default.Warning,
            "Steps to take if FIR is denied.",
            listOf(
                "1. Note officer name & designation.",
                "2. Write to Superintendent of Police.",
                "3. File online complaint.",
                "4. Approach Magistrate."
            )
        ),
        GuideTopic(
            "Domestic Violence?",
            Icons.Default.Woman,
            "Immediate protection steps.",
            listOf(
                "1. Call 100 or 1091.",
                "2. File Domestic Incident Report.",
                "3. Seek protection order.",
                "4. Free legal aid available."
            )
        ),
        GuideTopic(
            "Cyber Crime / Fraud?",
            Icons.Default.Computer,
            "Online fraud or harassment help.",
            listOf(
                "1. Call 1930 immediately.",
                "2. Register complaint online.",
                "3. Take screenshots as evidence.",
                "4. Inform your bank quickly."
            )
        ),
        GuideTopic(
            "Road Accident?",
            Icons.Default.CarCrash,
            "Good Samaritan protection.",
            listOf(
                "1. Help victim without fear.",
                "2. Take to nearest hospital.",
                "3. Note vehicle number.",
                "4. Call emergency services."
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        Text("Legal Awareness", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Practical guidance for every citizen.", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Button stays here (your intended position)
        Button(
            onClick = onOpenMap,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.8f))
        ) {
            Icon(Icons.Default.LocalPolice, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Find Help Nearby (Maps)", color = Color.White)
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
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentGold.copy(0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(topic.icon, contentDescription = null, tint = AccentGold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(topic.title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(topic.shortDesc, color = Color.Gray, fontSize = 12.sp)
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
                    topic.steps.forEach {
                        Text(it, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
