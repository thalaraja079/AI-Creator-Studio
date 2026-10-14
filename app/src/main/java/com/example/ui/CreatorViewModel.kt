package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiTool
import com.example.data.AiToolId
import com.example.data.CreatorPreferences
import com.example.data.AiGenerationService
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
    val showSignUpSuccessDialog: Boolean = false
)

class CreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = CreatorPreferences(application)
    private val authClient = (application as com.example.CreatorApplication).authClient
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
        // Fetch fresh config from Firestore
        adConfigRepository.fetchConfig {
            viewModelScope.launch {
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
        }
    }

    // --- Authentication & User Profile ---

    fun login(email: String, pass: String): Boolean {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(authErrorMessage = "Please enter both email and password.") }
            return false
        }
        viewModelScope.launch {
            authClient.login(email, pass) { success, error ->
                if (success) {
                    val localId = preferences.getLocalId()
                    val email = preferences.getUserEmail()
                    val userRepository = getApplication<android.app.Application>().let { (it as com.example.CreatorApplication).userRepository }
                    val maxCredits = adConfigRepository.getDailyFreeQuota()
                    
                    val idToken = preferences.getIdToken()
                    userRepository.getUserData(localId, idToken) { fields ->
                        if (fields == null) {
                            // First time login - init user in Firestore
                            val today = java.time.LocalDate.now().toString()
                            userRepository.createUser(localId, idToken, email, maxCredits, today)
                            preferences.resetQuotaToFull()
                        } else {
                            // Existing user - fetch remaining credits
                            val remainingCredits = fields.getJSONObject("remaining_credits").getInt("integerValue")
                            // used = max - remaining
                            val used = maxOf(0, maxCredits - remainingCredits)
                            preferences.setUsedQuotaForLogin(used)
                            _uiState.update { it.copy(remainingQuota = remainingCredits) }
                        }
                    }
                    
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            userName = preferences.getUserName(),
                            userEmail = preferences.getUserEmail(),
                            remainingQuota = preferences.getRemainingQuota(),
                            authErrorMessage = null,
                            currentTab = CreatorTab.HOME
                        )
                    }
                } else {
                    _uiState.update { it.copy(authErrorMessage = error ?: "Login failed") }
                }
            }
        }
        return true
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

        viewModelScope.launch {
            authClient.signUp(email, pass, name) { success, error ->
                if (success) {
                    val localId = preferences.getLocalId()
                    val email = preferences.getUserEmail()
                    val userRepository = getApplication<android.app.Application>().let { (it as com.example.CreatorApplication).userRepository }
                    val maxCredits = adConfigRepository.getDailyFreeQuota()
                    
                    val idToken = preferences.getIdToken()
                    val today = java.time.LocalDate.now().toString()
                    userRepository.createUser(localId, idToken, email, maxCredits, today)
                    preferences.resetQuotaToFull()
                    
                    _uiState.update {
                        it.copy(
                            showSignUpSuccessDialog = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(authErrorMessage = error ?: "Sign Up failed") }
                }
            }
        }
        return true
    }

    fun finalizeSignUp() {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                userName = preferences.getUserName(),
                userEmail = preferences.getUserEmail(),
                remainingQuota = preferences.getRemainingQuota(),
                showSignUpSuccessDialog = false,
                currentTab = CreatorTab.HOME
            )
        }
    }

    fun dismissSignUpSuccessDialog() {
        _uiState.update { it.copy(showSignUpSuccessDialog = false) }
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
        val idToken = preferences.getIdToken()
        if (idToken.isNotEmpty()) {
            viewModelScope.launch {
                authClient.deleteAccount(idToken) { _ ->
                    // Server deletion handled
                }
            }
        }
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
            val idToken = preferences.getIdToken()
            if (idToken.isNotEmpty()) {
                viewModelScope.launch {
                    authClient.updateDisplayName(idToken, newName.trim()) { success ->
                        if (success) {
                            preferences.setUserName(newName.trim())
                            _uiState.update {
                                it.copy(
                                    userName = preferences.getUserName(),
                                    showEditNameDialog = false
                                )
                            }
                        }
                    }
                }
            } else {
                // Fallback for local-only users
                preferences.setUserName(newName.trim())
                _uiState.update {
                    it.copy(
                        userName = preferences.getUserName(),
                        showEditNameDialog = false
                    )
                }
            }
        }
    }

    fun requestPasswordReset(email: String): Boolean {
        if (email.isBlank() || !email.contains("@")) {
            return false
        }
        viewModelScope.launch {
            authClient.sendPasswordReset(email) { sent ->
                _uiState.update { it.copy(forgotPasswordSent = sent) }
            }
        }
        return true
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
        if (tab == CreatorTab.HOME) {
            refreshState()
        }
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

        // API key check removed as we use the production worker

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
IMPORTANT: Keep the response concise, punchy, and short (maximum 2 to 4 sentences or under 80 words). Do not generate essays, full guides, or overly long playbooks.
            """.trimIndent()

            val result = AiGenerationService.generateContent(
                prompt = prompt
            )

            result.fold(
                onSuccess = { responseText ->
                    preferences.deductCredit()
                    val newRemaining = preferences.getRemainingQuota()
                    val localId = preferences.getLocalId()
                    val userRepository = getApplication<android.app.Application>().let { (it as com.example.CreatorApplication).userRepository }
                    val today = java.time.LocalDate.now().toString()
                    val idToken = preferences.getIdToken()
                    userRepository.updateUserData(localId, idToken, newRemaining, maxOf(0, state.dailyFreeQuota - newRemaining), today)
                    
                    val cleanResponse = cleanAiResponse(responseText)
                    val historyItem = GenerationHistoryItem(
                        toolId = "QUICK_STUDIO",
                        toolTitle = "Quick Studio Generator",
                        emojiIcon = "✨",
                        prompt = input,
                        output = cleanResponse
                    )
                    preferences.saveHistoryItem(historyItem)
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingTarget = null,
                            quickOutput = cleanResponse,
                            remainingQuota = newRemaining,
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

            val result = AiGenerationService.generateContent(
                prompt = finalPrompt
            )

            result.fold(
                onSuccess = { responseText ->
                    preferences.deductCredit()
                    val newRemaining = preferences.getRemainingQuota()
                    val localId = preferences.getLocalId()
                    val userRepository = getApplication<android.app.Application>().let { (it as com.example.CreatorApplication).userRepository }
                    val today = java.time.LocalDate.now().toString()
                    val idToken = preferences.getIdToken()
                    userRepository.updateUserData(localId, idToken, newRemaining, maxOf(0, state.dailyFreeQuota - newRemaining), today)
                    
                    val cleanResponse = cleanAiResponse(responseText)
                    val historyItem = GenerationHistoryItem(
                        toolId = tool.id.name,
                        toolTitle = tool.title,
                        emojiIcon = tool.emojiIcon,
                        prompt = input,
                        output = cleanResponse
                    )
                    preferences.saveHistoryItem(historyItem)
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingTarget = null,
                            toolOutput = cleanResponse,
                            remainingQuota = newRemaining,
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
            val newRemaining = preferences.getRemainingQuota()
            val localId = preferences.getLocalId()
            val userRepository = getApplication<android.app.Application>().let { (it as com.example.CreatorApplication).userRepository }
            val idToken = preferences.getIdToken()
            userRepository.updateUserData(localId, idToken, newRemaining, maxOf(0, state.dailyFreeQuota - newRemaining), java.time.LocalDate.now().toString())
            
            delay(300)
            _uiState.update {
                it.copy(
                    showRewardedAdDialog = false,
                    adProgress = 1f,
                    remainingQuota = newRemaining
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
                effectiveApiKey = preferences.getEffectiveApiKey()
            )
        }
    }

    fun saveModel(model: String) {
        preferences.saveSelectedModel(model)
        _uiState.update { it.copy(selectedModel = model) }
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

    private fun cleanAiResponse(response: String): String {
        var clean = response
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .trim()

        clean = clean
            .replace(Regex("\"(result|response|text|content|output|message)\":\\s*\"?"), "")
            .removePrefix("{")
            .removeSuffix("}")
            .removePrefix("\"")
            .removeSuffix("\"")
            .trim()

        // Remove duplicate text if the model output got repeated back-to-back
        val lines = clean.lines()
        if (lines.size > 4) {
            val mid = lines.size / 2
            val firstHalf = lines.subList(0, mid).joinToString("\n")
            val secondHalf = lines.subList(mid, lines.size).joinToString("\n")
            if (firstHalf.trim() == secondHalf.trim()) {
                clean = firstHalf.trim()
            }
        }

        // Ensure each major section appears at most once
        val sections = listOf("[TITLES]", "[DESCRIPTION]", "[TAGS]", "[HASHTAGS]", "[HOOKS]", "[SCRIPT]", "[CAPTIONS & HASHTAGS]")
        for (sec in sections) {
            val firstIdx = clean.indexOf(sec)
            if (firstIdx != -1) {
                val secondIdx = clean.indexOf(sec, firstIdx + sec.length)
                if (secondIdx != -1) {
                    clean = clean.substring(0, secondIdx).trim()
                }
            }
        }

        return clean.trim()
    }
}
