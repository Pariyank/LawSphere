package com.lawsphere.app.presentation.drafting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lawsphere.app.data.api.LawApi
import com.lawsphere.app.domain.model.LegalForm
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DraftingViewModel @Inject constructor(
    private val api: LawApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _forms = MutableStateFlow<List<LegalForm>>(emptyList())
    val forms = _forms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadForms()
    }

    private fun loadForms() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val jsonString = context.assets.open("bnss_forms.json").bufferedReader().use { it.readText() }
                val type = object : TypeToken<Map<String, List<LegalForm>>>() {}.type
                val data: Map<String, List<LegalForm>> = Gson().fromJson(jsonString, type)
                _forms.value = data["forms"] ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }

            try {
                val remoteData = api.getRemoteBnssForms()
                val remoteForms = remoteData["forms"] ?: emptyList()
                if (remoteForms.isNotEmpty()) {
                    _forms.value = remoteForms
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}