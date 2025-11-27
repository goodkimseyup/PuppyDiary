package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val vaccine: String,
    val nextDate: String,
    val completed: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
