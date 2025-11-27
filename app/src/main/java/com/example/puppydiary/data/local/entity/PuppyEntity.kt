package com.example.puppydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puppy")
data class PuppyEntity(
    @PrimaryKey
    val id: Long = 1L, // 단일 강아지만 관리
    val name: String,
    val breed: String,
    val birthDate: String,
    val profileImage: String? = null
)
