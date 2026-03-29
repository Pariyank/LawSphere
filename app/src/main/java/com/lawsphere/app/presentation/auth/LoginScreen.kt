package com.lawsphere.app.presentation.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.GlassDark

@OptIn(ExperimentalAnimationApi::class)
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

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F0F0F), Color(0xFF1A1A1A), Color(0xFF121212))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(20.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = if (isLoginMode) "Welcome Back" else "Create Account",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Access your legal workspace",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RoleChip("Citizen", selectedRole == "citizen") { selectedRole = "citizen" }
                        RoleChip("Lawyer", selectedRole == "lawyer") { selectedRole = "lawyer" }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(
                        visible = !isLoginMode,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            ModernTextField(name, "Full Name") { name = it }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    ModernTextField(email, "Email") { email = it }

                    Spacer(modifier = Modifier.height(12.dp))

                    ModernTextField(
                        value = password,
                        label = "Password",
                        isPassword = true,
                        onValueChange = { password = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (selectedRole == null) {
                                Toast.makeText(context, "Select role first", Toast.LENGTH_SHORT).show()
                            } else {
                                if (isLoginMode) {
                                    viewModel.login(email, password, selectedRole!!)
                                } else {
                                    viewModel.signup(email, password, name, selectedRole!!)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                        } else {
                            Text(
                                text = if (isLoginMode) "Login" else "Sign Up",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            if (selectedRole == null) {
                                Toast.makeText(context, "Select role first", Toast.LENGTH_SHORT).show()
                            } else {
                                googleSignInLauncher.launch(viewModel.getGoogleLoginIntent())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Continue with Google", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isLoginMode) "Don't have an account? Sign Up"
                        else "Already have an account? Login",
                        color = AccentGold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { isLoginMode = !isLoginMode }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    label: String,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = AccentGold,
            unfocusedBorderColor = Color.Gray,
            cursorColor = AccentGold
        )
    )
}

@Composable
fun RoleChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) AccentGold else Color(0xFF2A2A2A),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        tonalElevation = if (isSelected) 6.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}