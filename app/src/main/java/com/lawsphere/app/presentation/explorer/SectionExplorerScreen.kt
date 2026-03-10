package com.lawsphere.app.presentation.explorer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lawsphere.app.domain.model.BnsSection
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface

@Composable
fun SectionExplorerScreen(
    onOpenRoadmap: () -> Unit,
    onOpenCompare: () -> Unit,
    viewModel: ExplorerViewModel = hiltViewModel()
) {
    val sections by viewModel.sections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val focusManager = LocalFocusManager.current

    val categories = listOf("All", "BNS", "Women", "Traffic", "National", "Corporate", "Cyber")

    // Update filter safely via ViewModel
    LaunchedEffect(searchQuery, selectedCategory) {
        if (searchQuery.isEmpty()) {
            viewModel.resetToLocal()
            if (!isLoading) viewModel.filterSections(searchQuery, selectedCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        // Header
        Text("Legal Library", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Explore Acts, Codes & Tools", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Tools Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToolCard("Compare Laws", Icons.Default.CompareArrows, AccentGold, onOpenCompare, Modifier.weight(1f))
            ToolCard("Career Path", Icons.Default.School, Color(0xFF64B5F6), onOpenRoadmap, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search (e.g. 302, Theft)...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.resetToLocal()
                    }) { Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray) }
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                viewModel.performCloudSearch(searchQuery)
            })
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips (Scrollable)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, color = if(isSelected) Color.Black else Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGold,
                        containerColor = GlassSurface
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

        Spacer(modifier = Modifier.height(12.dp))

        // Cloud Search Button
        if (sections.isEmpty() && !isLoading && searchQuery.isNotEmpty()) {
            Button(
                onClick = { viewModel.performCloudSearch(searchQuery) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = AccentGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Server for '$searchQuery'", color = AccentGold)
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGold)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sections) { section ->
                    SectionCard(section)
                }
            }
        }
    }
}

@Composable
fun SectionCard(section: BnsSection) {
    var expanded by remember { mutableStateOf(false) }

    // 🟢 NULL SAFETY PREPARATION
    // We prepare safe strings here to avoid crashing inside the UI
    val rawSection = section.section ?: "N/A"
    val safeSectionTitle = rawSection.replace("BNS", "").trim().take(10) // Limit length
    val safeTitle = section.title ?: "Unknown Title"
    val safeChapter = section.chapter ?: "General"
    val safeDescription = section.description ?: "No description provided."
    val safePunishment = section.punishment ?: "See Act for details."
    val safeCognizable = section.cognizable ?: "-"
    val safeBailable = section.bailable ?: "-"

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
            // Header
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color = AccentGold,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                ) {
                    Text(
                        text = "Sec $safeSectionTitle",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = safeTitle,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Chapter $safeChapter",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            // Expanded Details
            if (expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.White.copy(0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("DESCRIPTION", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Text(
                        text = safeDescription,
                        color = Color.White.copy(0.9f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LegalTag("Cognizable", safeCognizable)
                        LegalTag("Bailable", safeBailable)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(color = Color.Red.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("PUNISHMENT", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = safePunishment,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Case Laws (Safe Iteration)
                    if (!section.cases.isNullOrEmpty()) {
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
fun ToolCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.height(80.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GlassSurface), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(color.copy(alpha = 0.1f), Color.Transparent))))
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(4.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun LegalTag(label: String, value: String) {
    val color = if (value.equals("Yes", true)) Color(0xFF4CAF50) else if (value.equals("No", true)) Color(0xFFFF5252) else Color.Gray
    Column { Text(text = label, color = Color.Gray, fontSize = 11.sp); Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
}