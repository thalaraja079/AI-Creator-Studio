package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiTool
import com.example.data.AiToolId
import com.example.data.CreatorPreferences
import com.example.data.GeminiApiClient
import com.example.data.GenerationHistoryItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CreatorTab(val title: String, val iconLabel: String) {
    HOME("Home", "home"),
    TOOLS("Tools", "tools"),
    HISTORY("History", "history"),
    SETTINGS("Settings", "settings")
}

enum class KeyTestStatus {
    IDLE,
    TESTING,
    SUCCESS,
    ERROR
}

data class CreatorUiState(
    val currentTab: CreatorTab = CreatorTab.HOME,
    // Auth & Profile
    val isLoggedIn: Boolean = false,
    val userName: String = "Creator",
    val userEmail: String = "",
    val authErrorMessage: String? = null,
    val forgotPasswordSent: Boolean = false,
    val showPrivacyPolicyDialog: Boolean = false,
    val showEditNameDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false,
    val showDeleteAccountConfirmDialog: Boolean = false,
    // Quota & Tools
    val remainingQuota: Int = CreatorPreferences.DEFAULT_MAX_QUOTA,
    val dailyFreeQuota: Int = CreatorPreferences.DEFAULT_MAX_QUOTA,
    val selectedToolId: AiToolId = AiToolId.YOUTUBE_SEO_TAGS,
    val isToolDetailOpen: Boolean = false,
    val toolInput: String = "",
    val toolExtraArg: String = "English",
    val toolOutput: String = "",
    val quickInput: String = "",
    val quickOutput: String = "",
    val isGenerating: Boolean = false,
    val generatingTarget: String? = null, // "quick" or "tool"
    val errorMessage: String? = null,
    val showRewardedAdDialog: Boolean = false,
    val adProgress: Float = 0f,
    val history: List<GenerationHistoryItem> = emptyList(),
    val customApiKey: String = "",
    val effectiveApiKey: String = "",
    val selectedModel: String = CreatorPreferences.DEFAULT_MODEL,
    val keyTestStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val keyTestMessage: String? = null
)

class CreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = CreatorPreferences(application)
    private val adConfigRepository = (application as com.example.CreatorApplication).adConfigRepository

    private val _uiState = MutableStateFlow(
        CreatorUiState(
            isLoggedIn = preferences.isLoggedIn(),
            userName = preferences.getUserName(),
            userEmail = preferences.getUserEmail(),
            remainingQuota = preferences.getRemainingQuota(),
            dailyFreeQuota = preferences.getDailyFreeQuota(),
            history = preferences.getHistory(),
            customApiKey = preferences.getSavedCustomApiKey(),
            effectiveApiKey = preferences.getEffectiveApiKey(),
            selectedModel = preferences.getSelectedModel()
        )
    )
    val uiState: StateFlow<CreatorUiState> = _uiState.asStateFlow()

    private var adSimulationJob: Job? = null

    init {
        refreshState()
    }

    private fun refreshState() {
        _uiState.update { current ->
            current.copy(
                isLoggedIn = preferences.isLoggedIn(),
                userName = preferences.getUserName(),
                userEmail = preferences.getUserEmail(),
                remainingQuota = preferences.getRemainingQuota(),
                dailyFreeQuota = preferences.getDailyFreeQuota(),
                history = preferences.getHistory(),
                customApiKey = preferences.getSavedCustomApiKey(),
                effectiveApiKey = preferences.getEffectiveApiKey(),
                selectedModel = preferences.getSelectedModel()
            )
        }
    }

    // --- Authentication & User Profile ---

    fun login(email: String, pass: String): Boolean {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(authErrorMessage = "Please enter both email and password.") }
            return false
        }
        val success = preferences.loginUser(email, pass)
        if (success) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userName = preferences.getUserName(),
                    userEmail = preferences.getUserEmail(),
                    authErrorMessage = null,
                    currentTab = CreatorTab.HOME
                )
            }
            return true
        } else {
            _uiState.update { it.copy(authErrorMessage = "Invalid email or password.") }
            return false
        }
    }

    fun signUp(name: String, email: String, pass: String): Boolean {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(authErrorMessage = "All fields are required.") }
            return false
        }
        if (!email.contains("@") || !email.contains(".")) {
            _uiState.update { it.copy(authErrorMessage = "Please enter a valid email address.") }
            return false
        }
        if (pass.length < 6) {
            _uiState.update { it.copy(authErrorMessage = "Password must be at least 6 characters.") }
            return false
        }

        val success = preferences.registerUser(name, email, pass)
        if (success) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userName = preferences.getUserName(),
                    userEmail = preferences.getUserEmail(),
                    authErrorMessage = null,
                    currentTab = CreatorTab.HOME
                )
            }
            return true
        }
        return false
    }

    fun loginWithGoogle(name: String = "Google Creator", email: String = "creator.google@gmail.com") {
        preferences.loginWithGoogle(name, email)
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                userName = preferences.getUserName(),
                userEmail = preferences.getUserEmail(),
                authErrorMessage = null,
                currentTab = CreatorTab.HOME
            )
        }
    }

    fun logout() {
        preferences.logoutUser()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                showLogoutConfirmDialog = false,
                currentTab = CreatorTab.HOME
            )
        }
    }

    fun deleteAccount() {
        preferences.deleteAccountAndWipeData()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                userName = "Creator",
                userEmail = "",
                remainingQuota = preferences.getRemainingQuota(),
                history = emptyList(),
                customApiKey = "",
                showDeleteAccountConfirmDialog = false,
                currentTab = CreatorTab.HOME
            )
        }
    }

    fun updateUserName(newName: String) {
        if (newName.isNotBlank()) {
            preferences.setUserName(newName.trim())
            _uiState.update {
                it.copy(
                    userName = preferences.getUserName(),
                    showEditNameDialog = false
                )
            }
        }
    }

    fun requestPasswordReset(email: String): Boolean {
        if (email.isBlank() || !email.contains("@")) {
            return false
        }
        val sent = preferences.requestPasswordReset(email)
        _uiState.update { it.copy(forgotPasswordSent = sent) }
        return sent
    }

    fun clearAuthErrorMessage() {
        _uiState.update { it.copy(authErrorMessage = null) }
    }

    fun resetForgotPasswordStatus() {
        _uiState.update { it.copy(forgotPasswordSent = false) }
    }

    // Dialog toggles
    fun openPrivacyPolicyDialog() {
        _uiState.update { it.copy(showPrivacyPolicyDialog = true) }
    }

    fun dismissPrivacyPolicyDialog() {
        _uiState.update { it.copy(showPrivacyPolicyDialog = false) }
    }

    fun openEditNameDialog() {
        _uiState.update { it.copy(showEditNameDialog = true) }
    }

    fun dismissEditNameDialog() {
        _uiState.update { it.copy(showEditNameDialog = false) }
    }

    fun openLogoutDialog() {
        _uiState.update { it.copy(showLogoutConfirmDialog = true) }
    }

    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutConfirmDialog = false) }
    }

    fun openDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountConfirmDialog = true) }
    }

    fun dismissDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountConfirmDialog = false) }
    }

    // --- Tab & Tool Navigation ---

    fun switchTab(tab: CreatorTab) {
        _uiState.update { it.copy(currentTab = tab, errorMessage = null) }
    }

    fun selectTool(toolId: AiToolId, prefillPrompt: String? = null, openDetail: Boolean = true) {
        _uiState.update {
            it.copy(
                selectedToolId = toolId,
                toolInput = prefillPrompt ?: it.toolInput,
                isToolDetailOpen = openDetail,
                errorMessage = null
            )
        }
    }

    fun closeToolDetail() {
        _uiState.update { it.copy(isToolDetailOpen = false) }
    }

    fun updateToolInput(text: String) {
        _uiState.update { it.copy(toolInput = text) }
    }

    fun updateToolExtraArg(arg: String) {
        _uiState.update { it.copy(toolExtraArg = arg) }
    }

    fun updateQuickInput(text: String) {
        _uiState.update { it.copy(quickInput = text) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun generateQuickStudio() {
        val state = _uiState.value
        val input = state.quickInput.trim()
        if (input.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter an idea or prompt to generate.") }
            return
        }

        if (state.remainingQuota <= 0) {
            _uiState.update {
                it.copy(errorMessage = "Daily credits exhausted! Watch an ad on Home to earn +1 credit.")
            }
            return
        }

        val apiKey = preferences.getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Gemini API Key is not set. Go to Settings or Secrets to configure your key.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generatingTarget = "quick",
                    errorMessage = null
                )
            }

            val prompt = """
You are AI Creator Studio. Provide a rich, creative, and production-ready answer for:
$input

Structure with bold headings, clean bullet points, actionable tips, and tags where appropriate.
            """.trimIndent()

            val result = GeminiApiClient.generateContent(
                apiKey = apiKey,
                model = state.selectedModel,
                prompt = prompt
            )

            result.fold(
                onSuccess = { responseText ->
                    preferences.deductCredit()
                    val historyItem = GenerationHistoryItem(
                        toolId = "QUICK_STUDIO",
                        toolTitle = "Quick Studio Generator",
                        emojiIcon = "✨",
                        prompt = input,
                        output = responseText
                    )
                    preferences.saveHistoryItem(historyItem)
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingTarget = null,
                            quickOutput = responseText,
                            remainingQuota = preferences.getRemainingQuota(),
                            history = preferences.getHistory()
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingTarget = null,
                            errorMessage = error.message ?: "Failed to generate AI content"
                        )
                    }
                }
            )
        }
    }

    fun generateFromTool() {
        val state = _uiState.value
        val input = state.toolInput.trim()
        if (input.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter prompt details for this tool.") }
            return
        }

        if (state.remainingQuota <= 0) {
            _uiState.update {
                it.copy(errorMessage = "Daily credits exhausted! Watch an ad on the Home screen to earn +1 credit.")
            }
            return
        }

        val apiKey = preferences.getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Gemini API Key is not configured. Configure it in Settings.")
            }
            return
        }

        val tool = AiTool.findById(state.selectedToolId)
        val finalPrompt = tool.systemPromptTemplate(input, state.toolExtraArg)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generatingTarget = "tool",
                    errorMessage = null
                )
            }

            val result = GeminiApiClient.generateContent(
                apiKey = apiKey,
                model = state.selectedModel,
                prompt = finalPrompt
            )

            result.fold(
                onSuccess = { responseText ->
                    preferences.deductCredit()
                    val historyItem = GenerationHistoryItem(
                        toolId = tool.id.name,
                        toolTitle = tool.title,
                        emojiIcon = tool.emojiIcon,
                        prompt = input,
                        output = responseText
                    )
                    preferences.saveHistoryItem(historyItem)
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingTarget = null,
                            toolOutput = responseText,
                            remainingQuota = preferences.getRemainingQuota(),
                            history = preferences.getHistory()
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingTarget = null,
                            errorMessage = error.message ?: "Generation error"
                        )
                    }
                }
            )
        }
    }

    // Rewarded Ad Simulation
    fun startWatchAdFlow() {
        val state = _uiState.value
        if (state.remainingQuota >= state.dailyFreeQuota) {
            return
        }

        _uiState.update { it.copy(showRewardedAdDialog = true, adProgress = 0f) }

        adSimulationJob?.cancel()
        adSimulationJob = viewModelScope.launch {
            val totalSteps = 20
            for (step in 1..totalSteps) {
                delay(120) // ~2.4 seconds total simulation
                val progress = step.toFloat() / totalSteps
                _uiState.update { it.copy(adProgress = progress) }
            }
            // Add credit
            preferences.addCredit()
            delay(300)
            _uiState.update {
                it.copy(
                    showRewardedAdDialog = false,
                    adProgress = 1f,
                    remainingQuota = preferences.getRemainingQuota()
                )
            }
        }
    }

    fun dismissAdDialog() {
        adSimulationJob?.cancel()
        _uiState.update { it.copy(showRewardedAdDialog = false, adProgress = 0f) }
    }

    // Settings & Dev Testing Actions
    fun resetQuotaToFull() {
        preferences.resetQuotaToFull()
        _uiState.update { it.copy(remainingQuota = preferences.getRemainingQuota()) }
    }

    fun simulateUseOneCredit() {
        preferences.simulateUseOneCredit()
        _uiState.update { it.copy(remainingQuota = preferences.getRemainingQuota()) }
    }

    fun saveCustomApiKey(key: String) {
        preferences.saveCustomApiKey(key)
        _uiState.update {
            it.copy(
                customApiKey = key,
                effectiveApiKey = preferences.getEffectiveApiKey(),
                keyTestStatus = KeyTestStatus.IDLE,
                keyTestMessage = null
            )
        }
    }

    fun saveModel(model: String) {
        preferences.saveSelectedModel(model)
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun testApiKey() {
        val apiKey = preferences.getEffectiveApiKey()
        if (apiKey.isBlank()) {
            _uiState.update {
                it.copy(
                    keyTestStatus = KeyTestStatus.ERROR,
                    keyTestMessage = "API Key is empty. Please enter a key or set GEMINI_API_KEY."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    keyTestStatus = KeyTestStatus.TESTING,
                    keyTestMessage = "Verifying with Gemini endpoint..."
                )
            }

            val testPrompt = "Reply with exactly one word: 'CONNECTED'."
            val result = GeminiApiClient.generateContent(
                apiKey = apiKey,
                model = _uiState.value.selectedModel,
                prompt = testPrompt
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            keyTestStatus = KeyTestStatus.SUCCESS,
                            keyTestMessage = "Success! Gemini API is active ($response)"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            keyTestStatus = KeyTestStatus.ERROR,
                            keyTestMessage = "Test failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    // History Actions
    fun deleteHistoryItem(id: String) {
        preferences.deleteHistoryItem(id)
        _uiState.update { it.copy(history = preferences.getHistory()) }
    }

    fun clearHistory() {
        preferences.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
    }
}
