package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_records")
data class MedicationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val puppyId: Long = 0,
    val date: String,
    val medicationType: String,
    val medicationName: String,
    val nextDate: String? = null,
    val intervalDays: Int? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
