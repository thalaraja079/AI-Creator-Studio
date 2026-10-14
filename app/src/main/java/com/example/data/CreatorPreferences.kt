package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatorPreferences(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_creator_studio_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USED_QUOTA = "used_quota" // New key
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_HISTORY_JSON = "history_json"

        // Auth & User Profile keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        
        // Token storage
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_LOCAL_ID = "local_id"

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

        if (lastDate == null || lastDate != today) {
            // New day -> reset used quota
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putInt(KEY_USED_QUOTA, 0)
                .apply()
        }
    }

    // --- Quota Management ---

    private fun getUsedQuota(): Int {
        // Migration from remaining_quota if necessary
        if (!prefs.contains(KEY_USED_QUOTA) && prefs.contains("remaining_quota")) {
            val remaining = prefs.getInt("remaining_quota", 0)
            val max = getDailyFreeQuota()
            val used = maxOf(0, max - remaining)
            prefs.edit().putInt(KEY_USED_QUOTA, used).remove("remaining_quota").apply()
            return used
        }
        return prefs.getInt(KEY_USED_QUOTA, 0)
    }

    fun getDailyFreeQuota(): Int {
        val appConfigPrefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        return appConfigPrefs.getInt("daily_free_quota", DEFAULT_MAX_QUOTA)
    }

    fun getRemainingQuota(): Int {
        checkAndResetDailyQuota()
        val max = getDailyFreeQuota()
        val used = getUsedQuota()
        return maxOf(0, max - used)
    }

    fun deductCredit(): Boolean {
        val used = getUsedQuota()
        val max = getDailyFreeQuota()
        if (used < max) {
            prefs.edit().putInt(KEY_USED_QUOTA, used + 1).apply()
            return true
        }
        return false
    }

    fun addCredit(): Boolean {
        val used = getUsedQuota()
        if (used > 0) {
            prefs.edit().putInt(KEY_USED_QUOTA, used - 1).apply()
            return true
        }
        return false
    }

    fun resetQuotaToFull() {
        prefs.edit()
            .putString(KEY_LAST_RESET_DATE, getCurrentDateKey())
            .putInt(KEY_USED_QUOTA, 0)
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
        return list.sortedByDescending { it.timestampMillis }
    }

    fun saveHistoryItem(item: GenerationHistoryItem) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.add(0, item)
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
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Creator") ?: "Creator"
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun setUserName(name: String) = prefs.edit().putString(KEY_USER_NAME, name).apply()

    fun saveAuthTokens(idToken: String, refreshToken: String, localId: String, email: String, displayName: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_ID_TOKEN, idToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_LOCAL_ID, localId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, displayName)
            .apply()
    }

    fun getLocalId(): String = prefs.getString(KEY_LOCAL_ID, "") ?: ""
    fun getIdToken(): String = prefs.getString(KEY_ID_TOKEN, "") ?: ""
    fun getRefreshToken(): String = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""

    fun loginWithGoogle(name: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun logoutUser() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_ID_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_LOCAL_ID)
            .apply()
    }

    fun setUsedQuotaForLogin(used: Int) {
        prefs.edit().putInt(KEY_USED_QUOTA, used).apply()
    }

    fun deleteAccountAndWipeData() {
        prefs.edit().clear().apply()
    }
}
