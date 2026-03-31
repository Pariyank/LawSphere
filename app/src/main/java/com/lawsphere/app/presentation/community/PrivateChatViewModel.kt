package com.lawsphere.app.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawsphere.app.data.repository.PrivateChatRepository
import com.lawsphere.app.domain.model.PrivateMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    val currentUserId: String? get() = repository.currentUserId
    private var chatJob: Job? = null

    fun loadMessages(otherUserId: String) {
        chatJob?.cancel()
        _messages.value = emptyList()
        val roomId = repository.getChatRoomId(otherUserId)
        chatJob = viewModelScope.launch {
            repository.getMessages(roomId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(otherUserId: String, otherUserName: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(otherUserId, otherUserName, text)
        }
    }

    fun deleteForEveryone(otherUserId: String, messageId: String) {
        val roomId = repository.getChatRoomId(otherUserId)
        viewModelScope.launch {
            repository.deleteForEveryone(roomId, messageId)
        }
    }

    fun deleteForMe(otherUserId: String, messageId: String) {
        val roomId = repository.getChatRoomId(otherUserId)
        viewModelScope.launch {
            repository.deleteForMe(roomId, messageId)
        }
    }

    fun markSeen(otherUserId: String, messageId: String) {
        val roomId = repository.getChatRoomId(otherUserId)
        viewModelScope.launch {
            repository.markSeen(roomId, messageId)
        }
    }
}