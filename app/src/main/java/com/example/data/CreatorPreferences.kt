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
        val maxQuota = getDailyFreeQuota()

        if (lastDate == null || lastDate != today) {
            // New day or first launch -> reset remaining quota to max
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putInt(KEY_REMAINING_QUOTA, maxQuota)
                .apply()
        }
    }

    // --- Authentication & User Profile Management ---

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Creator") ?: "Creator"
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name.trim()).apply()
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email.trim()).apply()
    }

    fun registerUser(name: String, email: String, pass: String): Boolean {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, name.trim())
            .putString(KEY_USER_EMAIL, email.trim().lowercase())
            .putString(KEY_USER_PASSWORD, pass)
            .apply()
        return true
    }

    fun loginUser(email: String, pass: String): Boolean {
        val savedEmail = getUserEmail()
        val savedPass = prefs.getString(KEY_USER_PASSWORD, "") ?: ""

        // If user already registered, verify credentials
        if (savedEmail.isNotEmpty() && savedPass.isNotEmpty()) {
            if (savedEmail.equals(email.trim(), ignoreCase = true) && savedPass == pass) {
                setLoggedIn(true)
                return true
            }
            return false
        }

        // First login or initial direct user login
        val derivedName = email.substringBefore("@").replace(".", " ").capitalize(Locale.ROOT).trim()
        registerUser(
            name = if (derivedName.isNotBlank()) derivedName else "Creator",
            email = email,
            pass = pass
        )
        return true
    }

    fun loginWithGoogle(name: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, name.trim())
            .putString(KEY_USER_EMAIL, email.trim().lowercase())
            .apply()
    }

    fun saveAuthTokens(idToken: String, refreshToken: String, localId: String, email: String, name: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_ID_TOKEN, idToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_LOCAL_ID, localId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getIdToken(): String = prefs.getString(KEY_ID_TOKEN, "") ?: ""
    fun getLocalId(): String = prefs.getString(KEY_LOCAL_ID, "") ?: ""
    
    fun logoutUser() {
        prefs.edit().remove(KEY_IS_LOGGED_IN)
            .remove(KEY_ID_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_LOCAL_ID)
            .apply()
    }

    /**
     * Google Play Policy Compliance: Complete Account Deletion
     * Permanently wipes user identity, history, quota, and local storage.
     */
    fun deleteAccountAndWipeData() {
        prefs.edit().clear().apply()
        // Reset quota afresh for clean state
        resetQuotaToFull()
    }

    fun requestPasswordReset(email: String): Boolean {
        // Validates email syntax or registered account
        return email.contains("@") && email.contains(".")
    }

    // --- Quota Management ---

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
}
