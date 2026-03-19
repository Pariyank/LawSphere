package com.lawsphere.app.presentation.explorer

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed // 🟢 Changed to itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lawsphere.app.domain.model.BnsSection
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface

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
        if (availableActs.isNotEmpty() && selectedAct.isEmpty()) selectedAct = availableActs[0]
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(GlassDark), contentPadding = PaddingValues(16.dp)) {
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
            Box(modifier = Modifier.fillMaxWidth().background(GlassSurface, RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).clickable { if (availableActs.isNotEmpty()) showPicker = true }.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = if (selectedAct.isEmpty()) "Loading..." else selectedAct, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, null, tint = AccentGold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(value = secNum, onValueChange = { secNum = it }, placeholder = { Text("Section Number", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentGold))
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Button(onClick = { focusManager.clearFocus(); viewModel.performExactSearch(selectedAct, secNum) }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGold), shape = RoundedCornerShape(12.dp), enabled = !isLoading && secNum.isNotBlank()) {
                Text("SEARCH DATABASE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isLoading) {
            item { Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator(color = AccentGold) } }
        } else {
            itemsIndexed(sections) { index, section ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(animationSpec = tween(400, delayMillis = index * 100)) + fadeIn()
                ) {
                    ResultCard(section)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showPicker) {
        ModalBottomSheet(onDismissRequest = { showPicker = false }, containerColor = Color(0xFF1A1A1A)) {
            ActPickerContent(allActs = availableActs, onSelect = { selectedAct = it; showPicker = false; viewModel.clearResults() })
        }
    }
}
@Composable
fun ActPickerContent(allActs: List<String>, onSelect: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf(20) } // 🟢 Initial limit

    val filtered = allActs.filter { it.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxHeight(0.8f).padding(16.dp)) {
        Text("Browse Acts", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; limit = 20 },
            placeholder = { Text("Search Act Name...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentGold) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filtered.take(limit)) { act ->
                Text(
                    text = act,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(act) }
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    fontSize = 14.sp
                )
                Divider(color = Color.White.copy(0.05f))
            }

            if (filtered.size > limit) {
                item {
                    TextButton(
                        onClick = { limit += 20 },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("See More (+20)", color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(section: BnsSection) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AccentGold, shape = RoundedCornerShape(6.dp)) {
                    Text("Sec ${section.section}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(section.title ?: "Section Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("CONTENT", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(section.description ?: "Content unavailable", color = Color.White.copy(0.9f), fontSize = 14.sp, lineHeight = 22.sp)

            if (!section.punishment.isNullOrBlank() && section.punishment != "N/A") {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = Color.Red.copy(0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(section.punishment!!, color = Color(0xFFFF8A8A), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(65.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}