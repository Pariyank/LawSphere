package com.lawsphere.app.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawsphere.app.data.repository.CommunityRepository
import com.lawsphere.app.data.repository.PrivateChatRepository
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var inboxJob: Job? = null

    fun refreshData(userRole: String) {
        inboxJob?.cancel()

        _isLoading.value = true

        inboxJob = viewModelScope.launch {
            privateChatRepository.getInbox().collect { items ->
                _inbox.value = items
                _isLoading.value = false
            }
        }

        if (!userRole.equals("lawyer", ignoreCase = true)) {
            viewModelScope.launch {
                repository.getLawyers().onSuccess { _lawyers.value = it }
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            if (_isLoading.value) _isLoading.value = false
        }
    }
}