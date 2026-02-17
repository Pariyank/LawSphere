package com.example.lawsphere.presentation.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.ChatViewModel
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun CompareScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var sectionA by remember { mutableStateOf("") }
    var sectionB by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // 🟢 Observes ONLY the comparison result, ignoring chat history
    val comparisonResult by viewModel.comparisonResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Clear result when entering/leaving to keep it fresh
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearComparison()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Compare Laws", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inputs
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = sectionA,
                onValueChange = { sectionA = it },
                label = { Text("Section A") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold, unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = AccentGold, modifier = Modifier.padding(horizontal = 8.dp))

            OutlinedTextField(
                value = sectionB,
                onValueChange = { sectionB = it },
                label = { Text("Section B") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold, unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Compare Button
        Button(
            onClick = {
                focusManager.clearFocus() // Hide keyboard
                viewModel.compareSections(sectionA, sectionB)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            enabled = !isLoading && sectionA.isNotEmpty() && sectionB.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
            } else {
                Text("Analyze Differences", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🟢 Show Result only if comparisonResult is not null
        if (!comparisonResult.isNullOrEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Comparison Result", color = AccentGold, fontWeight = FontWeight.Bold)
                    Divider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    MarkdownText(
                        markdown = comparisonResult!!,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}