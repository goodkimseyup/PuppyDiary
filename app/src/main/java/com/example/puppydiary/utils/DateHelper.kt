package com.example.puppydiary.utils

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MM월 dd일", Locale.getDefault())
    private val fullDisplayDateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())

    fun formatDate(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            date?.let { displayDateFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatFullDate(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            date?.let { fullDisplayDateFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun getCurrentDateString(): String {
        return dateFormat.format(Date())
    }

    fun addDays(dateString: String, days: Int): String {
        return try {
            val date = dateFormat.parse(dateString) ?: return dateString
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, days)
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getDaysBetween(startDate: String, endDate: String): Int {
        return try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            if (start != null && end != null) {
                val diffInMillis = end.time - start.time
                (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    fun isDateInRange(date: String, startDate: String, endDate: String): Boolean {
        return date >= startDate && date <= endDate
    }
}