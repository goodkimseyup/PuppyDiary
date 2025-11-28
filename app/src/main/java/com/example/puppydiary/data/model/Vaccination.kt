package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vaccination(
    val id: Long = 0,
    val date: String,
    val vaccine: String,
    val nextDate: String,
    val completed: Boolean
) : Parcelable