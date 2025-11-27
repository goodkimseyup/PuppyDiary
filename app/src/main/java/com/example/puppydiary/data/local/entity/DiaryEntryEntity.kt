package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val title: String,
    val content: String,
    val photo: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
