package com.example.summraai.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface BackendApi {
    @POST("api/summarize")
    suspend fun summarize(@Body request: SummarizeRequest): SummarizeResponse

    @Multipart
    @POST("api/summarize/pdf")
    suspend fun summarizePdf(
        @Part file: MultipartBody.Part,
        @Part("style") style: RequestBody
    ): PdfSummarizeResponse
}
