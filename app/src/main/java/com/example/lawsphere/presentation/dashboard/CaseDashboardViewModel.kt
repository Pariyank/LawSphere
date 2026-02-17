package com.example.lawsphere.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawsphere.data.repository.CaseRepository
import com.example.lawsphere.domain.model.CaseFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaseDashboardViewModel @Inject constructor(
    private val repository: CaseRepository
) : ViewModel() {

    private val _cases = MutableStateFlow<List<CaseFile>>(emptyList())
    val cases = _cases.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadCases()
    }

    fun loadCases() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getCases()
            result.onSuccess {
                _cases.value = it
            }
            _isLoading.value = false
        }
    }

    fun addCase(client: String, number: String, court: String, date: String, notes: String) {
        viewModelScope.launch {
            val newCase = CaseFile(
                clientName = client,
                caseNumber = number,
                courtName = court,
                nextHearingDate = date,
                notes = notes
            )
            repository.addCase(newCase)
            loadCases() // Refresh list
        }
    }

    fun deleteCase(caseId: String) {
        viewModelScope.launch {
            repository.deleteCase(caseId)
            loadCases() // Refresh list
        }
    }
}