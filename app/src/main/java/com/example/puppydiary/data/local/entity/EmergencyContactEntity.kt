package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val puppyId: Long = 0,
    val contactType: String,
    val name: String,
    val phoneNumber: String,
    val address: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
