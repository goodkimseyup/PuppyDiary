package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccination_reminders")
data class VaccinationReminderEntity(
    @PrimaryKey
    val id: String,
    val vaccineName: String,
    val scheduledDate: String,
    val reminderDate: String,
    val isActive: Boolean = true,
    val notificationId: Int
)
