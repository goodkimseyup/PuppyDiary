package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthMetrics(
    val date: String,
    val healthScore: Int,
    val weightScore: Int,
    val vaccinationScore: Int,
    val activityScore: Int
) : Parcelable