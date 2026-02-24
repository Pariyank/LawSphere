package com.example.lawsphere.presentation.explorer

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
import com.example.lawsphere.data.utils.JsonParser
import com.example.lawsphere.domain.model.BnsSection
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun OfflineLiteScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val offlineSections = remember {
        try {
            val json = context.assets.open("offline_critical.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<BnsSection>>() {}.type
            Gson().fromJson<List<BnsSection>>(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Arrest", "Women", "Traffic", "General")

    val filteredList = if (selectedCategory == "All") offlineSections else offlineSections.filter { it.category == selectedCategory }

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
                    Text("Offline Lite Mode", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                }
                Text("Critical laws available without internet", color = Color.Gray, fontSize = 12.sp)
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
            items(filteredList) { section ->
                OfflineCard(section)
            }
        }
    }
}

@Composable
fun OfflineCard(section: BnsSection) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = section.section,
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = section.category.uppercase(),
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = section.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = section.description,
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
                    text = "Penalty: ${section.punishment}",
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}