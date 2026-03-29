package com.lawsphere.app.domain.model

data class LegalForm(
    val id: Int,
    val title: String,
    val category: String,
    val storage_url: String
)