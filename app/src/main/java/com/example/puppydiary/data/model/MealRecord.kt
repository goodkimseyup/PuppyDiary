package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MealRecord(
    val id: Long = 0,
    val date: String,
    val time: String,
    val foodType: String,
    val foodName: String,
    val amountGrams: Float,
    val note: String? = null
) : Parcelable

enum class FoodType(val displayName: String) {
    DRY_FOOD("건식 사료"),
    WET_FOOD("습식 사료"),
    TREATS("간식"),
    HOMEMADE("자연식"),
    OTHER("기타")
}
