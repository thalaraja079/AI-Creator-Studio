package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatorPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_creator_studio_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DAILY_FREE_QUOTA = "daily_free_quota"
        private const val KEY_REMAINING_QUOTA = "remaining_quota"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_HISTORY_JSON = "history_json"

        const val DEFAULT_MAX_QUOTA = 5
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    init {
        checkAndResetDailyQuota()
    }

    private fun getCurrentDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun checkAndResetDailyQuota() {
        val today = getCurrentDateKey()
        val lastDate = prefs.getString(KEY_LAST_RESET_DATE, null)
        val maxQuota = getDailyFreeQuota()

        if (lastDate == null || lastDate != today) {
            // New day or first launch -> reset remaining quota to max
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putInt(KEY_REMAINING_QUOTA, maxQuota)
                .apply()
        }
    }

    fun getDailyFreeQuota(): Int {
        return prefs.getInt(KEY_DAILY_FREE_QUOTA, DEFAULT_MAX_QUOTA)
    }

    fun getRemainingQuota(): Int {
        checkAndResetDailyQuota()
        return prefs.getInt(KEY_REMAINING_QUOTA, DEFAULT_MAX_QUOTA)
    }

    fun deductCredit(): Boolean {
        val current = getRemainingQuota()
        if (current > 0) {
            prefs.edit().putInt(KEY_REMAINING_QUOTA, current - 1).apply()
            return true
        }
        return false
    }

    fun addCredit(): Boolean {
        val current = getRemainingQuota()
        val max = getDailyFreeQuota()
        if (current < max) {
            prefs.edit().putInt(KEY_REMAINING_QUOTA, current + 1).apply()
            return true
        }
        return false
    }

    fun resetQuotaToFull() {
        val max = getDailyFreeQuota()
        val today = getCurrentDateKey()
        prefs.edit()
            .putString(KEY_LAST_RESET_DATE, today)
            .putInt(KEY_REMAINING_QUOTA, max)
            .apply()
    }

    fun simulateUseOneCredit(): Boolean {
        return deductCredit()
    }

    fun getEffectiveApiKey(): String {
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (customKey.isNotEmpty()) {
            return customKey
        }
        val buildConfigKey = BuildConfig.GEMINI_API_KEY
        if (buildConfigKey.isNotEmpty() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        return ""
    }

    fun getSavedCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
    }

    fun saveCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(): String {
        return prefs.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun saveSelectedModel(model: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL, model.trim()).apply()
    }

    fun getHistory(): List<GenerationHistoryItem> {
        val rawJson = prefs.getString(KEY_HISTORY_JSON, "[]") ?: "[]"
        val list = mutableListOf<GenerationHistoryItem>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(GenerationHistoryItem.fromJson(obj))
            }
        } catch (_: Exception) {
        }
        // Reverse order so newest is at the top
        return list.sortedByDescending { it.timestampMillis }
    }

    fun saveHistoryItem(item: GenerationHistoryItem) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.add(0, item) // prepend
        // Limit history to 100 items to keep SharedPreferences snappy
        val trimmed = if (currentHistory.size > 100) currentHistory.take(100) else currentHistory
        persistHistory(trimmed)
    }

    fun deleteHistoryItem(id: String) {
        val currentHistory = getHistory().filterNot { it.id == id }
        persistHistory(currentHistory)
    }

    fun clearHistory() {
        prefs.edit().putString(KEY_HISTORY_JSON, "[]").apply()
    }

    private fun persistHistory(items: List<GenerationHistoryItem>) {
        val jsonArray = JSONArray()
        for (item in items) {
            jsonArray.put(item.toJson())
        }
        prefs.edit().putString(KEY_HISTORY_JSON, jsonArray.toString()).apply()
    }
}
