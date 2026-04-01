package com.lawsphere.app.presentation.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lawsphere.app.domain.model.BnsSection
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lawsphere.app.data.api.LawApi

@Composable
fun OfflineLiteScreen(
    onBack: () -> Unit,
    api: LawApi
) {
    val context = LocalContext.current

    var offlineSections by remember {
        mutableStateOf(
            try {
                val json = context.assets.open("offline_critical.json")
                    .bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<BnsSection>>() {}.type
                Gson().fromJson<List<BnsSection>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        )
    }

    LaunchedEffect(Unit) {
        try {
            val latestData = api.getRemoteOfflineData()
            if (latestData.isNotEmpty()) {
                offlineSections = latestData
            }
        } catch (e: Exception) {

        }
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Arrest", "Women", "Traffic", "General")

    val filteredList = if (selectedCategory == "All") {
        offlineSections
    } else {
        offlineSections.filter { section ->
            section.category == selectedCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Offline Lite Mode",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    "Critical laws available without internet",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    color = if (isSelected) AccentGold else GlassSurface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredList) { item ->
                OfflineCard(item)
            }
        }
    }
}

@Composable
fun OfflineCard(section: BnsSection) {
    val safeSectionNum = section.section ?: "N/A"
    val safeTitle = section.title ?: "Unknown Title"
    val safeDesc = section.description ?: "No description available."
    val safePunish = section.punishment ?: "Refer to Act"
    val safeCat = (section.category ?: "General").uppercase()

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = AccentGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = safeSectionNum,
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = safeCat,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = safeTitle,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = safeDesc,
                color = Color.White.copy(0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Surface(
                color = Color.Red.copy(0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Penalty: $safePunish",
                    color = Color(0xFFFF8A8A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}