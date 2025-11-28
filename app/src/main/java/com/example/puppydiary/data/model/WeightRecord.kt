package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeightRecord(
    val id: Long = 0,
    val date: String,
    val weight: Float
) : Parcelable