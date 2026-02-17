package com.example.lawsphere.domain.model

sealed class DraftingTemplate(val title: String, val id: String) {
    object FIR : DraftingTemplate("FIR Generator", "fir")
    object Bail : DraftingTemplate("Bail Application", "bail")
    object Notice : DraftingTemplate("Legal Notice", "notice")
}

data class DraftInput(
    val senderName: String = "",
    val recipientName: String = "", // Or Police Station
    val date: String = "",
    val subject: String = "",
    val contentDetails: String = ""
)