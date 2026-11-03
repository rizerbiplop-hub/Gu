package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import com.example.model.LanguageMode
import com.example.model.PersonalityStyle
import com.example.model.VoiceEngineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rivon_preferences")

class RivonPreferences(private val context: Context) {

    companion object {
        val KEY_SELECTED_MODEL = stringPreferencesKey("selected_ai_model")
        val KEY_AUTO_FALLBACK = booleanPreferencesKey("auto_fallback_enabled")
        val KEY_VOICE_ID = stringPreferencesKey("elevenlabs_voice_id")
        val KEY_VOICE_ENGINE = stringPreferencesKey("voice_engine")
        val KEY_VOICE_SPEED = floatPreferencesKey("voice_speed")
        val KEY_OPENROUTER_KEY = stringPreferencesKey("openrouter_api_key")
        val KEY_ELEVENLABS_KEY = stringPreferencesKey("elevenlabs_api_key")
        val KEY_WAKE_WORD_ENABLED = booleanPreferencesKey("wake_word_enabled")
        val KEY_PERSONALITY_STYLE = stringPreferencesKey("personality_style")
        val KEY_LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_CONTINUOUS_CONVERSATION = booleanPreferencesKey("continuous_conversation")

        const val DEFAULT_MODEL = "meta-llama/llama-3.3-70b-instruct:free"
        const val DEFAULT_VOICE_ID = "21m00Tcm4TlvDq8ikWAM" // Rachel
        const val DEFAULT_OPENROUTER_KEY = "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"
        const val DEFAULT_ELEVENLABS_KEY = "sk_d55ecc9f7dfa7adc86ebfb859786598f60411b653e7bab7c"
    }

    val selectedModelFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_MODEL] ?: DEFAULT_MODEL
    }

    val autoFallbackFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_FALLBACK] ?: true
    }

    val voiceIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_ID] ?: DEFAULT_VOICE_ID
    }

    val voiceEngineFlow: Flow<VoiceEngineType> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_VOICE_ENGINE] ?: VoiceEngineType.ELEVENLABS.name
        try {
            VoiceEngineType.valueOf(name)
        } catch (e: Exception) {
            VoiceEngineType.ELEVENLABS
        }
    }

    val voiceSpeedFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_SPEED] ?: 1.0f
    }

    val continuousConversationFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CONTINUOUS_CONVERSATION] ?: true
    }

    val openRouterKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val custom = prefs[KEY_OPENROUTER_KEY]?.takeIf { it.isNotBlank() }
        val buildKey = getBuildConfigKey("OPENROUTER_API_KEY").takeIf { it.isNotBlank() }
        custom ?: buildKey ?: DEFAULT_OPENROUTER_KEY
    }

    val elevenLabsKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val custom = prefs[KEY_ELEVENLABS_KEY]?.takeIf { it.isNotBlank() }
        val buildKey = getBuildConfigKey("ELEVENLABS_API_KEY").takeIf { it.isNotBlank() }
        custom ?: buildKey ?: DEFAULT_ELEVENLABS_KEY
    }

    val wakeWordEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WAKE_WORD_ENABLED] ?: false
    }

    val personalityStyleFlow: Flow<PersonalityStyle> = context.dataStore.data.map { prefs ->
        val styleName = prefs[KEY_PERSONALITY_STYLE] ?: PersonalityStyle.JARVIS.name
        try {
            PersonalityStyle.valueOf(styleName)
        } catch (e: Exception) {
            PersonalityStyle.JARVIS
        }
    }

    val languageModeFlow: Flow<LanguageMode> = context.dataStore.data.map { prefs ->
        val langName = prefs[KEY_LANGUAGE_MODE] ?: LanguageMode.AUTO.name
        try {
            LanguageMode.valueOf(langName)
        } catch (e: Exception) {
            LanguageMode.AUTO
        }
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "Commander"
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setSelectedModel(modelId: String) {
        context.dataStore.edit { it[KEY_SELECTED_MODEL] = modelId }
    }

    suspend fun setAutoFallback(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_FALLBACK] = enabled }
    }

    suspend fun setVoiceId(voiceId: String) {
        context.dataStore.edit { it[KEY_VOICE_ID] = voiceId }
    }

    suspend fun setVoiceEngine(engine: VoiceEngineType) {
        context.dataStore.edit { it[KEY_VOICE_ENGINE] = engine.name }
    }

    suspend fun setVoiceSpeed(speed: Float) {
        context.dataStore.edit { it[KEY_VOICE_SPEED] = speed }
    }

    suspend fun setOpenRouterKey(key: String) {
        context.dataStore.edit { it[KEY_OPENROUTER_KEY] = key.trim() }
    }

    suspend fun setElevenLabsKey(key: String) {
        context.dataStore.edit { it[KEY_ELEVENLABS_KEY] = key.trim() }
    }

    suspend fun setWakeWordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WAKE_WORD_ENABLED] = enabled }
    }

    suspend fun setPersonalityStyle(style: PersonalityStyle) {
        context.dataStore.edit { it[KEY_PERSONALITY_STYLE] = style.name }
    }

    suspend fun setLanguageMode(mode: LanguageMode) {
        context.dataStore.edit { it[KEY_LANGUAGE_MODE] = mode.name }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setContinuousConversation(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CONTINUOUS_CONVERSATION] = enabled }
    }

    private fun getBuildConfigKey(name: String): String {
        return try {
            val field = BuildConfig::class.java.getField(name)
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
