package com.example.lawsphere.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawsphere.domain.model.CaseFile
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark
import com.example.lawsphere.presentation.chat.GlassSurface
import com.example.lawsphere.presentation.drafting.CustomTextField
import com.example.lawsphere.presentation.drafting.InputLabel

@Composable
fun CaseDashboardScreen(viewModel: CaseDashboardViewModel = hiltViewModel()) {
    val cases by viewModel.cases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Case Dashboard",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Manage your clients & hearings",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = AccentGold)
            } else if (cases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active cases. Add one +", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(cases) { caseFile ->
                        CaseCard(caseFile, onDelete = { viewModel.deleteCase(caseFile.id) })
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = AccentGold,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Case")
        }

        if (showAddDialog) {
            AddCaseDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { client, number, court, date, notes ->
                    viewModel.addCase(client, number, court, date, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun CaseCard(caseFile: CaseFile, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(caseFile.clientName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(0.7f))
                }
            }

            Divider(color = Color.Gray.copy(0.3f), modifier = Modifier.padding(vertical = 8.dp))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Case No:", color = Color.Gray, fontSize = 12.sp)
                    Text(caseFile.caseNumber, color = Color.White, fontSize = 14.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Court:", color = Color.Gray, fontSize = 12.sp)
                    Text(caseFile.courtName, color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Next Hearing: ${caseFile.nextHearingDate}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if(caseFile.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Note: ${caseFile.notes}", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
fun AddCaseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String) -> Unit
) {
    var client by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var court by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("New Case Details", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = client, onValueChange = { client = it }, label = { Text("Client Name") })
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Case Number") })
                OutlinedTextField(value = court, onValueChange = { court = it }, label = { Text("Court Name") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Next Hearing (DD/MM/YYYY)") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(client, number, court, date, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
            ) {
                Text("Save Case", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}