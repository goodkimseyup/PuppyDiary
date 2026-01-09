package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walk_records")
data class WalkRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val puppyId: Long = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val distanceMeters: Float? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
