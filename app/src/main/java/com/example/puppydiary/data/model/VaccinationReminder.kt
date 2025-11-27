package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VaccinationReminder(
    val id: String,
    val vaccineName: String,
    val scheduledDate: String,
    val reminderDate: String,
    val isActive: Boolean = true,
    val notificationId: Int
) : Parcelable