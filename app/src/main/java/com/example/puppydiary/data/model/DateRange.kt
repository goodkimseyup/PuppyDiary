package com.example.puppydiary.data.model

enum class DateRange(val displayName: String, val days: Int) {
    WEEK("주간", 7),
    MONTH("월간", 30),
    THREE_MONTHS("3개월", 90),
    YEAR("연간", 365)
}