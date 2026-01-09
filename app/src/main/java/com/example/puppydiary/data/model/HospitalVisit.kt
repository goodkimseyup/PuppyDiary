package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HospitalVisit(
    val id: Long = 0,
    val date: String,
    val hospitalName: String,
    val visitReason: String,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val prescription: String? = null,
    val cost: Int? = null,
    val nextVisitDate: String? = null,
    val note: String? = null
) : Parcelable
