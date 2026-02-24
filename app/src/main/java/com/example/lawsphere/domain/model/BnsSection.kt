package com.example.lawsphere.domain.model

data class BnsSection(
    val chapter: String,
    val section: String,
    val title: String,
    val description: String,
    val cognizable: String,
    val bailable: String,
    val punishment: String,
    val cases: List<String> = emptyList()
)