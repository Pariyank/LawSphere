package com.lawsphere.app.presentation.explorer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawsphere.app.data.api.LawApi
import com.lawsphere.app.data.model.LookupRequest
import com.lawsphere.app.data.utils.JsonParser
import com.lawsphere.app.domain.model.BnsSection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val api: LawApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _sections = MutableStateFlow<List<BnsSection>>(emptyList())
    val sections = _sections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 🟢 NEW: State for Act Names loaded from File
    private val _availableActs = MutableStateFlow<List<String>>(emptyList())
    val availableActs = _availableActs.asStateFlow()

    init {
        loadActsFromFile()
    }

    private fun loadActsFromFile() {
        viewModelScope.launch {
            val acts = JsonParser.loadActList(context)
            _availableActs.value = acts.sorted()
        }
    }

    fun performExactSearch(actName: String, sectionNumber: String) {
        if (sectionNumber.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _sections.value = emptyList()
                val result = api.lookupSection(LookupRequest(actName, sectionNumber))

                val mappedSection = BnsSection(
                    section = result.section ?: sectionNumber,
                    title = result.title ?: "Section Details",
                    description = result.description ?: "Content not available.",
                    punishment = result.punishment ?: "N/A",
                    cognizable = result.cognizable ?: "N/A",
                    bailable = result.bailable ?: "N/A",
                    chapter = result.chapter ?: "General"
                )
                _sections.value = listOf(mappedSection)
            } catch (e: Exception) {
                _sections.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun clearResults() { _sections.value = emptyList() }
}