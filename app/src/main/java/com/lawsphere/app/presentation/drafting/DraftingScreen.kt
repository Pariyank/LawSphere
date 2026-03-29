package com.lawsphere.app.presentation.drafting

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lawsphere.app.data.utils.FileDownloader
import com.lawsphere.app.data.utils.PdfGenerator
import com.lawsphere.app.domain.model.DraftingTemplate
import com.lawsphere.app.domain.model.LegalForm
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.chat.GlassSurface
import java.text.SimpleDateFormat
import java.util.*

enum class DraftingView {
    DASHBOARD, AI_CUSTOM_DRAFT, BNSS_FORMS_GALLERY
}

@Composable
fun DraftingScreen() {
    var currentView by remember { mutableStateOf(DraftingView.DASHBOARD) }
    var selectedTemplate by remember { mutableStateOf<DraftingTemplate?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = GlassDark) {
        AnimatedContent(
            targetState = currentView,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
            label = "ScreenTransition"
        ) { targetView ->
            when (targetView) {
                DraftingView.DASHBOARD -> {
                    DraftingDashboard(
                        onSelectAI = { template ->
                            selectedTemplate = template
                            currentView = DraftingView.AI_CUSTOM_DRAFT
                        },
                        onSelectGallery = { currentView = DraftingView.BNSS_FORMS_GALLERY }
                    )
                }
                DraftingView.AI_CUSTOM_DRAFT -> {
                    selectedTemplate?.let {
                        DraftingForm(template = it, onBack = { currentView = DraftingView.DASHBOARD })
                    }
                }
                DraftingView.BNSS_FORMS_GALLERY -> {
                    BNSSFormsGalleryContent(onBack = { currentView = DraftingView.DASHBOARD })
                }
            }
        }
    }
}

@Composable
fun DraftingDashboard(onSelectAI: (DraftingTemplate) -> Unit, onSelectGallery: () -> Unit) {
    val aiTemplates = listOf(DraftingTemplate.FIR, DraftingTemplate.Bail, DraftingTemplate.Notice)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Drafting Suite", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Create custom documents or download official forms", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(110.dp).clickable { onSelectGallery() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).background(AccentGold.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesomeMotion, null, tint = AccentGold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("BNSS Form Library", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Download official Second Schedule forms", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("AI QUICK DRAFTS", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(aiTemplates) { template ->
                Card(
                    modifier = Modifier.height(140.dp).clickable { onSelectAI(template) },
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, null, tint = AccentGold, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(template.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun BNSSFormsGalleryContent(onBack: () -> Unit) {
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

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Official BNSS Forms", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            placeholder = { Text("Search form name...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AccentGold) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentGold)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(filtered) { index, form ->
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }

                AnimatedVisibility(visible = isVisible, enter = slideInHorizontally(tween(400, index * 30)) + fadeIn()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GlassSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.05f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("FORM ${form.id}", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(form.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

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

@Composable
fun DraftingForm(template: DraftingTemplate, onBack: () -> Unit) {
    val context = LocalContext.current
    val date = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
    var sender by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text(template.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { DraftingField("Your Full Name", sender) { sender = it } }
            item { DraftingField("Recipient/Court Name", recipient) { recipient = it } }
            item { DraftingField("Details of Incident", details, true) { details = it } }
        }

        Button(
            onClick = {
                val html = "<h1>${template.title}</h1><p>Date: $date</p><p>From: $sender</p><p>To: $recipient</p><p>Details: $details</p>"
                PdfGenerator.generatePdf(context, "Legal_Draft", html)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            shape = RoundedCornerShape(12.dp),
            enabled = sender.isNotBlank() && recipient.isNotBlank() && details.isNotBlank()
        ) {
            Icon(Icons.Default.PictureAsPdf, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GENERATE & EXPORT PDF", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DraftingField(label: String, value: String, isLarge: Boolean = false, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(if(isLarge) 180.dp else 60.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentGold),
            shape = RoundedCornerShape(12.dp)
        )
    }
}