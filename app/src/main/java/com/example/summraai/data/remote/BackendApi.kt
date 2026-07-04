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

    @POST("api/summarize/website")
    suspend fun summarizeWebsite(@Body request: WebsiteSummarizeRequest): WebsiteSummarizeResponse

    @POST("api/pdf/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @POST("api/pdf/task")
    suspend fun performTask(@Body request: TaskRequest): TaskResponse

    @POST("api/youtube/summarize")
    suspend fun summarizeYoutube(@Body request: YoutubeSummarizeRequest): YoutubeSummarizeResponse

    @POST("api/youtube/chat")
    suspend fun youtubeChat(@Body request: ChatRequest): ChatResponse

    @POST("api/youtube/task")
    suspend fun youtubeTask(@Body request: TaskRequest): TaskResponse
}
