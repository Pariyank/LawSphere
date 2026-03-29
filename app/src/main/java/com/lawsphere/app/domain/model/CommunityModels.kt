package com.lawsphere.app.domain.model

data class LawyerProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val specialization: String = "General Law",
    val experience: Int = 0,
    val location: String = "India",
    val role: String = "lawyer"
)