package com.example.lawsphere.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawsphere.data.repository.PrivateChatRepository
import com.example.lawsphere.domain.model.PrivateMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivateChatViewModel @Inject constructor(
    private val repository: PrivateChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<PrivateMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    val currentUserId = repository.currentUserId

    fun loadMessages(otherUserId: String) {
        val roomId = repository.getChatRoomId(otherUserId)
        viewModelScope.launch {
            repository.getMessages(roomId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(otherUserId: String, text: String) {
        if (text.isBlank()) return
        val roomId = repository.getChatRoomId(otherUserId)
        viewModelScope.launch {
            repository.sendMessage(roomId, text)
        }
    }
}