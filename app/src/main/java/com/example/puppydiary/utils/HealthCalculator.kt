package com.example.puppydiary.utils

import com.example.puppydiary.data.model.*

object HealthCalculator {

    fun calculateOverallHealthScore(
        weightRecords: List<WeightRecord>,
        vaccinations: List<Vaccination>,
        diaryEntries: List<DiaryEntry>,
        dateRange: DateRange
    ): Int {
        val weightScore = calculateWeightHealthScore(weightRecords)
        val vaccinationScore = calculateVaccinationScore(vaccinations)
        val activityScore = calculateActivityScore(diaryEntries, dateRange)
        val consistencyScore = calculateConsistencyScore(weightRecords, diaryEntries)

        return ((weightScore + vaccinationScore + activityScore + consistencyScore) / 4.0).toInt()
    }

    private fun calculateWeightHealthScore(weightRecords: List<WeightRecord>): Int {
        if (weightRecords.size < 2) return 50

        val recentRecords = weightRecords.takeLast(5)
        val growthTrend = analyzeGrowthTrend(recentRecords)
        val consistency = analyzeWeightConsistency(recentRecords)

        return when {
            growthTrend > 0.8f && consistency > 0.8f -> 100
            growthTrend > 0.6f && consistency > 0.6f -> 85
            growthTrend > 0.4f && consistency > 0.4f -> 70
            growthTrend > 0.2f || consistency > 0.5f -> 55
            else -> 30
        }
    }

    private fun calculateVaccinationScore(vaccinations: List<Vaccination>): Int {
        if (vaccinations.isEmpty()) return 0

        val completedCount = vaccinations.count { it.completed }
        val completionRate = completedCount.toFloat() / vaccinations.size

        val upcomingVaccinations = vaccinations.filter { !it.completed }
        val overdueCount = upcomingVaccinations.count {
            DateHelper.getDaysBetween(it.nextDate, DateHelper.getCurrentDateString()) > 0
        }

        val baseScore = (completionRate * 100).toInt()
        val penaltyForOverdue = overdueCount * 10

        return (baseScore - penaltyForOverdue).coerceAtLeast(0)
    }

    private fun calculateActivityScore(diaryEntries: List<DiaryEntry>, dateRange: DateRange): Int {
        val activeDays = diaryEntries.size
        val totalDays = dateRange.days
        val activityRate = activeDays.toFloat() / totalDays

        return when {
            activityRate >= 0.7f -> 100
            activityRate >= 0.5f -> 85
            activityRate >= 0.3f -> 70
            activityRate >= 0.2f -> 55
            activityRate >= 0.1f -> 40
            else -> 20
        }
    }

    private fun calculateConsistencyScore(
        weightRecords: List<WeightRecord>,
        diaryEntries: List<DiaryEntry>
    ): Int {
        val weightConsistency = calculateRecordConsistency(weightRecords.map { it.date })
        val diaryConsistency = calculateRecordConsistency(diaryEntries.map { it.date })

        return ((weightConsistency + diaryConsistency) / 2 * 100).toInt()
    }

    private fun analyzeGrowthTrend(weightRecords: List<WeightRecord>): Float {
        if (weightRecords.size < 2) return 0.5f

        val weights = weightRecords.map { it.weight }
        val trend = weights.zipWithNext { a, b -> if (b >= a) 1f else 0f }

        return if (trend.isNotEmpty()) trend.average().toFloat() else 0.5f
    }

    private fun analyzeWeightConsistency(weightRecords: List<WeightRecord>): Float {
        if (weightRecords.size < 3) return 1f

        val weights = weightRecords.map { it.weight }
        val mean = weights.average()
        val variance = weights.map { (it - mean) * (it - mean) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)

        return when {
            standardDeviation < 0.1 -> 1f
            standardDeviation < 0.2 -> 0.8f
            standardDeviation < 0.5 -> 0.6f
            standardDeviation < 1.0 -> 0.4f
            else -> 0.2f
        }
    }

    private fun calculateRecordConsistency(dates: List<String>): Float {
        if (dates.size < 2) return 1f

        val sortedDates = dates.sorted()
        val intervals = mutableListOf<Int>()

        for (i in 1 until sortedDates.size) {
            val interval = DateHelper.getDaysBetween(sortedDates[i-1], sortedDates[i])
            intervals.add(interval)
        }

        if (intervals.isEmpty()) return 1f

        val averageInterval = intervals.average()
        val variance = intervals.map { (it - averageInterval) * (it - averageInterval) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)

        return when {
            standardDeviation < 2 -> 1f
            standardDeviation < 5 -> 0.8f
            standardDeviation < 10 -> 0.6f
            standardDeviation < 20 -> 0.4f
            else -> 0.2f
        }
    }

    fun generateHealthRecommendations(
        healthScore: Int,
        weightRecords: List<WeightRecord>,
        vaccinations: List<Vaccination>,
        diaryEntries: List<DiaryEntry>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when {
            healthScore >= 90 -> recommendations.add("🎉 매우 건강합니다! 현재 관리를 계속 유지하세요.")
            healthScore >= 70 -> recommendations.add("👍 건강 상태가 좋습니다. 꾸준한 관리가 중요해요.")
            healthScore >= 50 -> recommendations.add("⚠️ 건강 관리에 더 신경쓰시기 바랍니다.")
            else -> recommendations.add("🚨 건강 상태 개선이 필요합니다. 수의사와 상담하세요.")
        }

        if (weightRecords.size >= 2) {
            val recentGrowth = weightRecords.takeLast(2).let {
                it.last().weight - it.first().weight
            }
            when {
                recentGrowth > 0.5f -> recommendations.add("📈 빠른 성장을 보이고 있어요. 영양 상태가 좋습니다!")
                recentGrowth < -0.2f -> recommendations.add("📉 체중이 감소했습니다. 식사량을 확인해보세요.")
                recentGrowth in -0.2f..0.1f -> recommendations.add("⚖️ 체중이 안정적입니다.")
            }
        }

        val overdueVaccinations = vaccinations.filter {
            !it.completed && DateHelper.getDaysBetween(it.nextDate, DateHelper.getCurrentDateString()) > 0
        }
        if (overdueVaccinations.isNotEmpty()) {
            recommendations.add("💉 예정일이 지난 예방접종이 있습니다: ${overdueVaccinations.joinToString(", ") { it.vaccine }}")
        }

        val upcomingVaccinations = vaccinations.filter {
            !it.completed && DateHelper.getDaysBetween(DateHelper.getCurrentDateString(), it.nextDate) in 1..7
        }
        if (upcomingVaccinations.isNotEmpty()) {
            recommendations.add("📅 다음 주에 예정된 예방접종: ${upcomingVaccinations.joinToString(", ") { it.vaccine }}")
        }

        val recentDiaries = diaryEntries.filter {
            DateHelper.getDaysBetween(it.date, DateHelper.getCurrentDateString()) <= 7
        }
        when {
            recentDiaries.isEmpty() -> recommendations.add("📝 최근 일주일간 활동 기록이 없습니다. 일기를 작성해보세요!")
            recentDiaries.size < 3 -> recommendations.add("🎯 더 많은 활동 기록을 남겨보세요. 건강 관리에 도움이 됩니다.")
            recentDiaries.size >= 5 -> recommendations.add("🌟 활발한 활동 기록을 남기고 있어요. 훌륭합니다!")
        }

        return recommendations
    }
}