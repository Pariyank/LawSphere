package com.example.lawsphere.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawsphere.data.api.LawApi
import com.example.lawsphere.data.model.ChatRequest
import com.example.lawsphere.data.model.CompareRequest
import com.example.lawsphere.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val api: LawApi
) : ViewModel() {

    // 🟢 State 1: For the Main Chat Tab (History)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    // 🟢 State 2: For the Compare Tool (Single Result)
    private val _comparisonResult = MutableStateFlow<String?>(null)
    val comparisonResult = _comparisonResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ================= ASK (Main Chat) =================
    fun sendMessage(query: String) {
        if (query.isBlank()) return

        _messages.value += ChatMessage(text = query, isUser = true)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = withContext(Dispatchers.IO) {
                    api.chatWithLawSphere(ChatRequest(query = query))
                }

                val sourceList = response.retrievedSources?.map {
                    "Source ${it.sourceNumber}"
                } ?: emptyList()

                _messages.value += ChatMessage(
                    text = response.formattedAnswer ?: "No answer received.",
                    isUser = false,
                    sources = sourceList
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value += ChatMessage(
                    text = "Error: ${e.localizedMessage ?: "Unknown Connection Error"}",
                    isUser = false
                )
            }
            _isLoading.value = false
        }
    }

    // ================= COMPARE (Compare Tool) =================
    fun compareSections(section1: String, section2: String) {
        if (section1.isBlank() || section2.isBlank()) return

        // Clear previous result
        _comparisonResult.value = null

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = withContext(Dispatchers.IO) {
                    api.compareSections(CompareRequest(section1, section2))
                }

                // 🟢 Update the specific comparison state, NOT the chat history
                _comparisonResult.value = response.formattedAnswer ?: "Comparison failed."

            } catch (e: Exception) {
                e.printStackTrace()
                _comparisonResult.value = "Comparison Error: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }

    // Helper to clear comparison when leaving screen
    fun clearComparison() {
        _comparisonResult.value = null
    }
}