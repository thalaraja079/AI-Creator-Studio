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

            val extractedText = extractTextFromJson(responseBodyString)
            if (extractedText.isNotEmpty()) {
                return@withContext Result.success(extractedText)
            } else {
                return@withContext Result.success(responseBodyString)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(IOException("Error calling AI generation service: ${e.message}", e))
        }
    }

    private fun extractTextFromJson(rawJson: String): String {
        try {
            val trimmed = rawJson.trim()
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                return unescapeText(trimmed)
            }
            
            val json = JSONObject(trimmed)
            val possibleKeys = listOf("result", "response", "content", "text", "output", "message")
            for (key in possibleKeys) {
                if (json.has(key)) {
                    val value = json.get(key)
                    if (value is String) {
                        val nested = extractTextFromJson(value)
                        if (nested.isNotEmpty()) return unescapeText(nested)
                        return unescapeText(value)
                    } else if (value is JSONObject) {
                        val nested = extractTextFromJson(value.toString())
                        if (nested.isNotEmpty()) return unescapeText(nested)
                    }
                }
            }

            // Check candidates / choices
            if (json.has("candidates")) {
                val candidates = json.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    if (candidate.has("content")) {
                        val content = candidate.getJSONObject("content")
                        if (content.has("parts")) {
                            val parts = content.getJSONArray("parts")
                            val sb = StringBuilder()
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                if (part.has("text")) {
                                    sb.append(part.getString("text"))
                                }
                            }
                            if (sb.isNotEmpty()) return unescapeText(sb.toString())
                        }
                    }
                }
            }

            if (json.has("choices")) {
                val choices = json.getJSONArray("choices")
                if (choices.length() > 0) {
                    val choice = choices.getJSONObject(0)
                    if (choice.has("message")) {
                        val msg = choice.getJSONObject("message")
                        if (msg.has("content")) {
                            val content = msg.getString("content")
                            if (content.isNotEmpty()) return unescapeText(content)
                        }
                    }
                    if (choice.has("text")) {
                        val text = choice.getString("text")
                        if (text.isNotEmpty()) return unescapeText(text)
                    }
                }
            }
        } catch (e: Exception) {
            // Not valid JSON
        }
        return unescapeText(rawJson)
    }

    private fun unescapeText(text: String): String {
        return text
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\r", "\r")
            .trim()
    }
}
