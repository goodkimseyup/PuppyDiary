package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_memories")
data class PhotoMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photo: String,
    val date: String,
    val weight: Float? = null,
    val description: String = "",
    val diaryEntryId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
