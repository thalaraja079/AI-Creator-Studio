package com.example.data.repository

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class AppConfigRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val sharedPrefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    // Using the project's API key
    private val projectId = "ai-creator-studio-e0ac3"
    private val apiKey = "AIzaSyAduASPbIPCD5gJAv2-vgjyOVJ94HfBjt8"
    private val firestoreUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/app_config/settings?key=$apiKey"

    fun getAdsEnabled(): Boolean = sharedPrefs.getBoolean("ads_enabled", true)
    
    fun getAppOpenAdUnitId(): String = sharedPrefs.getString("app_open_ad_id", "ca-app-pub-3940256099942544/9257395921") ?: "ca-app-pub-3940256099942544/9257395921"
    
    fun getBannerAdUnitId(): String = sharedPrefs.getString("banner_ad_id", "ca-app-pub-3940256099942544/6300978111") ?: "ca-app-pub-3940256099942544/6300978111"
    
    fun getRewardedAdUnitId(): String = sharedPrefs.getString("rewarded_ad_id", "ca-app-pub-3940256099942544/5224354917") ?: "ca-app-pub-3940256099942544/5224354917"

    fun getDailyFreeQuota(): Int = sharedPrefs.getInt("daily_free_quota", 10)

    fun fetchConfig(onComplete: (() -> Unit)? = null) {
        val request = Request.Builder().url(firestoreUrl).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("AppConfig", "Failed to fetch app config", e)
                onComplete?.invoke()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    onComplete?.invoke()
                    return
                }
                val json = response.body?.string() ?: return
                
                try {
                    val root = JSONObject(json)
                    val fields = root.optJSONObject("fields") ?: return
                    
                    val dailyFreeQuota = fields.optJSONObject("daily_free_quota")?.optInt("integerValue") ?: 10
                    val adsEnabled = fields.optJSONObject("ads_enabled")?.optBoolean("booleanValue") ?: true
                    val appOpenAdId = fields.optJSONObject("app_open_ad_id")?.optString("stringValue") ?: "ca-app-pub-3940256099942544/9257395921"
                    val bannerAdId = fields.optJSONObject("banner_ad_id")?.optString("stringValue") ?: "ca-app-pub-3940256099942544/6300978111"
                    val rewardedAdId = fields.optJSONObject("rewarded_ad_id")?.optString("stringValue") ?: "ca-app-pub-3940256099942544/5224354917"
                    
                    sharedPrefs.edit()
                        .putInt("daily_free_quota", dailyFreeQuota)
                        .putBoolean("ads_enabled", adsEnabled)
                        .putString("app_open_ad_id", appOpenAdId)
                        .putString("banner_ad_id", bannerAdId)
                        .putString("rewarded_ad_id", rewardedAdId)
                        .apply()
                        
                } catch (e: Exception) {
                    Log.e("AppConfig", "Error parsing config", e)
                } finally {
                    onComplete?.invoke()
                }
            }
        })
    }
}
