package com.example.data.repository

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class UserRepository(private val apiKey: String) {
    private val client = OkHttpClient()
    private val projectId = "ai-creator-studio-e0ac3"
    private val baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users"

    fun getUserData(localId: String, callback: (JSONObject?) -> Unit) {
        val url = "$baseUrl/$localId?key=$apiKey"
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    return
                }
                try {
                    val json = JSONObject(response.body?.string() ?: "")
                    callback(json.getJSONObject("fields"))
                } catch (e: Exception) {
                    callback(null)
                }
            }
        })
    }

    fun updateUserData(localId: String, remainingCredits: Int) {
        val url = "$baseUrl/$localId?key=$apiKey&updateMask.fieldPaths=remaining_credits"
        val json = JSONObject().apply {
            put("fields", JSONObject().apply {
                put("remaining_credits", JSONObject().apply {
                    put("integerValue", remainingCredits.toString())
                })
            })
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).patch(body).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("UserRepository", "Failed to update credits", e)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                // Ignore
            }
        })
    }
    
    fun createUser(localId: String, email: String, maxCredits: Int) {
        val url = "$baseUrl?key=$apiKey&documentId=$localId"
        val json = JSONObject().apply {
            put("fields", JSONObject().apply {
                put("email", JSONObject().apply { put("stringValue", email) })
                put("remaining_credits", JSONObject().apply { put("integerValue", maxCredits.toString()) })
                put("max_credits", JSONObject().apply { put("integerValue", maxCredits.toString()) })
            })
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("UserRepository", "Failed to create user", e)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                // Ignore
            }
        })
    }
}
