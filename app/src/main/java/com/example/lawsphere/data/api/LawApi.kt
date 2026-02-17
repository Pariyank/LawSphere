package com.example.lawsphere.data.api

import com.example.lawsphere.data.model.ChatRequest
import com.example.lawsphere.data.model.ChatResponse
import com.example.lawsphere.data.model.CompareRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface LawApi {

    // 🟢 FIXED: Changed from 'api/chat' to 'api/ask' to match server.js
    @POST("api/ask")
    suspend fun chatWithLawSphere(
        @Body request: ChatRequest
    ): ChatResponse

    // 🟢 FIXED: Ensures this matches server.js
    @POST("api/compare")
    suspend fun compareSections(
        @Body request: CompareRequest
    ): ChatResponse
}