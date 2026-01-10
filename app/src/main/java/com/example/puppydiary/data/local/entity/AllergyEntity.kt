package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allergies")
data class AllergyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val puppyId: Long,
    val allergyName: String,      // 알러지 원인 (예: 닭고기, 소고기, 밀 등)
    val severity: String,          // 심각도: mild, moderate, severe
    val symptoms: String,          // 증상
    val diagnosedDate: String,     // 진단일
    val notes: String = ""         // 메모
)
