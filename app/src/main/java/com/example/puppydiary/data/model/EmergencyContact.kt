package com.example.puppydiary.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmergencyContact(
    val id: Long = 0,
    val contactType: String,
    val name: String,
    val phoneNumber: String,
    val address: String? = null,
    val note: String? = null
) : Parcelable

enum class ContactType(val displayName: String) {
    HOSPITAL("동물병원"),
    PET_SITTER("펫시터"),
    GROOMING("미용실"),
    EMERGENCY("응급병원"),
    OTHER("기타")
}
