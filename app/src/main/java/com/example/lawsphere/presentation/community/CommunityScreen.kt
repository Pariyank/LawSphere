package com.example.lawsphere.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.example.lawsphere.domain.model.ForumPost
import com.example.lawsphere.domain.model.LawyerProfile
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface

@Composable
fun CommunityScreen(
    userRole: String,
    viewModel: CommunityViewModel = hiltViewModel(),
    onLawyerSelected: (LawyerProfile) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Directory", "Forum")

    val lawyers by viewModel.lawyers.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        Text("Legal Community", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassSurface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) AccentGold else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGold)
            }
        } else {
            when (selectedTab) {
                0 -> DirectoryList(lawyers, onChatClick = onLawyerSelected)
                1 -> ForumList(posts, userRole, viewModel)
            }
        }
    }
}

@Composable
fun DirectoryList(
    lawyers: List<LawyerProfile>,
    onChatClick: (LawyerProfile) -> Unit
) {
    if (lawyers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Lawyers found.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(lawyers) { lawyer ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

                        Box(
                            modifier = Modifier.size(50.dp).background(Color.Gray.copy(0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lawyer.name.take(1), color = AccentGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(lawyer.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(lawyer.specialization, color = AccentGold, fontSize = 12.sp)
                            Text("${lawyer.experience} Yrs Exp • ${lawyer.location}", color = Color.Gray, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { onChatClick(lawyer) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = AccentGold.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "Consult", tint = AccentGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForumList(posts: List<ForumPost>, userRole: String, viewModel: CommunityViewModel) {
    var showAskDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (posts.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No discussions yet.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Be the first to ask a legal question!", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(posts) { post ->
                    ForumCard(post, userRole, viewModel)
                }
            }
        }

        FloatingActionButton(
            onClick = { showAskDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = AccentGold
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ask", tint = Color.Black)
        }
    }

    if (showAskDialog) {
        AskQuestionDialog(onDismiss = { showAskDialog = false }) { title, desc ->
            viewModel.postQuestion(title, desc)
            showAskDialog = false
        }
    }
}

@Composable
fun ForumCard(post: ForumPost, userRole: String, viewModel: CommunityViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var answerText by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(post.title, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(post.description, color = Color.Gray, fontSize = 14.sp, maxLines = if(expanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)

            if (expanded) {
                Divider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 12.dp))

                if (post.answers.isEmpty()) {
                    Text("No answers yet.", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    post.answers.forEach { ans ->
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(ans.lawyerName, color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(ans.content, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }

                if (userRole == "lawyer") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = answerText,
                            onValueChange = { answerText = it },
                            placeholder = { Text("Write professional advice...", color = Color.Gray, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        IconButton(onClick = {
                            if (answerText.isNotBlank()) {
                                viewModel.answerQuestion(post.id, answerText)
                                answerText = ""
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = AccentGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AskQuestionDialog(onDismiss: () -> Unit, onPost: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Ask Community", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, label = { Text("Topic") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it }, label = { Text("Details (Anonymous)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onPost(title, desc) }, colors = ButtonDefaults.buttonColors(containerColor = AccentGold)) {
                Text("Post", color = Color.Black)
            }
        }
    )
}