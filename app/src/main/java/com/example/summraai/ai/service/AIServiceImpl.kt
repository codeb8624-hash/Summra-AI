package com.example.summraai.ai.service

import android.util.Log
import com.example.summraai.data.remote.RetrofitClient
import com.example.summraai.data.remote.SummarizeRequest
import java.io.IOException

class AIServiceImpl : AIService {

    override suspend fun generateSummary(
        text: String,
        style: String
    ): Result<String> {
        return try {
            val request = SummarizeRequest(text = text, style = style)
            if (Log.isLoggable("AIServiceImpl", Log.DEBUG)) {
                Log.d("AIServiceImpl", "Sending: style=$style, text_len=${text.length}")
            }
            val response = RetrofitClient.backendApi.summarize(request)
            if (Log.isLoggable("AIServiceImpl", Log.DEBUG)) {
                Log.d("AIServiceImpl", "Response: success=${response.success}, has_summary=${response.summary != null}")
            }
            if (response.success && response.summary != null) {
                Result.success(response.summary)
            } else {
                Result.failure(IOException(response.message ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            if (Log.isLoggable("AIServiceImpl", Log.ERROR)) {
                Log.e("AIServiceImpl", "Request failed: ${e.message}", e)
            }
            Result.failure(e)
        }
    }
}
