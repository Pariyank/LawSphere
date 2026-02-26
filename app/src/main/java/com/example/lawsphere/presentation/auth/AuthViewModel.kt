package com.example.lawsphere.presentation.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawsphere.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, pass)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login Failed")
            }
        }
    }

    fun signup(email: String, pass: String, name: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signup(email, pass, name, role)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Signup Failed")
            }
        }
    }

    fun getGoogleLoginIntent(): Intent {
        return repository.getGoogleSignInIntent()
    }

    fun handleGoogleSignInResult(intent: Intent) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signInWithGoogle(intent)

            result.onSuccess { isExistingUser ->
                if (isExistingUser) {

                    _authState.value = AuthState.Success
                } else {

                    _authState.value = AuthState.RoleSelectionRequired
                }
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Google Sign-In Failed")
            }
        }
    }

    fun finalizeGoogleLogin(role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.createGoogleUserFirestore(role)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error("Failed to save role")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object RoleSelectionRequired : AuthState() // 🟢 New State
    data class Error(val message: String) : AuthState()
}