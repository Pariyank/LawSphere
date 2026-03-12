package com.lawsphere.app.presentation.explorer

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
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionExplorerScreen(
    onOpenRoadmap: () -> Unit,
    onOpenCompare: () -> Unit,
    viewModel: ExplorerViewModel = hiltViewModel()
) {
    val sections by viewModel.sections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    val indianActs = listOf(
        "Bar Council of India Rule, 1975",
        "Bharatiya Nagarik Suraksha Sanhita, 2023",
        "Bharatiya Nyaya Sanhita, 2023",
        "Bharatiya Sakshya Adhiniyam, 2023",
        "Indian Contract Act, 1872",
        "Insolvency & Bankruptcy Code, 2016",
        "Interest Act, 1978",
        "Representation of People Act, 1951",
        "The Advocates Act, 1961",
        "The Air (Prevention & Control of Pollution) Act, 1981",
        "The Arbitration & Conciliation Act, 1996",
        "The Arms Act, 1959",
        "The Code of Civil Procedure, 1908",
        "The Companies Act, 2013",
        "The Competition Act, 2002",
        "The Consumer Protection Act, 1986",
        "The Consumer Protection Act, 2019",
        "The Copyright Act, 1957",
        "The Designs Act, 2000",
        "The Dissolution of Muslim Marriage Act, 1939",
        "The Divorce Act, 1869",
        "The Employee's Compensation Act, 1923",
        "The Environment Protection Act, 1986",
        "The Factories Act, 1948",
        "The Foreign Exchange Management Act, 1999",
        "The Geographical Indication of Goods Act, 1999",
        "The Guardians & Wards Act, 1890",
        "The Hindu Adoption & Maintenance Act, 1956",
        "The Hindu Marriage Act, 1955",
        "The Hindu Succession Act, 1956",
        "The Indian Christian Marriage Act, 1872",
        "The Industrial Dispute Act, 1947",
        "The Information Technology Act, 2000",
        "The Insurance Act, 1938",
        "The Limitation Act, 1963",
        "The Minimum Wages Act, 1948",
        "The Negotiable Instruments Act, 1881",
        "The Parsi Marriage & Divorce Act, 1936",
        "The Patents Act, 1970",
        "The Payment of Wages Act, 1936",
        "The Protection of Children from Sexual Offences Act, 2012 (POCSO)",
        "The Reserve Bank of India Act, 1934",
        "The Sale of Goods Act, 1930",
        "The SARFAESI Act, 2019",
        "The Special Marriage Act, 1954",
        "The Special Relief Act, 1963",
        "The Trade Union Act, 1926",
        "The Trademark Act, 1999",
        "The Transfer of Property Act, 1882",
        "The Water (Prevention & Control of Pollution) Act, 1974",
        "The Wildlife (Protection) Act, 1972"
    ).sorted()

    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedAct by remember { mutableStateOf("Bharatiya Nyaya Sanhita, 2023") } // Default Act
    var sectionNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(GlassDark).padding(16.dp)) {
        Text("Legal Library", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Search Exact Statements Across 50+ Acts", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToolCard("Compare Laws", Icons.Default.CompareArrows, AccentGold, onOpenCompare, Modifier.weight(1f))
            ToolCard("Career Path", Icons.Default.School, Color(0xFF64B5F6), onOpenRoadmap, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        ExposedDropdownMenuBox(expanded = expandedDropdown, onExpandedChange = { expandedDropdown = !expandedDropdown }) {
            OutlinedTextField(
                value = selectedAct, onValueChange = {}, readOnly = true,
                label = { Text("Select Act / Law", color = Color.Gray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = GlassSurface, unfocusedContainerColor = GlassSurface),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }, modifier = Modifier.background(GlassSurface).heightIn(max = 300.dp)) {
                indianActs.forEach { act ->
                    DropdownMenuItem(text = { Text(act, color = Color.White) }, onClick = { selectedAct = act; expandedDropdown = false; viewModel.clearResults() })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = sectionNumber, onValueChange = { sectionNumber = it },
            label = { Text("Section Number (e.g. 102, 302(1))", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); viewModel.performExactSearch(selectedAct, sectionNumber) }),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp), singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { focusManager.clearFocus(); viewModel.performExactSearch(selectedAct, sectionNumber) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Extract Exact Statement", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentGold) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(sections) { section -> SectionCard(section, selectedAct) }
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
fun SectionCard(section: BnsSection, actName: String) {
    var expanded by remember { mutableStateOf(false) }
    val safeSection = section.section ?: "N/A"
    val safeTitle = section.title ?: "Unknown"
    val safeDescription = section.description ?: "Not Found in Context"
    val safePunishment = section.punishment ?: "N/A"
    val safeCognizable = section.cognizable ?: "N/A"
    val safeBailable = section.bailable ?: "N/A"

    Card(colors = CardDefaults.cardColors(containerColor = GlassSurface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().animateContentSize().border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp)).clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(color = AccentGold, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(end = 12.dp, top = 2.dp)) {
                    Text("Sec $safeSection", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = safeTitle, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(text = actName, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color.Gray)
            }
            if (expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = Color.White.copy(0.1f)); Spacer(modifier = Modifier.height(12.dp))
                    Text("EXACT STATEMENT", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(safeDescription, color = Color.White.copy(0.9f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp, top = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LegalTag("Cognizable", safeCognizable)
                        LegalTag("Bailable", safeBailable)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color.DarkGray, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("PUNISHMENT / PENALTY", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(safePunishment, color = Color.White, fontSize = 14.sp)
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
    Column { Text(label, color = Color.Gray, fontSize = 11.sp); Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
}