package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PuppyData(
    val name: String = "꼬미",
    val breed: String = "말티즈",
    val birthDate: String = "2023-03-15",
    val profileImage: String? = null
) : Parcelable