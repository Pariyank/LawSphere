package com.lawsphere.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.lawsphere.app.data.api.LawApi
import com.lawsphere.app.data.model.ChatRequest
import com.lawsphere.app.data.model.CompareRequest
import com.lawsphere.app.data.repository.ChatRepository
import com.lawsphere.app.data.utils.AppPreferences
import com.lawsphere.app.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val api: LawApi,
    private val chatRepository: ChatRepository,
    val generativeModel: GenerativeModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _comparisonResult = MutableStateFlow<String?>(null)
    val comparisonResult = _comparisonResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var chatJob: Job? = null

    fun loadChatHistory() {
        chatJob?.cancel()
        _messages.value = emptyList()

        chatJob = viewModelScope.launch {
            chatRepository.getChatHistory().collect { history ->
                _messages.value = history
            }
        }
    }

    fun sendMessage(query: String) {
        if (query.isBlank()) return
        val userMsg = ChatMessage(text = query, isUser = true)

        viewModelScope.launch {
            chatRepository.saveMessage(userMsg)
            _isLoading.value = true
            try {
                val lang = if (AppPreferences.isHindiMode) "hindi" else "english"
                val response = withContext(Dispatchers.IO) {
                    api.chatWithLawSphere(ChatRequest(query = query, language = lang))
                }

                val aiMsg = ChatMessage(
                    text = response.formattedAnswer ?: "No answer received.",
                    isUser = false
                )
                chatRepository.saveMessage(aiMsg)

            } catch (e: Exception) {
                e.printStackTrace()
                chatRepository.saveMessage(
                    ChatMessage(
                        text = "Error: ${e.localizedMessage ?: "Unknown Connection Error"}",
                        isUser = false
                    )
                )
            }
            _isLoading.value = false
        }
    }

    fun deleteHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.clearChatHistory()
            _isLoading.value = false
        }
    }

    fun compareSections(act1: String, sec1: String, act2: String, sec2: String) {
        _comparisonResult.value = null
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = withContext(Dispatchers.IO) {
                    api.compareSections(CompareRequest(act1, sec1, act2, sec2))
                }
                _comparisonResult.value = response.formattedAnswer
            } catch (e: Exception) {
                _comparisonResult.value = "Error connecting to server."
            }
            _isLoading.value = false
        }
    }

    fun clearComparison() {
        _comparisonResult.value = null
    }
}