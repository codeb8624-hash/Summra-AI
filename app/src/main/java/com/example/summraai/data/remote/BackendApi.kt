package com.example.summraai.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface BackendApi {
    @POST("api/summarize")
    suspend fun summarize(@Body request: SummarizeRequest): SummarizeResponse
}
