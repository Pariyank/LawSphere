package com.example.lawsphere.presentation.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.GlassDark

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null && selectedRole != null) {

                viewModel.handleGoogleSignInResult(data, selectedRole!!)
            }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassDark),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLoginMode) "Welcome Back" else "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text("Select Your Role:", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RoleChip("Citizen", selectedRole == "citizen") { selectedRole = "citizen" }
                    RoleChip("Lawyer", selectedRole == "lawyer") { selectedRole = "lawyer" }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.Gray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedRole == null) {
                            Toast.makeText(context, "Please select a Role first", Toast.LENGTH_SHORT).show()
                        } else {
                            if (isLoginMode) {

                                viewModel.login(email, password, selectedRole!!)
                            } else {
                                viewModel.signup(email, password, name, selectedRole!!)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isLoginMode) "Login" else "Sign Up",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedRole == null) {
                            Toast.makeText(context, "Please select a Role first", Toast.LENGTH_SHORT).show()
                        } else {
                            val signInIntent = viewModel.getGoogleLoginIntent()
                            googleSignInLauncher.launch(signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login",
                    color = Color.Gray,
                    modifier = Modifier.clickable { isLoginMode = !isLoginMode }
                )
            }
        }
    }
}

@Composable
fun RoleChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) AccentGold else Color.DarkGray,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onClick() },
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = AccentGold, unfocusedBorderColor = Color.Gray,
    cursorColor = AccentGold
)