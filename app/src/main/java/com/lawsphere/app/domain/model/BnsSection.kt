package com.lawsphere.app.domain.model

import com.google.gson.annotations.SerializedName

data class BnsSection(
    val section: String? = "N/A",
    val title: String? = "Unknown Title",
    val description: String? = "No content found.",
    val punishment: String? = "N/A",
    val cognizable: String? = "N/A",
    val bailable: String? = "N/A",
    val chapter: String? = "General",
    val cases: List<String>? = emptyList(),
    val category: String? = "General"
)