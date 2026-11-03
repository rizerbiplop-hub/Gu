package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ElevenLabsVoicesResponse(
    @Json(name = "voices") val voices: List<ElevenLabsVoiceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ElevenLabsVoiceDto(
    @Json(name = "voice_id") val voiceId: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "preview_url") val previewUrl: String? = null,
    @Json(name = "labels") val labels: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class ElevenLabsTtsRequest(
    @Json(name = "text") val text: String,
    @Json(name = "model_id") val modelId: String = "eleven_multilingual_v2",
    @Json(name = "voice_settings") val voiceSettings: ElevenLabsVoiceSettingsDto? = null
)

@JsonClass(generateAdapter = true)
data class ElevenLabsVoiceSettingsDto(
    @Json(name = "stability") val stability: Float = 0.5f,
    @Json(name = "similarity_boost") val similarityBoost: Float = 0.75f,
    @Json(name = "speed") val speed: Float = 1.0f
)
