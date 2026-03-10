package com.lawsphere.app.domain.model

data class LawyerProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val specialization: String = "General Law",
    val experience: Int = 0,
    val location: String = "India"
)

data class ForumPost(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val authorId: String = "",
    val answers: List<ForumAnswer> = emptyList()
)

data class ForumAnswer(
    val lawyerName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class NewsArticle(
    val title: String,
    val description: String,
    val source: String,
    val date: String
)