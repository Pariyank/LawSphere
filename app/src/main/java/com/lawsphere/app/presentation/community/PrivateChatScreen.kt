package com.lawsphere.app.presentation.community

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lawsphere.app.domain.model.PrivateMessage
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrivateChatScreen(
    otherUserId: String,
    otherUserName: String?,
    onBack: () -> Unit,
    viewModel: PrivateChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var messageToDelete by remember { mutableStateOf<PrivateMessage?>(null) }

    LaunchedEffect(otherUserId) {
        viewModel.loadMessages(otherUserId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messageToDelete != null) {
        val isMyMsg = messageToDelete!!.senderId == viewModel.currentUserId
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Delete Message", color = Color.White) },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                    if (isMyMsg) {
                        TextButton(onClick = { viewModel.deleteForEveryone(otherUserId, messageToDelete!!.id); messageToDelete = null }) {
                            Text("Delete for everyone", color = Color.Red)
                        }
                    }
                    TextButton(onClick = { viewModel.deleteForMe(otherUserId, messageToDelete!!.id); messageToDelete = null }) {
                        Text("Delete for me", color = AccentGold)
                    }
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(GlassDark)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(GlassSurface).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(otherUserName ?: "User", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Private Consultation", color = AccentGold, fontSize = 12.sp)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == viewModel.currentUserId

                LaunchedEffect(msg.id) {
                    if (!isMe) {
                        viewModel.markSeen(otherUserId, msg.id)
                    }
                }

                MessageBubble(
                    message = msg,
                    isMe = isMe,
                    currentUserId = viewModel.currentUserId,
                    onLongPress = { messageToDelete = msg }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(GlassSurface).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText, onValueChange = { inputText = it },
                placeholder = { Text("Type message...") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                viewModel.sendMessage(otherUserId, otherUserName ?: "User", inputText)
                inputText = ""
            }, colors = IconButtonDefaults.iconButtonColors(containerColor = AccentGold)) {
                Icon(Icons.Default.Send, null, tint = Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: PrivateMessage,
    isMe: Boolean,
    currentUserId: String?,
    onLongPress: () -> Unit
) {

    val isDelivered = message.deliveredTo.size > 1
    val isSeen = message.seenBy.size > 1

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    if (isMe)
                        RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp)
                    else
                        RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp)
                )
                .background(if (isMe) AccentGold else Color(0xFF333333))
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(12.dp)
        ) {

            Text(
                message.text ?: "",
                color = if (isMe) Color.Black else Color.White,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {

                val time = java.text.SimpleDateFormat(
                    "hh:mm a",
                    java.util.Locale.getDefault()
                ).format(java.util.Date(message.timestamp))

                Text(
                    time,
                    color = if (isMe) Color.Black.copy(0.6f) else Color.Gray,
                    fontSize = 10.sp
                )

                if (isMe) {
                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = when {
                            isSeen -> "✔✔"
                            isDelivered -> "✔✔"
                            else -> "✔"
                        },
                        fontSize = 12.sp,
                        color = when {
                            isSeen -> Color(0xFF4FC3F7)
                            else -> Color.Gray
                        }
                    )
                }
            }
        }
    }
}