package com.example.model

data class AiModel(
    val id: String,
    val name: String,
    val provider: String,
    val isFree: Boolean,
    val contextLength: Int,
    val description: String = "",
    val isAvailable: Boolean = true
) {
    companion object {
        // High quality models commonly available on OpenRouter, with free models prioritized
        val DEFAULT_FREE_MODELS = listOf(
            AiModel(
                id = "meta-llama/llama-3.3-70b-instruct:free",
                name = "Llama 3.3 70B Instruct (Free)",
                provider = "Meta",
                isFree = true,
                contextLength = 131072,
                description = "State of the art 70B parameter model with exceptional reasoning and speed."
            ),
            AiModel(
                id = "google/gemini-2.0-flash-exp:free",
                name = "Gemini 2.0 Flash (Free)",
                provider = "Google",
                isFree = true,
                contextLength = 1048576,
                description = "Ultra-fast multimodal model with massive context window and native function calling."
            ),
            AiModel(
                id = "mistralai/mistral-small-24b-instruct-2501:free",
                name = "Mistral Small 24B (Free)",
                provider = "Mistral AI",
                isFree = true,
                contextLength = 32768,
                description = "Highly capable, precise, multilingual European AI model."
            ),
            AiModel(
                id = "deepseek/deepseek-r1:free",
                name = "DeepSeek R1 (Free)",
                provider = "DeepSeek",
                isFree = true,
                contextLength = 65536,
                description = "Open reasoning model with advanced chain-of-thought analysis."
            ),
            AiModel(
                id = "qwen/qwen-2.5-72b-instruct:free",
                name = "Qwen 2.5 72B Instruct (Free)",
                provider = "Alibaba",
                isFree = true,
                contextLength = 32768,
                description = "Excels at coding, multilingual commands, and structured device automation."
            )
        )

        val DEFAULT_PREMIUM_MODELS = listOf(
            AiModel(
                id = "anthropic/claude-3.5-sonnet",
                name = "Claude 3.5 Sonnet",
                provider = "Anthropic",
                isFree = false,
                contextLength = 200000,
                description = "Industry standard for natural conversation, intelligence, and tool usage."
            ),
            AiModel(
                id = "openai/gpt-4o",
                name = "GPT-4o",
                provider = "OpenAI",
                isFree = false,
                contextLength = 128000,
                description = "High-speed flagship multimodal omni model."
            )
        )

        val ALL_DEFAULTS = DEFAULT_FREE_MODELS + DEFAULT_PREMIUM_MODELS
    }
}
