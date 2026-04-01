package com.lawsphere.app.presentation.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lawsphere.app.domain.model.ChatMessage
import com.lawsphere.app.presentation.scanner.CameraScreen
import dev.jeziellago.compose.markdowntext.MarkdownText

val GlassDark = Color(0xFF121212)
val GlassSurface = Color(0xFF2E2E2E)
val AccentGold = Color(0xFFD4AF37)

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadChatHistory()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Clear History?", color = Color.White) },
            text = { Text("This will permanently delete all your chats.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHistory()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showCamera) {
        CameraScreen(
            generativeModel = viewModel.generativeModel,
            onTextRecognized = { scannedText ->
                inputText = "Analyze this document context:\n$scannedText"
                showCamera = false
            },
            onClose = { showCamera = false }
        )
    } else {
        Scaffold(
            containerColor = GlassDark,
            topBar = {
                GlassTopBar(onDeleteClick = { showDeleteDialog = true })
            },
            bottomBar = {
                ChatInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    onCameraClick = { showCamera = true },
                    enabled = !isLoading
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (messages.isEmpty() && !isLoading) {
                    EmptyChatState(
                        onSuggestionClick = { suggestion ->
                            viewModel.sendMessage(suggestion)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(messages) { index, msg ->
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { isVisible = true }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInHorizontally(
                                    initialOffsetX = { if (msg.isUser) it else -it },
                                    animationSpec = tween(500)
                                ) + fadeIn()
                            ) {
                                ChatBubble(msg)
                            }
                        }

                        if (isLoading) {
                            item {
                                val infiniteTransition = rememberInfiniteTransition(label = "load")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.4f, targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha"
                                )
                                Text("Thinking...", color = Color.Gray.copy(alpha = alpha), modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(32.dp)
                        .background(GlassSurface, CircleShape)
                        .border(1.dp, AccentGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Scale, null, tint = AccentGold, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Card(
                modifier = Modifier.weight(1f, fill = false),
                shape = if (isUser)
                    RoundedCornerShape(20.dp, 20.dp, 2.dp, 20.dp)
                else
                    RoundedCornerShape(2.dp, 20.dp, 20.dp, 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) AccentGold else Color(0xFF1E1E1E)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if(isUser) 2.dp else 4.dp),
                border = if(!isUser) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isUser) {
                        Text(
                            text = message.text,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        MarkdownText(
                            markdown = message.text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatState(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "Right to Privacy (Article 21)?",
        "Minimum Wage Rules for workers?",
        "Punishment for POCSO offences?",
        "Procedure for Bail under BNSS?"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Scale, null, tint = AccentGold, modifier = Modifier.size(72.dp).scale(scale))
        Spacer(modifier = Modifier.height(24.dp))
        Text("How can I help?", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Access 840+ Indian Acts instantly.", color = Color.Gray, fontSize = 15.sp, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(40.dp))
        Text("SUGGESTED QUERIES", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))

        suggestions.forEachIndexed { index, suggestion ->
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { isVisible = true }

            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(animationSpec = tween(400, delayMillis = index * 100)) + fadeIn()
            ) {
                OutlinedButton(
                    onClick = { onSuggestionClick(suggestion) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = GlassSurface.copy(alpha = 0.5f), contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(suggestion, fontSize = 14.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onCameraClick: () -> Unit,
    enabled: Boolean
) {
    val speechLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val text = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!text.isNullOrBlank()) onValueChange(text)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(GlassDark).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCameraClick) {
            Icon(Icons.Default.DocumentScanner, "Scan", tint = AccentGold)
        }
        Spacer(modifier = Modifier.width(4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask about any law...", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold, unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                cursorColor = AccentGold, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp), singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))

        AnimatedContent(targetState = value.isBlank(), label = "icon") { isBlank ->
            if (isBlank) {
                IconButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    }
                    try { speechLauncher.launch(intent) } catch (e: Exception) {}
                }) {
                    Icon(Icons.Default.Mic, "Speak", tint = AccentGold)
                }
            } else {
                IconButton(onClick = onSend, enabled = enabled) {
                    Icon(Icons.Default.Send, "Send", tint = AccentGold)
                }
            }
        }
    }
}

@Composable
fun GlassTopBar(onDeleteClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.9f), Color.Transparent)))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("LawSphere AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        IconButton(onClick = onDeleteClick, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(Icons.Default.Delete, "Clear", tint = Color.Gray)
        }
    }
}