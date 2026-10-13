package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiApiClient {

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        apiKey: String,
        model: String,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Gemini API Key is not configured. Please add your API key in Settings or AI Studio Secrets.")
            )
        }

        val cleanModelName = model.trim().removePrefix("models/")
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${cleanModelName}:generateContent?key=${apiKey.trim()}"

        try {
            // Build request payload using org.json
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()

            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val requestBody = rootJson.toString().toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                var errorMessage = "API request failed with HTTP ${response.code}"
                try {
                    val errorJson = JSONObject(responseBodyString)
                    if (errorJson.has("error")) {
                        val errorObj = errorJson.getJSONObject("error")
                        val msg = errorObj.optString("message", "")
                        if (msg.isNotEmpty()) {
                            errorMessage = msg
                        }
                    }
                } catch (_: Exception) {
                }
                return@withContext Result.failure(IOException(errorMessage))
            }

            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val firstPart = parts.getJSONObject(0)
                    val generatedText = firstPart.optString("text", "")
                    if (generatedText.isNotEmpty()) {
                        return@withContext Result.success(generatedText)
                    }
                }
            }

            return@withContext Result.failure(IOException("No text content returned by Gemini."))
        } catch (e: Exception) {
            val friendlyMsg = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "Network error: Please check your internet connection."
                e.message?.contains("timeout") == true ->
                    "Request timed out. Please try again."
                else -> e.message ?: "Unknown error while calling Gemini API"
            }
            return@withContext Result.failure(IOException(friendlyMsg, e))
        }
    }
}
