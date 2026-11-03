package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<ChatMessageDto>,
    @Json(name = "temperature") val temperature: Double = 0.7,
    @Json(name = "max_tokens") val maxTokens: Int = 1000
)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "choices") val choices: List<ChoiceDto> = emptyList(),
    @Json(name = "model") val model: String? = null,
    @Json(name = "error") val error: OpenRouterErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class ChoiceDto(
    @Json(name = "message") val message: ChatMessageDto? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterErrorDto(
    @Json(name = "message") val message: String? = null,
    @Json(name = "code") val code: Int? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterModelsResponse(
    @Json(name = "data") val data: List<OpenRouterModelItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenRouterModelItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "context_length") val contextLength: Int? = 0,
    @Json(name = "pricing") val pricing: ModelPricingDto? = null
)

@JsonClass(generateAdapter = true)
data class ModelPricingDto(
    @Json(name = "prompt") val prompt: String? = null,
    @Json(name = "completion") val completion: String? = null
)
