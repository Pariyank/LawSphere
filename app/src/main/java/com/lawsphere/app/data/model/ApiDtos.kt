package com.lawsphere.app.data.model

data class ChatRequest(val query: String, val language: String = "english")
data class CompareRequest(val section1: String, val section2: String)
data class LookupRequest(val act: String, val section: String)

data class RetrievedSource(val sourceNumber: Int = 0, val snippet: String = "")

data class ChatResponse(
    val formattedAnswer: String? = null,
    val section: String? = null,
    val title: String? = null,
    val description: String? = null,
    val punishment: String? = null,
    val cognizable: String? = null,
    val bailable: String? = null,
    val chapter: String? = null,
    val cases: List<String>? = emptyList(),
    val retrievedSources: List<RetrievedSource>? = emptyList()
)