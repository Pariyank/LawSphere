package com.example.lawsphere.domain.model

sealed class DraftingTemplate(val title: String, val id: String) {
    object FIR : DraftingTemplate("Police Complaint / FIR Application", "fir")
    object Bail : DraftingTemplate("Bail Application", "bail")
    object Notice : DraftingTemplate("Legal Notice", "notice")
}

data class DraftInput(
    val senderName: String = "",
    val recipientName: String = "",
    val date: String = "",
    val subject: String = "",
    val contentDetails: String = ""
)