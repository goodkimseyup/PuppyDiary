package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_records")
data class MealRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val puppyId: Long = 0,
    val date: String,
    val time: String,
    val foodType: String,
    val foodName: String,
    val amountGrams: Float,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
