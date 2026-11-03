package com.example.data.remote

import com.example.model.AiModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface OpenRouterService {
    @POST("api/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://github.com/rivon-ai",
        @Header("X-Title") title: String = "RIVON AI Assistant",
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @GET("api/v1/models")
    suspend fun getModels(
        @Header("Authorization") authorization: String? = null
    ): Response<OpenRouterModelsResponse>
}

class OpenRouterRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openrouter.ai/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(OpenRouterService::class.java)

    suspend fun fetchAvailableModels(apiKey: String): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        try {
            val authHeader = if (apiKey.isNotBlank()) "Bearer $apiKey" else null
            val response = service.getModels(authHeader)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.data.map { item ->
                    val isFree = item.id.endsWith(":free") ||
                            (item.pricing?.prompt == "0" && item.pricing.completion == "0")
                    val provider = item.id.split("/").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "OpenRouter"
                    AiModel(
                        id = item.id,
                        name = item.name ?: item.id,
                        provider = provider,
                        isFree = isFree,
                        contextLength = item.contextLength ?: 32768,
                        description = item.description ?: ""
                    )
                }
                // Sort with free models first, then popularity/name
                val sorted = list.sortedWith(
                    compareByDescending<AiModel> { it.isFree }
                        .thenBy { it.provider }
                        .thenBy { it.name }
                )
                Result.success(sorted)
            } else {
                // Fallback to rich defaults
                Result.success(AiModel.ALL_DEFAULTS)
            }
        } catch (e: Exception) {
            Result.success(AiModel.ALL_DEFAULTS)
        }
    }

    suspend fun sendChatCompletion(
        apiKey: String,
        modelId: String,
        messages: List<ChatMessageDto>,
        allowFallback: Boolean = true
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("OpenRouter API key is missing. Please configure it in Settings."))
        }

        val authHeader = "Bearer $apiKey"

        // Build candidate models: selected model first, followed by reliable free models
        val candidateModels = mutableListOf(modelId)
        if (allowFallback) {
            val fallbackFree = listOf(
                "meta-llama/llama-3.3-70b-instruct:free",
                "google/gemini-2.0-flash-exp:free",
                "mistralai/mistral-small-24b-instruct-2501:free",
                "deepseek/deepseek-r1:free",
                "qwen/qwen-2.5-72b-instruct:free"
            )
            for (m in fallbackFree) {
                if (m !in candidateModels) candidateModels.add(m)
            }
        }

        var lastError: Exception? = null

        for (candidate in candidateModels) {
            try {
                val request = ChatCompletionRequest(
                    model = candidate,
                    messages = messages
                )
                val response = service.createChatCompletion(
                    authorization = authHeader,
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val choice = body.choices.firstOrNull()
                    val text = choice?.message?.content
                    if (!text.isNullOrBlank()) {
                        return@withContext Result.success(Pair(text, candidate))
                    } else if (body.error != null) {
                        lastError = Exception(body.error.message ?: "OpenRouter API error code: ${body.error.code}")
                    }
                } else {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string() ?: ""
                    lastError = Exception("OpenRouter request failed ($code): $errorBody")
                }
            } catch (e: Exception) {
                lastError = e
            }
            // If failed and fallback allowed, try next model
            if (!allowFallback) break
        }

        Result.failure(lastError ?: Exception("Unknown error calling OpenRouter AI."))
    }
}
