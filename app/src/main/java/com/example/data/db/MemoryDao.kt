package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NexusMemory
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM nexus_memories ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<NexusMemory>>

    @Query("SELECT * FROM nexus_memories WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveMemoriesList(): List<NexusMemory>

    @Query("SELECT COUNT(*) FROM nexus_memories")
    suspend fun getMemoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: NexusMemory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<NexusMemory>)

    @Update
    suspend fun updateMemory(memory: NexusMemory)

    @Query("DELETE FROM nexus_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM nexus_memories")
    suspend fun deleteAllMemories()
}
