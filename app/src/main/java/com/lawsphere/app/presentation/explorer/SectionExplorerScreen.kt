package com.lawsphere.app.presentation.explorer

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionExplorerScreen(
    onOpenRoadmap: () -> Unit,
    onOpenCompare: () -> Unit,
    viewModel: ExplorerViewModel = hiltViewModel()
) {
    val sections by viewModel.sections.collectAsState()
    val availableActs by viewModel.availableActs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    var showPicker by remember { mutableStateOf(false) }
    var selectedAct by remember { mutableStateOf("") }
    var secNum by remember { mutableStateOf("") }

    LaunchedEffect(availableActs) {
        if (availableActs.isNotEmpty() && selectedAct.isEmpty()) {
            selectedAct = availableActs[0]
        }
    }

    val sheetState = rememberModalBottomSheetState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(GlassDark),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, null, tint = AccentGold, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Legal Library", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolCard("Compare", Icons.Default.CompareArrows, AccentGold, onOpenCompare, Modifier.weight(1f))
                ToolCard("Roadmap", Icons.Default.Map, Color(0xFF64B5F6), onOpenRoadmap, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text("SELECT ACT / LAW", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .clickable { if (availableActs.isNotEmpty()) showPicker = true }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (selectedAct.isEmpty()) "Loading Acts..." else selectedAct,
                        color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = AccentGold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = secNum, onValueChange = { secNum = it },
                placeholder = { Text("Section/Article Number", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    viewModel.performExactSearch(selectedAct, secNum)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentGold, unfocusedBorderColor = Color.White.copy(0.1f)
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.performExactSearch(selectedAct, secNum)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && secNum.isNotBlank()
            ) {
                Text("SEARCH DATABASE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGold)
                }
            }
        } else {
            itemsIndexed(sections) { index, section ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, index * 100)) + slideInVertically(tween(500, index * 100))) {
                    SectionCard(section, selectedAct)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1A1A1A)
        ) {
            ActPickerContent(
                allActs = availableActs,
                onSelect = { selectedAct = it; showPicker = false; viewModel.clearResults() }
            )
        }
    }
}

@Composable
fun SectionCard(section: BnsSection, actName: String) {
    var expanded by remember { mutableStateOf(false) }
    val safeSection = (section.section ?: "N/A").ifBlank { "N/A" }
    val description = section.description ?: ""

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(color = AccentGold, shape = RoundedCornerShape(8.dp)) {
                    Text("Sec $safeSection", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = section.title ?: "Section", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = actName, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                }
                Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.Gray)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                if (description.isNotBlank()) {
                    MarkdownText(markdown = description, color = Color.White.copy(0.9f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color.DarkGray, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Punishment: ${section.punishment ?: "Refer to Act"}", color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ActPickerContent(allActs: List<String>, onSelect: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var limit by remember { mutableIntStateOf(20) }
    val filtered = allActs.filter { it.contains(query, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.85f)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Search Act",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it; limit = 20 },
            placeholder = { Text("Type Act Name...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentGold) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = Color.White.copy(0.1f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            itemsIndexed(filtered.take(limit)) { _, act ->
                Text(
                    text = act,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(act) }
                        .padding(16.dp),
                    fontSize = 14.sp
                )
                Divider(color = Color.White.copy(0.05f))
            }

            if (filtered.size > limit) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = { limit += 20 },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = AccentGold.copy(alpha = 0.1f)
                            )
                        ) {
                            Text("See More Acts...", color = AccentGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ToolCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.height(65.dp), colors = CardDefaults.cardColors(containerColor = GlassSurface), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.05f))) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}