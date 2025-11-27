package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puppy")
data class PuppyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val breed: String,
    val birthDate: String,
    val profileImage: String? = null,
    val isSelected: Boolean = false // 현재 선택된 강아지
)
