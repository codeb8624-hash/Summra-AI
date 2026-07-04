package com.example.summraai.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.summraai.ai.service.AIService
import com.example.summraai.ai.service.AIServiceImpl
import com.example.summraai.ai.service.ChatResult
import com.example.summraai.data.remote.ChatMessage
import com.example.summraai.domain.model.SummaryStyle
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AISummaryRepositoryImpl(
    private val aiService: AIService = AIServiceImpl()
) : AISummaryRepository {

    override suspend fun generateTextSummary(
        text: String,
        style: SummaryStyle
    ): Result<String> {
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Text cannot be empty"))
        }

        return aiService.generateSummary(text, style.name)
            .mapError { mapToUserFriendlyMessage(it) }
    }

    override suspend fun generateWebsiteSummary(
        url: String,
        style: SummaryStyle
    ): Result<WebsiteSummaryResult> {
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("URL cannot be empty"))
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return Result.failure(IllegalArgumentException("Invalid URL format. URL must start with http:// or https://"))
        }
        return aiService.generateWebsiteSummary(url, style.name)
            .mapError { mapToUserFriendlyMessage(it) }
            .fold(
                onSuccess = { data ->
                    Result.success(
                        WebsiteSummaryResult(
                            content = data.content,
                            documentId = data.documentId,
                            style = style,
                            title = data.title,
                            domain = data.domain,
                            author = data.author,
                            publishedDate = data.publishedDate,
                            language = data.language,
                            description = data.description,
                            ogImage = data.ogImage,
                            favicon = data.favicon,
                            canonicalUrl = data.canonicalUrl,
                            wordCount = data.wordCount,
                            originalWordCount = data.originalWordCount,
                            compressionRatio = data.compressionRatio,
                            readingTimeSeconds = data.readingTimeSeconds
                        )
                    )
                },
                onFailure = { Result.failure(it) }
            )
    }

    override suspend fun generateYoutubeSummary(
        url: String,
        style: SummaryStyle
    ): Result<YoutubeSummaryResult> {
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("URL cannot be empty"))
        }
        return aiService.generateYoutubeSummary(url, style.name)
            .mapError { mapToUserFriendlyMessage(it) }
            .fold(
                onSuccess = { data ->
                    Result.success(
                        YoutubeSummaryResult(
                            content = data.content,
                            documentId = data.documentId,
                            style = style,
                            title = data.title,
                            channel = data.channel,
                            channelUrl = data.channelUrl,
                            durationSeconds = data.durationSeconds,
                            thumbnailUrl = data.thumbnailUrl,
                            viewCount = data.viewCount,
                            publishDate = data.publishDate,
                            description = data.description,
                            language = data.language,
                            wordCount = data.wordCount,
                            transcriptWordCount = data.transcriptWordCount,
                            videoDurationFormatted = data.videoDurationFormatted
                        )
                    )
                },
                onFailure = { Result.failure(it) }
            )
    }

    override suspend fun generatePdfSummary(
        uri: Uri,
        style: SummaryStyle,
        contentResolver: ContentResolver
    ): Result<PdfSummaryResult> {
        return aiService.generatePdfSummary(uri, style.name, contentResolver)
            .mapError { mapToUserFriendlyMessage(it) }
            .fold(
                onSuccess = { data ->
                    Result.success(
                        PdfSummaryResult(
                            content = data.content,
                            documentId = data.documentId,
                            style = style,
                            fileName = data.fileName,
                            pageCount = data.pages,
                            wordCount = data.wordCount
                        )
                    )
                },
                onFailure = { Result.failure(it) }
            )
    }

    override suspend fun chat(
        documentId: String,
        question: String,
        history: List<ChatMessage>?
    ): Result<ChatResult> {
        return aiService.chat(documentId, question, history)
            .mapError { mapToUserFriendlyMessage(it) }
    }

    override suspend fun performTask(
        documentId: String,
        taskType: String,
        language: String?
    ): Result<String> {
        return aiService.performTask(documentId, taskType, language)
            .mapError { mapToUserFriendlyMessage(it) }
    }

    private fun mapToUserFriendlyMessage(error: Throwable): Throwable {
        val message = when (error) {
            is SocketTimeoutException -> "Request timed out. Please check your connection and try again."
            is UnknownHostException -> "No internet connection. Please check your network settings."
            is HttpException -> mapHttpError(error.code())
            is IOException -> "Network error: ${error.message ?: "Please try again later."}"
            else -> error.message ?: "Something went wrong. Please try again."
        }
        return RuntimeException(message, error)
    }

    private fun mapHttpError(code: Int): String = when (code) {
        400 -> "Invalid request. Please try again."
        401 -> "Authentication failed. Please contact support."
        403 -> "Access denied. Please check your permissions."
        404 -> "Service endpoint not found. Please update the app."
        429 -> "Too many requests. Please wait a moment and try again."
        500 -> "Server error. Please try again later."
        502 -> "AI service temporarily unavailable. Please try again later."
        503 -> "Service is currently unavailable. Please try again later."
        else -> "Server error ($code). Please try again later."
    }

    private fun <T> Result<T>.mapError(mapper: (Throwable) -> Throwable): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(mapper(it)) }
        )
    }
}
