package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WalkRecord(
    val id: Long = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val distanceMeters: Float? = null,
    val note: String? = null
) : Parcelable
