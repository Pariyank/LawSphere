package com.example.lawsphere.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawsphere.data.repository.CommunityRepository
import com.example.lawsphere.domain.model.ForumPost
import com.example.lawsphere.domain.model.LawyerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository
) : ViewModel() {


    private val _lawyers = MutableStateFlow<List<LawyerProfile>>(emptyList())
    val lawyers = _lawyers.asStateFlow()

    private val _posts = MutableStateFlow<List<ForumPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getLawyers().onSuccess { _lawyers.value = it }
            repository.getPosts().onSuccess { _posts.value = it }

            _isLoading.value = false
        }
    }

    fun postQuestion(title: String, desc: String) {
        viewModelScope.launch {
            repository.createPost(title, desc)
            loadAllData()
        }
    }

    fun answerQuestion(postId: String, answer: String) {
        viewModelScope.launch {
            repository.addAnswer(postId, answer)
            loadAllData()
        }
    }
}