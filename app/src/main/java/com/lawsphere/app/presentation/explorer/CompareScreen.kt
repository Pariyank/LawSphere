package com.lawsphere.app.presentation.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lawsphere.app.presentation.chat.*
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    onBack: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    explorerViewModel: ExplorerViewModel = hiltViewModel()
) {
    val availableActs by explorerViewModel.availableActs.collectAsState()
    val comparisonResult by chatViewModel.comparisonResult.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()

    var actA by remember { mutableStateOf("") }
    var secA by remember { mutableStateOf("") }
    var actB by remember { mutableStateOf("") }
    var secB by remember { mutableStateOf("") }

    var showPickerA by remember { mutableStateOf(false) }
    var showPickerB by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(availableActs) {
        if (availableActs.isNotEmpty()) {
            actA = availableActs[0]
            actB = availableActs[0]
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(GlassDark)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Compare Provisions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {

            CompareInputGroup("FIRST PROVISION", actA, secA, { showPickerA = true }, { secA = it })

            Spacer(modifier = Modifier.height(16.dp))
            Icon(Icons.Default.CompareArrows, null, tint = AccentGold, modifier = Modifier.align(Alignment.CenterHorizontally).size(32.dp))
            Spacer(modifier = Modifier.height(16.dp))

            CompareInputGroup("SECOND PROVISION", actB, secB, { showPickerB = true }, { secB = it })

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    chatViewModel.compareSections(actA, secA, actB, secB)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                shape = RoundedCornerShape(12.dp),
            )
            {
                if (isLoading) CircularProgressIndicator(color = AccentGold, modifier = Modifier.size(24.dp))
                else Text("GENERATE COMPARISON", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!comparisonResult.isNullOrEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ANALYSIS RESULT", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        MarkdownText(markdown = comparisonResult!!, color = Color.White)
                    }
                }
            }
        }
    }

    if (showPickerA) {
        ModalBottomSheet(onDismissRequest = { showPickerA = false }, containerColor = Color(0xFF1A1A1A)) {
            ActPickerContent(allActs = availableActs, onSelect = { actA = it; showPickerA = false })
        }
    }

    if (showPickerB) {
        ModalBottomSheet(onDismissRequest = { showPickerB = false }, containerColor = Color(0xFF1A1A1A)) {
            ActPickerContent(allActs = availableActs, onSelect = { actB = it; showPickerB = false })
        }
    }
}

@Composable
fun CompareInputGroup(label: String, selectedAct: String, secValue: String, onActClick: () -> Unit, onSecChange: (String) -> Unit) {
    Column {
        Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth().background(GlassSurface, RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                .clickable { onActClick() }.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(selectedAct.ifEmpty { "Select Act" }, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null, tint = AccentGold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = secValue, onValueChange = onSecChange,
            placeholder = { Text("Sec/Art No.", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentGold)
        )
    }
}