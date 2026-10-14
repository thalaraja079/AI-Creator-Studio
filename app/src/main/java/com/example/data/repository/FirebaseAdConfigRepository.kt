package com.example.data.repository

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class FirebaseAdConfigRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val sharedPrefs = context.getSharedPreferences("ad_config", Context.MODE_PRIVATE)

    // TODO: Replace with the actual Firebase Firestore or RTDB REST URL
    private val firebaseConfigUrl = "https://your-firebase-project.firebaseio.com/ad_config.json"

    fun getAdsEnabled(): Boolean {
        return sharedPrefs.getBoolean("ads_enabled", true)
    }

    fun getAppOpenAdUnitId(): String {
        return sharedPrefs.getString("app_open_ad_unit_id", "ca-app-pub-3940256099942544/9257395921") ?: "ca-app-pub-3940256099942544/9257395921"
    }

    fun fetchConfig() {
        val request = Request.Builder().url(firebaseConfigUrl).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("FirebaseAdConfig", "Failed to fetch ad config", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) return
                val json = response.body?.string() ?: return
                val jsonObject = JSONObject(json)
                
                sharedPrefs.edit()
                    .putBoolean("ads_enabled", jsonObject.optBoolean("ads_enabled", true))
                    .putString("app_open_ad_unit_id", jsonObject.optString("app_open_ad_unit_id", "ca-app-pub-3940256099942544/9257395921"))
                    .apply()
            }
        })
    }
}
