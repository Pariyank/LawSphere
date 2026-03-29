package com.lawsphere.app.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lawsphere.app.domain.model.InboxItem
import com.lawsphere.app.domain.model.LawyerProfile
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface

@Composable
fun CommunityScreen(
    userRole: String,
    viewModel: CommunityViewModel = hiltViewModel(),
    onChatClick: (String, String) -> Unit
) {
    val isLawyer = userRole.equals("lawyer", ignoreCase = true)
    val lawyers by viewModel.lawyers.collectAsState()
    val inbox by viewModel.inbox.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showCitizenInbox by remember { mutableStateOf(false) }

    LaunchedEffect(userRole) {
        viewModel.refreshData(userRole)
    }

    Column(modifier = Modifier.fillMaxSize().background(GlassDark).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLawyer) "Your Inbox" else if (showCitizenInbox) "My Consultations" else "Find a Lawyer",
                    color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isLawyer) "Manage your clients" else "Expert legal guidance",
                    color = Color.Gray, fontSize = 13.sp
                )
            }

            if (!isLawyer) {
                IconButton(
                    onClick = { showCitizenInbox = !showCitizenInbox },
                    modifier = Modifier.background(if(showCitizenInbox) AccentGold else GlassSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = if (showCitizenInbox) Icons.Default.PersonSearch else Icons.Default.AllInbox,
                        contentDescription = null,
                        tint = if (showCitizenInbox) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading && (inbox.isEmpty() && lawyers.isEmpty())) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = AccentGold)
            }
        } else {
            if (isLawyer) {
                InboxList(inbox, onChatClick)
            } else {
                if (showCitizenInbox) InboxList(inbox, onChatClick)
                else DirectoryList(lawyers, onChatClick)
            }
        }
    }
}

@Composable
fun InboxList(inbox: List<InboxItem>, onChatClick: (String, String) -> Unit) {
    if (inbox.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("No active chats found.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(inbox) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onChatClick(item.otherUserId, item.otherUserName) }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(45.dp).background(Color.Gray.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(item.otherUserName.take(1), color = AccentGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.otherUserName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(item.lastMessage, color = Color.Gray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun DirectoryList(lawyers: List<LawyerProfile>, onChatClick: (String, String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lawyers) { lawyer ->
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).background(AccentGold.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(lawyer.name.take(1), color = AccentGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lawyer.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(lawyer.specialization, color = AccentGold, fontSize = 12.sp)
                        Text("${lawyer.experience} Yrs Exp • ${lawyer.location}", color = Color.Gray, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = { onChatClick(lawyer.uid, lawyer.name) },
                        modifier = Modifier.background(AccentGold, RoundedCornerShape(12.dp)).size(40.dp)
                    ) {
                        Icon(Icons.Default.ChatBubble, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}