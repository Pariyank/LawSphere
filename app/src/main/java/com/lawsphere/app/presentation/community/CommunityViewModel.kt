package com.lawsphere.app.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawsphere.app.data.repository.CommunityRepository
import com.lawsphere.app.data.repository.PrivateChatRepository
import com.lawsphere.app.domain.model.ForumPost
import com.lawsphere.app.domain.model.InboxItem
import com.lawsphere.app.domain.model.LawyerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
    private val privateChatRepository: PrivateChatRepository
) : ViewModel() {

    private val _lawyers = MutableStateFlow<List<LawyerProfile>>(emptyList())
    val lawyers = _lawyers.asStateFlow()

    private val _inbox = MutableStateFlow<List<InboxItem>>(emptyList())
    val inbox = _inbox.asStateFlow()

    private val _posts = MutableStateFlow<List<ForumPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private var inboxJob: Job? = null

    fun refreshData() {

        _lawyers.value = emptyList()
        _inbox.value = emptyList()
        _posts.value = emptyList()

        inboxJob?.cancel()

        viewModelScope.launch {
            _isLoading.value = true
            repository.getLawyers().onSuccess { _lawyers.value = it }
            repository.getPosts().onSuccess { _posts.value = it }
            _isLoading.value = false
        }

        inboxJob = viewModelScope.launch {
            privateChatRepository.getInbox().collect { items ->
                _inbox.value = items
            }
        }
    }

    fun postQuestion(title: String, desc: String) {
        viewModelScope.launch { repository.createPost(title, desc); refreshData() }
    }

    fun answerQuestion(postId: String, answer: String) {
        viewModelScope.launch { repository.addAnswer(postId, answer); refreshData() }
    }
}