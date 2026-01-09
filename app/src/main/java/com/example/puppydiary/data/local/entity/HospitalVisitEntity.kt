package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospital_visits")
data class HospitalVisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val puppyId: Long = 0,
    val date: String,
    val hospitalName: String,
    val visitReason: String,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val prescription: String? = null,
    val cost: Int? = null,
    val nextVisitDate: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
