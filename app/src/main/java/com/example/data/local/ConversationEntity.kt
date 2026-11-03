package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "RIVON" or "SYSTEM"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionTitle: String? = null,
    val actionStatus: String? = null,
    val sessionId: String = "default"
)

@Entity(tableName = "saved_facts")
data class SavedFactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "general", // "preference", "nickname", "profile"
    val timestamp: Long = System.currentTimeMillis()
)
