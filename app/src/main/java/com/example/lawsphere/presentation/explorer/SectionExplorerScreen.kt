package com.example.lawsphere.presentation.explorer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lawsphere.data.utils.JsonParser
import com.example.lawsphere.domain.model.BnsSection
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface

@Composable
fun SectionExplorerScreen(
    onOpenRoadmap: () -> Unit,
    onOpenCompare: () -> Unit // ✅ ADDED THIS
) {
    val context = LocalContext.current
    val allSections = remember { JsonParser.loadBnsSections(context) }

    var searchQuery by remember { mutableStateOf("") }

    val filteredSections = remember(searchQuery, allSections) {
        if (searchQuery.isBlank()) {
            allSections
        } else {
            allSections.filter {
                it.section.contains(searchQuery, true) ||
                        it.title.contains(searchQuery, true) ||
                        it.description.contains(searchQuery, true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {

        Text(
            text = "BNS Explorer",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Browse sections, punishments & details",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Buttons Row (YOUR POSITION – not top)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = onOpenCompare,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                border = BorderStroke(1.dp, AccentGold)
            ) {
                Icon(
                    Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compare Laws", color = AccentGold, fontSize = 12.sp)
            }

            Button(
                onClick = onOpenRoadmap,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Career Roadmap", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredSections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No sections found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredSections) { section ->
                    SectionCard(section)
                }
            }
        }
    }
}

@Composable
fun SectionCard(section: BnsSection) {

    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.Top) {

                Surface(
                    color = AccentGold,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                ) {
                    Text(
                        text = "Sec ${section.section}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(section.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Chapter ${section.chapter}", color = Color.Gray, fontSize = 12.sp)
                }

                Icon(
                    imageVector = if (expanded)
                        Icons.Default.ExpandLess
                    else
                        Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            if (expanded) {

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("DESCRIPTION", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(section.description, color = Color.White.copy(0.9f), fontSize = 14.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegalTag("Cognizable", section.cognizable)
                    LegalTag("Bailable", section.bailable)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color.Red.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PUNISHMENT", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(section.punishment, color = Color.White)
                    }
                }

                if (section.cases.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("LANDMARK CASES", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    section.cases.forEach { caseName ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                caseName,
                                color = Color.White.copy(0.8f),
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegalTag(label: String, value: String) {

    val color =
        if (value.equals("Yes", true))
            Color(0xFF4CAF50)
        else
            Color(0xFFFF5252)

    Column {
        Text(label, color = Color.Gray, fontSize = 11.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}
