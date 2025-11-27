package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotoMemory(
    val id: Long,
    val photo: String,
    val date: String,
    val weight: Float? = null,
    val description: String = "",
    val diaryEntryId: Long? = null
) : Parcelable