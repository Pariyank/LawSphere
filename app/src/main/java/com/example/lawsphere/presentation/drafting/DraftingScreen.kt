package com.example.lawsphere.presentation.drafting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lawsphere.data.utils.PdfGenerator
import com.example.lawsphere.domain.model.DraftInput
import com.example.lawsphere.domain.model.DraftingTemplate
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DraftingScreen() {
    var selectedTemplate by remember { mutableStateOf<DraftingTemplate?>(null) }

    if (selectedTemplate == null) {
        TemplateSelectionList(onSelect = { selectedTemplate = it })
    } else {
        DraftingForm(
            template = selectedTemplate!!,
            onBack = { selectedTemplate = null }
        )
    }
}

@Composable
fun TemplateSelectionList(onSelect: (DraftingTemplate) -> Unit) {
    val templates = listOf(
        DraftingTemplate.FIR,
        DraftingTemplate.Bail,
        DraftingTemplate.Notice
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {
        Text(
            "Legal Drafting Tools",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Select a document to generate",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(templates) { template ->
                TemplateCard(template, onSelect)
            }
        }
    }
}

@Composable
fun TemplateCard(template: DraftingTemplate, onClick: (DraftingTemplate) -> Unit) {
    val icon = when (template) {
        DraftingTemplate.FIR -> Icons.Default.Assignment
        DraftingTemplate.Bail -> Icons.Default.Gavel
        DraftingTemplate.Notice -> Icons.Default.Mail
        else -> Icons.Default.Assignment
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .height(140.dp)
            .clickable { onClick(template) }
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(template.title, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DraftingForm(template: DraftingTemplate, onBack: () -> Unit) {
    val context = LocalContext.current
    val currentDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())

    var senderName by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") } // or Station Name
    var details by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(template.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color.Gray.copy(0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InputLabel("Your Name / Complainant")
                CustomTextField(senderName) { senderName = it }
            }

            item {
                val label = if (template == DraftingTemplate.FIR) "Police Station Name" else "Recipient / Court Name"
                InputLabel(label)
                CustomTextField(recipientName) { recipientName = it }
            }

            item {
                val label = if (template == DraftingTemplate.FIR) "Incident Details" else "Case Details / Notice Content"
                InputLabel(label)
                CustomTextField(details, isMultiLine = true) { details = it }
            }
        }

        Button(
            onClick = {
                val html = generateLegalHtml(template, senderName, recipientName, currentDate, details)
                PdfGenerator.generatePdf(context, "${template.title}_$currentDate", html)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            enabled = senderName.isNotBlank() && recipientName.isNotBlank() && details.isNotBlank()
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate PDF", color = Color.Black)
        }
    }
}

@Composable
fun InputLabel(text: String) {
    Text(text, color = AccentGold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
}

@Composable
fun CustomTextField(value: String, isMultiLine: Boolean = false, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(if (isMultiLine) 150.dp else 60.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentGold,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = AccentGold
        ),
        maxLines = if (isMultiLine) 10 else 1
    )
}

fun generateLegalHtml(
    template: DraftingTemplate,
    sender: String,
    recipient: String,
    date: String,
    details: String
): String {
    return when (template) {
        DraftingTemplate.FIR -> """
            <h1>First Information Report (FIR)</h1>
            <p><b>Date:</b> $date</p>
            <p><b>To, The Station House Officer (SHO),</b><br>$recipient</p>
            <h2>Subject: Complaint regarding incident.</h2>
            <div class='content'>
                <p>Respected Sir/Madam,</p>
                <p>I, <b>$sender</b>, wish to report an incident that took place as described below:</p>
                <p>$details</p>
                <p>I request you to kindly register an FIR and take necessary legal action against the culprits.</p>
            </div>
            <div class='signature'>
                <p>Sincerely,</p>
                <p>$sender</p>
            </div>
        """
        DraftingTemplate.Bail -> """
            <h1>Application for Bail</h1>
            <p><b>In The Court Of:</b> $recipient</p>
            <p><b>Date:</b> $date</p>
            <h2>In the matter of: $sender (Applicant)</h2>
            <div class='content'>
                <p>Most Respectfully Sheweth:</p>
                <p>1. That the applicant is innocent and has been falsely implicated in the case.</p>
                <p>2. Details of the case/allegation: $details</p>
                <p>3. That the applicant undertakes to cooperate with the investigation and will attend the court hearings regularly.</p>
                <p>It is therefore prayed that this Hon'ble Court may be pleased to grant bail to the applicant.</p>
            </div>
            <div class='signature'>
                <p>Counsel for Applicant</p>
            </div>
        """
        DraftingTemplate.Notice -> """
            <h1>Legal Notice</h1>
            <p><b>Date:</b> $date</p>
            <p><b>From:</b> Advocate on behalf of $sender</p>
            <p><b>To:</b> $recipient</p>
            <h2>Subject: Legal Notice</h2>
            <div class='content'>
                <p>Sir/Madam,</p>
                <p>Under instructions from my client <b>$sender</b>, I hereby serve you this legal notice:</p>
                <p>$details</p>
                <p>You are hereby called upon to comply with the demands of my client within 15 days of receipt of this notice, failing which appropriate legal action will be initiated against you.</p>
            </div>
            <div class='signature'>
                <p>Advocate Signature</p>
            </div>
        """
        else -> ""
    }
}