package com.example.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val plannedAction: PlannedAction? = null,
    val voiceAudioPath: String? = null
)

enum class MessageSender {
    USER,
    RIVON,
    SYSTEM
}
