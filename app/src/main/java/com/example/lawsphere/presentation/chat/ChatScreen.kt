package com.example.lawsphere.presentation.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawsphere.domain.model.ChatMessage
import com.example.lawsphere.presentation.scanner.CameraScreen // Ensure this file exists
import dev.jeziellago.compose.markdowntext.MarkdownText

val GlassDark = Color(0xFF121212)
val GlassSurface = Color(0xFF1E1E1E).copy(alpha = 0.7f)
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
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showCamera) {
        CameraScreen(
            onTextRecognized = { scannedText ->
                inputText = "Analyze this document context:\n$scannedText"
                showCamera = false
            },
            onClose = { showCamera = false }
        )
    } else {
        Scaffold(
            containerColor = GlassDark,
            topBar = { GlassTopBar() },
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
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }
                    if (isLoading) {
                        item {
                            Text(
                                "LawSphere is researching...",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                            )
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
    val align = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) AccentGold else GlassSurface
    val textColor = if (isUser) Color.Black else Color.White

    val bubbleShape = if (isUser)
        RoundedCornerShape(20.dp, 20.dp, 0.dp, 20.dp)
    else
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(bgColor, bubbleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), bubbleShape)
                .padding(16.dp)
        ) {
            if (isUser) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                MarkdownText(
                    markdown = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 🟢 REMOVED THE SOURCE LIST HERE for cleaner UI
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
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)
            if (!spokenText.isNullOrBlank()) {
                onValueChange(spokenText)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCameraClick,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(contentColor = AccentGold)
        ) {
            Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Doc")
        }

        Spacer(modifier = Modifier.width(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask or Scan...", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentGold,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (value.isBlank()) {
            IconButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your legal query...")
                    }
                    try {
                        speechLauncher.launch(intent)
                    } catch (e: Exception) {}
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = GlassSurface,
                    contentColor = AccentGold
                )
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Speak")
            }
        } else {
            IconButton(
                onClick = onSend,
                enabled = enabled,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = AccentGold,
                    disabledContainerColor = Color.Gray.copy(0.5f)
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
            }
        }
    }
}

@Composable
fun GlassTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(0.9f), Color.Transparent)
                )
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LawSphere AI",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}