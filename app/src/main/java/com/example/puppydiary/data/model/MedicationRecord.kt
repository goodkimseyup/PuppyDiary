package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicationRecord(
    val id: Long = 0,
    val date: String,
    val medicationType: String,
    val medicationName: String,
    val nextDate: String? = null,
    val intervalDays: Int? = null,
    val note: String? = null
) : Parcelable

enum class MedicationType(val displayName: String) {
    HEARTWORM("심장사상충"),
    DEWORMER("구충제"),
    FLEA_TICK("벼룩/진드기"),
    OTHER("기타 약")
}
