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

    private val _availableActs = MutableStateFlow<List<String>>(emptyList())
    val availableActs = _availableActs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val fallbackActs = listOf(
        "Bharatiya Nyaya Sanhita, 2023",
        "Bharatiya Nagarik Suraksha Sanhita, 2023",
        "Bharatiya Sakshya Adhiniyam, 2023",
        "The Constitution of India",
        "The Information Technology Act, 2000",
        "Indian Contract Act, 1872"
    )

    init {
        loadActs()
    }

    private fun loadActs() {
        viewModelScope.launch {
            val localActs = JsonParser.loadActList(context)
            _availableActs.value = localActs.sorted()

            try {
                val remoteResponse = api.getRemoteActList()
                val remoteActs = remoteResponse["acts"] ?: emptyList()
                if (remoteActs.isNotEmpty()) {
                    _availableActs.value = remoteActs.sorted()

                }
            } catch (e: Exception) { }
        }
    }

    fun performExactSearch(actName: String, sectionNumber: String) {
        if (sectionNumber.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _sections.value = emptyList()

                val res = api.lookupSection(LookupRequest(actName, sectionNumber))

                val mappedSection = BnsSection(
                    section = res.section ?: sectionNumber,
                    title = res.title ?: "Section Details",
                    description = res.description ?: "Statement not found.",
                    punishment = res.punishment ?: "N/A",
                    cognizable = res.cognizable ?: "N/A",
                    bailable = res.bailable ?: "N/A",
                    chapter = res.chapter ?: "General",
                    cases = res.cases ?: emptyList()
                )

                _sections.value = listOf(mappedSection)

            } catch (e: Exception) {
                _sections.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun clearResults() { _sections.value = emptyList() }
    fun resetToLocal() { _sections.value = emptyList() }
}