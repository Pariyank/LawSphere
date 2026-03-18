package com.lawsphere.app.data.api

import com.lawsphere.app.data.model.*
import com.lawsphere.app.domain.model.BnsSection
import retrofit2.http.Body
import retrofit2.http.POST

interface LawApi {
    @POST("api/ask")
    suspend fun chatWithLawSphere(@Body request: ChatRequest): ChatResponse

    @POST("api/compare")
    suspend fun compareSections(@Body request: CompareRequest): ChatResponse

    @POST("api/lookup")
    suspend fun lookupSection(@Body request: LookupRequest): ChatResponse
}