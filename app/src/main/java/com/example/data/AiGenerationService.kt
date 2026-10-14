package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object AiGenerationService {

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private const val WORKER_URL = "https://ai-creator-studio-api.thalaraja079.workers.dev/"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val rootJson = JSONObject()
            rootJson.put("prompt", prompt)

            val requestBody = rootJson.toString().toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url(WORKER_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("API request failed with HTTP ${response.code}: $responseBodyString"))
            }

            // Assuming the worker returns the generated text directly or in a JSON structure.
            // Let's assume it returns a JSON like {"result": "..."}
            val jsonResponse = JSONObject(responseBodyString)
            val generatedText = jsonResponse.optString("result", "")
            
            if (generatedText.isNotEmpty()) {
                return@withContext Result.success(generatedText)
            } else {
                return@withContext Result.success(responseBodyString) // Fallback if no result key
            }
        } catch (e: Exception) {
            return@withContext Result.failure(IOException("Error calling AI generation service: ${e.message}", e))
        }
    }
}
