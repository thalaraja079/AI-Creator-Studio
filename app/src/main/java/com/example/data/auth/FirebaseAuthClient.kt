package com.example.data.auth

import android.content.Context
import android.util.Log
import com.example.data.CreatorPreferences
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class FirebaseAuthClient(private val context: Context, private val preferences: CreatorPreferences) {

    private val client = OkHttpClient()
    private val apiKey = "AIzaSyAduASPbIPCD5gJAv2-vgjyOVJ94HfBjt8"
    private val baseUrl = "https://identitytoolkit.googleapis.com/v1"

    fun signUp(email: String, pass: String, name: String, callback: (Boolean, String?) -> Unit) {
        val url = "$baseUrl/accounts:signUp?key=$apiKey"
        val json = JSONObject().apply {
            put("email", email)
            put("password", pass)
            put("returnSecureToken", true)
        }
        executeRequest(url, json) { success, jsonResponse ->
            if (success && jsonResponse != null) {
                val idToken = jsonResponse.getString("idToken")
                val localId = jsonResponse.getString("localId")
                val refreshToken = jsonResponse.getString("refreshToken")
                
                // Now update display name
                updateDisplayName(idToken, name) { displayNameSuccess ->
                    if (displayNameSuccess) {
                        preferences.saveAuthTokens(idToken, refreshToken, localId, email, name)
                        callback(true, null)
                    } else {
                        callback(false, "Failed to update profile name")
                    }
                }
            } else {
                callback(false, "Sign Up failed")
            }
        }
    }

    fun login(email: String, pass: String, callback: (Boolean, String?) -> Unit) {
        val url = "$baseUrl/accounts:signInWithPassword?key=$apiKey"
        val json = JSONObject().apply {
            put("email", email)
            put("password", pass)
            put("returnSecureToken", true)
        }
        executeRequest(url, json) { success, jsonResponse ->
            if (success && jsonResponse != null) {
                val idToken = jsonResponse.getString("idToken")
                val localId = jsonResponse.getString("localId")
                val refreshToken = jsonResponse.getString("refreshToken")
                
                // Fetch profile to get name
                getProfile(idToken) { profileSuccess, profileJson ->
                    if (profileSuccess && profileJson != null) {
                        val users = profileJson.getJSONArray("users")
                        val user = users.getJSONObject(0)
                        val name = user.optString("displayName", "Creator")
                        preferences.saveAuthTokens(idToken, refreshToken, localId, email, name)
                        callback(true, null)
                    } else {
                        callback(false, "Login failed to fetch profile")
                    }
                }
            } else {
                callback(false, "Login failed")
            }
        }
    }

    fun sendPasswordReset(email: String, callback: (Boolean) -> Unit) {
        val url = "$baseUrl/accounts:sendOobCode?key=$apiKey"
        val json = JSONObject().apply {
            put("requestType", "PASSWORD_RESET")
            put("email", email)
        }
        executeRequest(url, json) { success, _ -> callback(success) }
    }

    fun updateDisplayName(idToken: String, newName: String, callback: (Boolean) -> Unit) {
        val url = "$baseUrl/accounts:update?key=$apiKey"
        val json = JSONObject().apply {
            put("idToken", idToken)
            put("displayName", newName)
            put("returnSecureToken", true)
        }
        executeRequest(url, json) { success, _ -> callback(success) }
    }

    fun deleteAccount(idToken: String, callback: (Boolean) -> Unit) {
        val url = "$baseUrl/accounts:delete?key=$apiKey"
        val json = JSONObject().apply {
            put("idToken", idToken)
        }
        executeRequest(url, json) { success, _ -> 
            if (success) {
                preferences.deleteAccountAndWipeData()
            }
            callback(success) 
        }
    }

    private fun getProfile(idToken: String, callback: (Boolean, JSONObject?) -> Unit) {
        val url = "$baseUrl/accounts:lookup?key=$apiKey"
        val json = JSONObject().apply {
            put("idToken", idToken)
        }
        executeRequest(url, json, callback)
    }

    private fun executeRequest(url: String, json: JSONObject, callback: (Boolean, JSONObject?) -> Unit) {
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("FirebaseAuthClient", "Request failed", e)
                callback(false, null)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback(true, JSONObject(responseBody))
                } else {
                    Log.e("FirebaseAuthClient", "Request error: ${response.code} ${response.message} $responseBody")
                    callback(false, null)
                }
            }
        })
    }
}
