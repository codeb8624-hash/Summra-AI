package com.example.summraai.ai.service

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.summraai.data.remote.ChatRequest
import com.example.summraai.data.remote.ChatMessage
import com.example.summraai.data.remote.RetrofitClient
import com.example.summraai.data.remote.SummarizeRequest
import com.example.summraai.data.remote.TaskRequest
import com.example.summraai.data.remote.WebsiteSummarizeRequest
import com.example.summraai.data.remote.YoutubeSummarizeRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class AIServiceImpl : AIService {

    override suspend fun generateSummary(
        text: String,
        style: String
    ): Result<String> {
        return try {
            val request = SummarizeRequest(text = text, style = style)
            val response = RetrofitClient.backendApi.summarize(request)
            if (response.success && response.summary != null) {
                Result.success(response.summary)
            } else {
                Result.failure(IOException(response.message ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateWebsiteSummary(
        url: String,
        style: String
    ): Result<WebsiteSummaryData> {
        return try {
            val request = WebsiteSummarizeRequest(url = url, style = style)
            val response = RetrofitClient.backendApi.summarizeWebsite(request)
            if (response.success && response.summary != null) {
                Result.success(
                    WebsiteSummaryData(
                        content = response.summary,
                        documentId = response.documentId,
                        title = response.metadata?.title,
                        domain = response.metadata?.domain,
                        author = response.metadata?.author,
                        publishedDate = response.metadata?.publishedDate,
                        language = response.metadata?.language,
                        description = response.metadata?.description,
                        ogImage = response.metadata?.ogImage,
                        favicon = response.metadata?.favicon,
                        canonicalUrl = response.metadata?.canonicalUrl,
                        wordCount = response.wordCount,
                        originalWordCount = response.originalWordCount,
                        compressionRatio = response.compressionRatio,
                        readingTimeSeconds = response.readingTimeSeconds,
                        style = response.style
                    )
                )
            } else {
                Result.failure(IOException(response.message ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateYoutubeSummary(
        url: String,
        style: String
    ): Result<YoutubeSummaryData> {
        return try {
            val request = YoutubeSummarizeRequest(url = url, style = style)
            val response = RetrofitClient.backendApi.summarizeYoutube(request)
            if (response.success && response.summary != null) {
                Result.success(
                    YoutubeSummaryData(
                        content = response.summary,
                        documentId = response.documentId,
                        title = response.metadata?.title,
                        channel = response.metadata?.channel,
                        channelUrl = response.metadata?.channelUrl,
                        durationSeconds = response.metadata?.durationSeconds,
                        thumbnailUrl = response.metadata?.thumbnailUrl,
                        viewCount = response.metadata?.viewCount,
                        publishDate = response.metadata?.publishDate,
                        description = response.metadata?.description,
                        language = response.metadata?.language,
                        wordCount = response.wordCount,
                        transcriptWordCount = response.transcriptWordCount,
                        videoDurationFormatted = response.videoDurationFormatted,
                        style = response.style
                    )
                )
            } else {
                Result.failure(IOException(response.message ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generatePdfSummary(
        uri: Uri,
        style: String,
        contentResolver: ContentResolver
    ): Result<PdfSummaryData> {
        return try {
            val fileName = getFileName(uri, contentResolver) ?: "document.pdf"

            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.failure(IOException("Could not read file"))

            val fileBody = bytes.toRequestBody("application/pdf".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, fileBody)
            val stylePart = style.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = RetrofitClient.backendApi.summarizePdf(filePart, stylePart)

            if (response.success && response.summary != null) {
                Result.success(
                    PdfSummaryData(
                        content = response.summary,
                        documentId = response.documentId,
                        fileName = response.fileName,
                        pages = response.pages,
                        wordCount = response.wordCount
                    )
                )
            } else {
                Result.failure(IOException(response.message ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun chat(
        documentId: String,
        question: String,
        history: List<ChatMessage>?
    ): Result<ChatResult> {
        return try {
            val request = ChatRequest(documentId = documentId, question = question, history = history)
            val response = RetrofitClient.backendApi.chat(request)
            Result.success(ChatResult(answer = response.answer, sources = response.sources))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun performTask(
        documentId: String,
        taskType: String,
        language: String?
    ): Result<String> {
        return try {
            val request = TaskRequest(documentId = documentId, taskType = taskType, language = language)
            val response = RetrofitClient.backendApi.performTask(request)
            Result.success(response.content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }
}
