package com.example.lawsphere.data.api

import com.example.lawsphere.data.model.ChatRequest
import com.example.lawsphere.data.model.ChatResponse
import com.example.lawsphere.data.model.CompareRequest
import com.example.lawsphere.domain.model.BnsSection // Reuse existing model
import com.example.lawsphere.domain.model.NewsArticle
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LookupRequest(val query: String)

interface LawApi {

    @POST("api/ask")
    suspend fun chatWithLawSphere(@Body request: ChatRequest): ChatResponse

    @POST("api/compare")
    suspend fun compareSections(@Body request: CompareRequest): ChatResponse


    @POST("api/lookup")
    suspend fun lookupSection(@Body request: LookupRequest): BnsSection
}