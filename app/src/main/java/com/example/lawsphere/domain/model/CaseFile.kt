package com.example.lawsphere.domain.model

data class CaseFile(
    val id: String = "",
    val clientName: String = "",
    val caseNumber: String = "",
    val courtName: String = "",
    val nextHearingDate: String = "",
    val status: String = "Active",
    val notes: String = ""
)