package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val progress: Float = 0f,
    val category: AchievementCategory
) : Parcelable

enum class AchievementCategory {
    WEIGHT, VACCINATION, DIARY, GENERAL
}