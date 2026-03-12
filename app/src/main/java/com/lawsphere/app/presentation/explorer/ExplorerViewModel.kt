package com.lawsphere.app.presentation.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawsphere.app.data.api.LawApi
import com.lawsphere.app.data.model.LookupRequest
import com.lawsphere.app.domain.model.BnsSection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val api: LawApi
) : ViewModel() {

    private val _sections = MutableStateFlow<List<BnsSection>>(emptyList())
    val sections = _sections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun performExactSearch(actName: String, sectionNumber: String) {
        if (sectionNumber.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _sections.value = emptyList()

                val result = api.lookupSection(
                    LookupRequest(
                        act = actName,
                        section = sectionNumber
                    )
                )
                _sections.value = listOf(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _sections.value = emptyList()
    }
}