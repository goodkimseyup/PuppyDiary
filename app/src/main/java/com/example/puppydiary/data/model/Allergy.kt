package com.example.puppydiary.data.model

data class Allergy(
    val id: Long = 0,
    val puppyId: Long,
    val allergyName: String,
    val severity: String,       // mild, moderate, severe
    val symptoms: String,
    val diagnosedDate: String,
    val notes: String = ""
) {
    fun getSeverityText(): String = when (severity) {
        "mild" -> "경미"
        "moderate" -> "보통"
        "severe" -> "심각"
        else -> severity
    }

    fun getSeverityEmoji(): String = when (severity) {
        "mild" -> "🟡"
        "moderate" -> "🟠"
        "severe" -> "🔴"
        else -> "⚪"
    }
}
