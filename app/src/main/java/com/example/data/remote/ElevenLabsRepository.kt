package com.example.data.remote

import android.content.Context
import com.example.model.VoiceModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

interface ElevenLabsService {
    @GET("v1/voices")
    suspend fun getVoices(
        @Header("xi-api-key") apiKey: String
    ): Response<ElevenLabsVoicesResponse>

    @POST("v1/text-to-speech/{voice_id}")
    @Streaming
    suspend fun textToSpeech(
        @Header("xi-api-key") apiKey: String,
        @Path("voice_id") voiceId: String,
        @Body request: ElevenLabsTtsRequest
    ): Response<ResponseBody>
}

class ElevenLabsRepository(private val context: Context) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.elevenlabs.io/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(ElevenLabsService::class.java)

    suspend fun fetchVoices(apiKey: String): Result<List<VoiceModel>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.success(VoiceModel.POPULAR_VOICES)
        }
        try {
            val response = service.getVoices(apiKey)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.voices.map { dto ->
                    VoiceModel(
                        voiceId = dto.voiceId,
                        name = dto.name,
                        category = dto.category ?: "custom",
                        description = dto.description ?: dto.labels?.get("description") ?: "",
                        previewUrl = dto.previewUrl,
                        accent = dto.labels?.get("accent") ?: "Neutral",
                        gender = dto.labels?.get("gender") ?: "Neutral"
                    )
                }
                Result.success(list)
            } else {
                Result.success(VoiceModel.POPULAR_VOICES)
            }
        } catch (e: Exception) {
            Result.success(VoiceModel.POPULAR_VOICES)
        }
    }

    suspend fun synthesizeSpeechToFile(
        apiKey: String,
        voiceId: String,
        text: String,
        speed: Float = 1.0f
    ): Result<File> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("ElevenLabs API key is empty."))
        }

        val cacheKey = md5("$voiceId:$speed:$text")
        val cacheFile = File(context.cacheDir, "elevenlabs_$cacheKey.mp3")
        if (cacheFile.exists() && cacheFile.length() > 0) {
            return@withContext Result.success(cacheFile)
        }

        try {
            val request = ElevenLabsTtsRequest(
                text = text,
                modelId = "eleven_multilingual_v2",
                voiceSettings = ElevenLabsVoiceSettingsDto(
                    stability = 0.5f,
                    similarityBoost = 0.75f,
                    speed = speed.coerceIn(0.5f, 2.0f)
                )
            )

            val response = service.textToSpeech(
                apiKey = apiKey,
                voiceId = voiceId,
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                FileOutputStream(cacheFile).use { fos ->
                    body.byteStream().use { input ->
                        input.copyTo(fos)
                    }
                }
                Result.success(cacheFile)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Code: ${response.code()}"
                Result.failure(Exception("ElevenLabs TTS failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
