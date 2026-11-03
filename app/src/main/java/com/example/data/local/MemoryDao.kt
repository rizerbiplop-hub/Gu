package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSessionMessages(sessionId: String): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationEntity): Long

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun clearAllMessages()

    @Query("SELECT * FROM saved_facts ORDER BY timestamp DESC")
    fun getAllFacts(): Flow<List<SavedFactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: SavedFactEntity): Long

    @Query("DELETE FROM saved_facts WHERE id = :id")
    suspend fun deleteFactById(id: Long)

    @Query("DELETE FROM saved_facts")
    suspend fun clearAllFacts()
}
