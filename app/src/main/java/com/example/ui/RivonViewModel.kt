package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.RivonPreferences
import com.example.data.local.SavedFactEntity
import com.example.data.remote.ChatMessageDto
import com.example.data.remote.ElevenLabsRepository
import com.example.data.remote.OpenRouterRepository
import com.example.engine.AndroidVoiceEngine
import com.example.engine.DeviceActionEngine
import com.example.model.ActionRiskLevel
import com.example.model.AiModel
import com.example.model.ChatMessage
import com.example.model.DeviceAction
import com.example.model.InstalledApp
import com.example.model.LanguageMode
import com.example.model.MessageSender
import com.example.model.PersonalityConfig
import com.example.model.PersonalityStyle
import com.example.model.PlannedAction
import com.example.model.RivonOrbState
import com.example.model.VoiceEngineType
import com.example.model.VoiceModel
import com.example.service.RivonWakeWordService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    MAIN,
    VOICE_IMMERSIVE,
    CHAT,
    AI_MODELS,
    ELEVENLABS_VOICES,
    APP_MANAGER,
    MEMORY,
    PERSONALITY,
    WAKE_WORD,
    PERMISSIONS,
    API_CONFIG,
    ABOUT
}

class RivonViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val preferences = RivonPreferences(context)
    private val database = AppDatabase.getDatabase(context)
    private val memoryDao = database.memoryDao()

    val openRouterRepository = OpenRouterRepository()
    val elevenLabsRepository = ElevenLabsRepository(context)
    val actionEngine = DeviceActionEngine(context)
    val voiceEngine = AndroidVoiceEngine(context, elevenLabsRepository, viewModelScope)

    // Navigation state
    private val _currentScreen = MutableStateFlow(ScreenDestination.MAIN)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Orb & Assistant State
    private val _orbState = MutableStateFlow(RivonOrbState.IDLE)
    val orbState: StateFlow<RivonOrbState> = _orbState.asStateFlow()

    // Messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Models & Voices
    private val _availableModels = MutableStateFlow<List<AiModel>>(AiModel.ALL_DEFAULTS)
    val availableModels: StateFlow<List<AiModel>> = _availableModels.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceModel>>(VoiceModel.POPULAR_VOICES)
    val availableVoices: StateFlow<List<VoiceModel>> = _availableVoices.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    // Confirmation Dialog state for Medium / High risk actions
    private val _pendingActionConfirmation = MutableStateFlow<PlannedAction?>(null)
    val pendingActionConfirmation: StateFlow<PlannedAction?> = _pendingActionConfirmation.asStateFlow()

    // Execution feedback toast/banner
    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    // Audio reactivity flow
    val audioRmsLevel: StateFlow<Float> = voiceEngine.audioRmsLevel
    val isListening: StateFlow<Boolean> = voiceEngine.isListening
    val isSpeaking: StateFlow<Boolean> = voiceEngine.isSpeaking
    val transcribedSpeech: StateFlow<String> = voiceEngine.transcribedText

    // Settings flows
    val selectedModel = preferences.selectedModelFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), RivonPreferences.DEFAULT_MODEL
    )
    val voiceId = preferences.voiceIdFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), RivonPreferences.DEFAULT_VOICE_ID
    )
    val voiceEngineType = preferences.voiceEngineFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), VoiceEngineType.ELEVENLABS
    )
    val voiceSpeed = preferences.voiceSpeedFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f
    )
    val openRouterKey = preferences.openRouterKeyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )
    val elevenLabsKey = preferences.elevenLabsKeyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )
    val wakeWordEnabled = preferences.wakeWordEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val personalityStyle = preferences.personalityStyleFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), PersonalityStyle.JARVIS
    )
    val languageMode = preferences.languageModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageMode.AUTO
    )
    val userName = preferences.userNameFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Commander"
    )

    // Saved memory facts
    val savedFacts = memoryDao.getAllFacts().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        // Voice recognition completion hook
        voiceEngine.onSpeechFinalResult = { text ->
            processUserInput(text)
        }

        // Sync orb state with voice engine
        viewModelScope.launch {
            combine(voiceEngine.isListening, voiceEngine.isSpeaking) { listening, speaking ->
                when {
                    listening -> RivonOrbState.LISTENING
                    speaking -> RivonOrbState.SPEAKING
                    else -> if (_orbState.value == RivonOrbState.THINKING || _orbState.value == RivonOrbState.EXECUTING) {
                        _orbState.value
                    } else {
                        RivonOrbState.IDLE
                    }
                }
            }.collect { state ->
                if (_orbState.value != RivonOrbState.THINKING && _orbState.value != RivonOrbState.EXECUTING) {
                    _orbState.value = state
                }
            }
        }

        // Load conversation history from Room
        viewModelScope.launch {
            memoryDao.getAllMessages().collect { list ->
                if (list.isNotEmpty() && _messages.value.isEmpty()) {
                    _messages.value = list.map { entity ->
                        ChatMessage(
                            id = entity.id.toString(),
                            sender = try { MessageSender.valueOf(entity.sender) } catch (e: Exception) { MessageSender.SYSTEM },
                            text = entity.text,
                            timestamp = entity.timestamp
                        )
                    }
                }
            }
        }

        // Refresh installed apps
        refreshInstalledApps()

        // Fetch dynamic models and voices in background
        refreshModelsAndVoices()
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun popBack(): Boolean {
        if (_currentScreen.value != ScreenDestination.MAIN) {
            _currentScreen.value = ScreenDestination.MAIN
            return true
        }
        return false
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = actionEngine.getInstalledApps()
            _installedApps.value = apps
        }
    }

    fun refreshModelsAndVoices() {
        viewModelScope.launch {
            val key = openRouterKey.value
            val modelsResult = openRouterRepository.fetchAvailableModels(key)
            modelsResult.onSuccess {
                _availableModels.value = it
            }

            val eKey = elevenLabsKey.value
            val voicesResult = elevenLabsRepository.fetchVoices(eKey)
            voicesResult.onSuccess {
                _availableVoices.value = it
            }
        }
    }

    fun toggleVoiceListening() {
        if (voiceEngine.isListening.value) {
            voiceEngine.stopListening()
            _orbState.value = RivonOrbState.IDLE
        } else {
            _orbState.value = RivonOrbState.LISTENING
            voiceEngine.startListening()
        }
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
        _orbState.value = RivonOrbState.IDLE
    }

    fun processUserInput(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank()) return

        // Add user message to state
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = text
        )
        _messages.value = _messages.value + userMsg

        // Persist to Room
        viewModelScope.launch {
            memoryDao.insertMessage(
                ConversationEntity(
                    sender = MessageSender.USER.name,
                    text = text
                )
            )
        }

        _orbState.value = RivonOrbState.THINKING

        // Check for local instant action matching first (e.g. "open instagram", "set volume to 50%")
        val instantAction = actionEngine.parseActionFromInput(text, null)
        if (instantAction != null) {
            handleParsedAction(instantAction, userQuery = text, aiText = null)
            return
        }

        // Call OpenRouter AI
        viewModelScope.launch {
            callAiAssistant(text)
        }
    }

    private suspend fun callAiAssistant(userPrompt: String) {
        val config = PersonalityConfig(
            style = personalityStyle.value,
            languageMode = languageMode.value,
            assistantName = "RIVON"
        )
        val systemPrompt = config.buildSystemPrompt()

        // Build recent conversation history
        val historyDto = mutableListOf(ChatMessageDto(role = "system", content = systemPrompt))
        val recentMsgs = _messages.value.takeLast(8)
        for (m in recentMsgs) {
            val role = if (m.sender == MessageSender.USER) "user" else "assistant"
            historyDto.add(ChatMessageDto(role = role, content = m.text))
        }

        val key = openRouterKey.value
        val model = selectedModel.value

        val responseResult = openRouterRepository.sendChatCompletion(
            apiKey = key,
            modelId = model,
            messages = historyDto,
            allowFallback = true
        )

        responseResult.onSuccess { (aiText, usedModel) ->
            // Check if AI included a device action
            val parsedAction = actionEngine.parseActionFromInput(userPrompt, aiText)

            if (parsedAction != null) {
                handleParsedAction(parsedAction, userQuery = userPrompt, aiText = aiText)
            } else {
                _orbState.value = RivonOrbState.SPEAKING
                val assistantMsg = ChatMessage(
                    sender = MessageSender.RIVON,
                    text = aiText
                )
                _messages.value = _messages.value + assistantMsg

                // Persist
                memoryDao.insertMessage(
                    ConversationEntity(
                        sender = MessageSender.RIVON.name,
                        text = aiText
                    )
                )

                // Speak via ElevenLabs or fallback TTS
                speakResponse(aiText)
            }
        }.onFailure { err ->
            _orbState.value = RivonOrbState.ERROR
            val fallbackMsg = "Connection issue: ${err.message}. Retrying or check your API keys."
            val errMessage = ChatMessage(
                sender = MessageSender.RIVON,
                text = fallbackMsg
            )
            _messages.value = _messages.value + errMessage
            speakResponse(fallbackMsg)
        }
    }

    private fun handleParsedAction(action: DeviceAction, userQuery: String, aiText: String?) {
        val planned = PlannedAction(action = action)

        // Safety Risk Check: Medium & High risk require user confirmation
        if (action.riskLevel == ActionRiskLevel.HIGH || (action.riskLevel == ActionRiskLevel.MEDIUM && action is DeviceAction.ComposeEmail)) {
            _orbState.value = RivonOrbState.IDLE
            _pendingActionConfirmation.value = planned

            val promptText = "Please confirm: ${action.description}"
            val msg = ChatMessage(
                sender = MessageSender.RIVON,
                text = promptText,
                plannedAction = planned
            )
            _messages.value = _messages.value + msg
            speakResponse(promptText)
        } else {
            // Low risk or immediate execution (e.g. launch app, search, alarms)
            executePlannedAction(planned, spokenExplanation = aiText)
        }
    }

    fun confirmPendingAction(planned: PlannedAction) {
        _pendingActionConfirmation.value = null
        executePlannedAction(planned, spokenExplanation = null)
    }

    fun cancelPendingAction(planned: PlannedAction) {
        _pendingActionConfirmation.value = null
        planned.status = com.example.model.ActionExecutionStatus.CANCELLED
        val cancelMsg = "Action cancelled."
        _messages.value = _messages.value + ChatMessage(
            sender = MessageSender.RIVON,
            text = cancelMsg
        )
        speakResponse(cancelMsg)
    }

    private fun executePlannedAction(planned: PlannedAction, spokenExplanation: String?) {
        _orbState.value = RivonOrbState.EXECUTING
        viewModelScope.launch {
            val result = actionEngine.executeAction(planned.action)
            planned.status = if (result.success) com.example.model.ActionExecutionStatus.SUCCESS else com.example.model.ActionExecutionStatus.FAILED
            planned.resultMessage = result.message

            _statusBanner.value = result.message

            val responseText = spokenExplanation ?: result.message
            val assistantMsg = ChatMessage(
                sender = MessageSender.RIVON,
                text = responseText,
                plannedAction = planned
            )
            _messages.value = _messages.value + assistantMsg

            memoryDao.insertMessage(
                ConversationEntity(
                    sender = MessageSender.RIVON.name,
                    text = responseText,
                    actionTitle = planned.action.title,
                    actionStatus = planned.status.name
                )
            )

            speakResponse(responseText)
        }
    }

    private fun speakResponse(text: String) {
        _orbState.value = RivonOrbState.SPEAKING
        voiceEngine.speak(
            text = text,
            preferredEngine = voiceEngineType.value,
            apiKey = elevenLabsKey.value,
            voiceId = voiceId.value,
            speed = voiceSpeed.value,
            onComplete = {
                _orbState.value = RivonOrbState.IDLE
            }
        )
    }

    fun clearHistory() {
        viewModelScope.launch {
            memoryDao.clearAllMessages()
            _messages.value = emptyList()
        }
    }

    fun saveFact(key: String, value: String, category: String = "general") {
        viewModelScope.launch {
            memoryDao.insertFact(SavedFactEntity(key = key, value = value, category = category))
        }
    }

    fun deleteFact(id: Long) {
        viewModelScope.launch {
            memoryDao.deleteFactById(id)
        }
    }

    fun setWakeWord(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setWakeWordEnabled(enabled)
            val serviceIntent = Intent(context, RivonWakeWordService::class.java)
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                context.stopService(serviceIntent)
            }
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
