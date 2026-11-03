package com.example.engine

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.remote.ElevenLabsRepository
import com.example.model.VoiceEngineType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class AndroidVoiceEngine(
    private val context: Context,
    private val elevenLabsRepository: ElevenLabsRepository,
    private val scope: CoroutineScope
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isTtsInitialized = false

    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeVoiceEngine = MutableStateFlow(VoiceEngineType.ELEVENLABS)
    val activeVoiceEngine: StateFlow<VoiceEngineType> = _activeVoiceEngine.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    // Callbacks
    var onSpeechFinalResult: ((String) -> Unit)? = null

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                isTtsInitialized = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun startListening() {
        // Stop any speech playing first (interruptibility)
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _speechError.value = "Speech recognition is not available on this device"
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _transcribedText.value = ""
                        _speechError.value = null
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRmsLevel.value = (rmsdB + 2f).coerceIn(0f, 10f) / 10f
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timed out."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Speech recognition error ($error)"
                        }
                        if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                            _speechError.value = message
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _transcribedText.value = text
                            onSpeechFinalResult?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            _transcribedText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _speechError.value = "Failed to start listening: ${e.message}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        _isListening.value = false
    }

    fun speak(
        text: String,
        preferredEngine: VoiceEngineType,
        apiKey: String,
        voiceId: String,
        speed: Float = 1.0f,
        onComplete: (() -> Unit)? = null
    ) {
        // Interrupt previous speech
        stopSpeaking()

        // Strip action code block or tags from spoken text
        val cleanedText = cleanSpokenText(text)
        if (cleanedText.isBlank()) {
            onComplete?.invoke()
            return
        }

        if (preferredEngine == VoiceEngineType.ELEVENLABS && apiKey.isNotBlank()) {
            _activeVoiceEngine.value = VoiceEngineType.ELEVENLABS
            _isSpeaking.value = true

            scope.launch {
                val result = elevenLabsRepository.synthesizeSpeechToFile(
                    apiKey = apiKey,
                    voiceId = voiceId,
                    text = cleanedText,
                    speed = speed
                )

                result.onSuccess { audioFile ->
                    playAudioFile(audioFile, onComplete)
                }.onFailure {
                    // Seamless fallback to Android TTS
                    withContext(Dispatchers.Main) {
                        fallbackToAndroidTts(cleanedText, onComplete)
                    }
                }
            }
        } else {
            fallbackToAndroidTts(cleanedText, onComplete)
        }
    }

    private fun playAudioFile(file: File, onComplete: (() -> Unit)?) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    _isSpeaking.value = true
                    start()
                }
                setOnCompletionListener {
                    _isSpeaking.value = false
                    onComplete?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    _isSpeaking.value = false
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _isSpeaking.value = false
        }
    }

    private fun fallbackToAndroidTts(text: String, onComplete: (() -> Unit)?) {
        _activeVoiceEngine.value = VoiceEngineType.ANDROID_TTS
        if (isTtsInitialized && textToSpeech != null) {
            _isSpeaking.value = true
            val utteranceId = "rivon_tts_${System.currentTimeMillis()}"
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            _isSpeaking.value = false
            onComplete?.invoke()
        }
    }

    fun stopSpeaking() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // ignore
        }

        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            // ignore
        }

        _isSpeaking.value = false
    }

    private fun cleanSpokenText(input: String): String {
        return input
            .replace(Regex("```action[\\s\\S]*?```"), "")
            .replace(Regex("```json[\\s\\S]*?```"), "")
            .replace(Regex("```[\\s\\S]*?```"), "")
            .replace(Regex("[*#_`~]"), "")
            .trim()
    }

    fun destroy() {
        stopSpeaking()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
