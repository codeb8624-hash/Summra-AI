package com.example.summraai.ai.service

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.summraai.data.remote.RetrofitClient
import com.example.summraai.data.remote.SummarizeRequest
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

            if (Log.isLoggable("AIServiceImpl", Log.DEBUG)) {
                Log.d("AIServiceImpl", "Uploading PDF: file=$fileName, size=${bytes.size}, style=$style")
            }

            val response = RetrofitClient.backendApi.summarizePdf(filePart, stylePart)

            if (Log.isLoggable("AIServiceImpl", Log.DEBUG)) {
                Log.d("AIServiceImpl", "PDF response: success=${response.success}, has_summary=${response.summary != null}")
            }

            if (response.success && response.summary != null) {
                Result.success(
                    PdfSummaryData(
                        content = response.summary,
                        fileName = response.fileName,
                        pages = response.pages,
                        wordCount = response.wordCount
                    )
                )
            } else {
                Result.failure(IOException(response.message ?: "Unknown server error"))
            }
        } catch (e: Exception) {
            if (Log.isLoggable("AIServiceImpl", Log.ERROR)) {
                Log.e("AIServiceImpl", "PDF request failed: ${e.message}", e)
            }
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
