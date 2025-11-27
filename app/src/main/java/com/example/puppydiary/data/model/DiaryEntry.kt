package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryEntry(
    val id: Long,
    val date: String,
    val title: String,
    val content: String,
    val photo: String? = null
) : Parcelable