package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nexus_memories")
data class NexusMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String, // Short label e.g., "User's Name", "Language Preference", "Interests"
    val fact: String, // Detailed fact e.g., "User prefers answers in Hindi and English", "Himanshu"
    val category: String = "General", // "Personal", "Preference", "Work", "General"
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
