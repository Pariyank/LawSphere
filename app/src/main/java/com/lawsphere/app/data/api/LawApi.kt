package com.lawsphere.app.data.api

import com.lawsphere.app.data.model.ChatRequest
import com.lawsphere.app.data.model.ChatResponse
import com.lawsphere.app.data.model.CompareRequest
import com.lawsphere.app.domain.model.BnsSection // Reuse existing model
import retrofit2.http.Body
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