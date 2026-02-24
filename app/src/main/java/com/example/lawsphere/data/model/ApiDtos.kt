package com.example.lawsphere.data.model

data class ChatRequest(
    val query: String,
    val language: String = "english"
)

data class CompareRequest(
    val section1: String,
    val section2: String
)

data class RetrievedSource(
    val sourceNumber: Int = 0,
    val snippet: String = ""
)

data class ChatResponse(
    val formattedAnswer: String? = "No response generated.",
    val reasoning: String? = "",
    val semanticTags: List<String>? = emptyList(),
    val retrievedSources: List<RetrievedSource>? = emptyList()
)