package com.lawsphere.app.presentation.drafting

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lawsphere.app.data.utils.FileDownloader
import com.lawsphere.app.domain.model.LegalForm
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface

@Composable
fun BNSSFormsGallery(onBack: () -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val allForms = remember {
        try {
            val json = context.assets.open("bnss_forms.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, List<LegalForm>>>() {}.type
            val data: Map<String, List<LegalForm>> = Gson().fromJson(json, type)
            data["forms"] ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    val filtered = allForms.filter { it.title.contains(searchQuery, true) }

    Column(modifier = Modifier.fillMaxSize().background(GlassDark).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("BNSS Form Library", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            placeholder = { Text("Search 58 official forms...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentGold) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentGold
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(filtered) { index, form ->
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(tween(400, index * 30)) + fadeIn()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GlassSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("FORM ${form.id}", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(form.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(form.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                            }

                            IconButton(
                                onClick = { FileDownloader.downloadPdf(context, form.storage_url, form.title) },
                                modifier = Modifier.background(AccentGold, RoundedCornerShape(12.dp)).size(44.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, null, tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}