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

    fun getUserQuota(localId: String, callback: (Int?) -> Unit) {
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
                    val usedQuota = json.getJSONObject("fields").getJSONObject("used_quota").getInt("integerValue")
                    callback(usedQuota)
                } catch (e: Exception) {
                    callback(null)
                }
            }
        })
    }

    fun updateUserQuota(localId: String, usedQuota: Int) {
        val url = "$baseUrl/$localId?key=$apiKey&updateMask.fieldPaths=used_quota"
        val json = JSONObject().apply {
            put("fields", JSONObject().apply {
                put("used_quota", JSONObject().apply {
                    put("integerValue", usedQuota.toString())
                })
            })
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).patch(body).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("UserRepository", "Failed to update quota", e)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                // Ignore
            }
        })
    }
    
    fun createUser(localId: String, initialUsedQuota: Int) {
        val url = "$baseUrl?key=$apiKey&documentId=$localId"
        val json = JSONObject().apply {
            put("fields", JSONObject().apply {
                put("used_quota", JSONObject().apply {
                    put("integerValue", initialUsedQuota.toString())
                })
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
