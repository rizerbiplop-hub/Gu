package com.example.model

data class VoiceModel(
    val voiceId: String,
    val name: String,
    val category: String = "premade",
    val description: String = "",
    val previewUrl: String? = null,
    val accent: String = "Neutral",
    val gender: String = "Male"
) {
    companion object {
        // Official popular ElevenLabs voices
        val POPULAR_VOICES = listOf(
            VoiceModel(
                voiceId = "21m00Tcm4TlvDq8ikWAM",
                name = "Rachel",
                category = "premade",
                description = "Calm, professional, natural assistant voice",
                accent = "American",
                gender = "Female"
            ),
            VoiceModel(
                voiceId = "AZnzlk1XvdvUeBnXmlld",
                name = "Domi",
                category = "premade",
                description = "Energetic, clear and friendly",
                accent = "American",
                gender = "Female"
            ),
            VoiceModel(
                voiceId = "EXAVITQu4vr4xnSDxMaL",
                name = "Bella",
                category = "premade",
                description = "Warm, soft conversational tone",
                accent = "American",
                gender = "Female"
            ),
            VoiceModel(
                voiceId = "ErXwobaYiN019PkySvjV",
                name = "Antoni",
                category = "premade",
                description = "Confident, deep, futuristic JARVIS-style male voice",
                accent = "American",
                gender = "Male"
            ),
            VoiceModel(
                voiceId = "MF3mGyEYCl7XYWbV9V6O",
                name = "Elli",
                category = "premade",
                description = "Crisp, polite and intelligent young voice",
                accent = "American",
                gender = "Female"
            ),
            VoiceModel(
                voiceId = "TxGEqnHWrfWFTfGW9XjX",
                name = "Josh",
                category = "premade",
                description = "Warm, casual and resonant male voice",
                accent = "American",
                gender = "Male"
            ),
            VoiceModel(
                voiceId = "VR6AewLTigWG4xSOukaG",
                name = "Arnold",
                category = "premade",
                description = "Crisp and authoritative narrator voice",
                accent = "American",
                gender = "Male"
            ),
            VoiceModel(
                voiceId = "pNInz6obpgDQGcFmaJgB",
                name = "Adam",
                category = "premade",
                description = "Deep, commanding and modern assistant voice",
                accent = "American",
                gender = "Male"
            )
        )
    }
}

enum class VoiceEngineType {
    ELEVENLABS,
    ANDROID_TTS
}
