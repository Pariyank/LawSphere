package com.example.lawsphere.presentation.explorer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    onOpenCompare: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val allSections = remember { JsonParser.loadBnsSections(context) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "BNS", "Women", "Traffic", "National")

    val filteredSections = remember(searchQuery, selectedCategory, allSections) {
        var list = allSections

        if (selectedCategory != "All") {
            list = list.filter { section ->
                when (selectedCategory) {
                    "BNS" -> section.section.startsWith("BNS")

                    "Women" -> {
                        val text = (section.title + section.description).lowercase()
                        text.contains("rape") || text.contains("sexual") ||
                                text.contains("woman") || text.contains("dowry") ||
                                text.contains("stalking") || text.contains("modesty")
                    }

                    "Traffic" -> section.section.contains("MV") || section.title.contains("Driving")

                    "National" -> {
                        val text = (section.title + section.description).lowercase()
                        text.contains("terrorist") || text.contains("sovereignty") ||
                                text.contains("organised crime") || text.contains("lynching")
                    }

                    else -> true
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.section.contains(searchQuery, ignoreCase = true) ||
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        Text(
            "Legal Library",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Explore Acts, Codes & Tools",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                title = "Compare Laws",
                icon = Icons.Default.CompareArrows,
                color = AccentGold,
                onClick = onOpenCompare,
                modifier = Modifier.weight(1f)
            )

            ToolCard(
                title = "Career Path",
                icon = Icons.Default.School,
                color = Color(0xFF64B5F6),
                onClick = onOpenRoadmap,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search sections (e.g. 302)...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentGold,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = GlassSurface,
                        labelColor = Color.White,
                        selectedContainerColor = AccentGold,
                        selectedLabelColor = Color.Black
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.Gray.copy(0.5f),
                        selectedBorderColor = AccentGold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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

@Composable
fun ToolCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
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
                        text = "Sec ${section.section.replace("BNS", "").trim()}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = section.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(text = "Chapter ${section.chapter}", color = Color.Gray, fontSize = 12.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = Color.Gray
                )
            }

            if (expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.White.copy(0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("DESCRIPTION", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(section.description, color = Color.White.copy(0.9f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LegalTag("Cognizable", section.cognizable.ifEmpty { "N/A" })
                        LegalTag("Bailable", section.bailable.ifEmpty { "N/A" })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(color = Color.Red.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("PUNISHMENT", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(section.punishment, color = Color.White, fontSize = 14.sp)
                        }
                    }

                    if (section.cases.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("LANDMARK CASES", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        section.cases.forEach { caseName ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(caseName, color = Color.White.copy(0.8f), fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegalTag(label: String, value: String) {
    val color = if (value.equals("Yes", true)) Color(0xFF4CAF50) else if (value.equals("No", true)) Color(0xFFFF5252) else Color.Gray
    Column {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}