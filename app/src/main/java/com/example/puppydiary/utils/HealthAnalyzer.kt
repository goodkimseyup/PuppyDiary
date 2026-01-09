package com.example.puppydiary.utils

import com.example.puppydiary.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * AI 기반 건강 분석기
 * 체중, 산책, 식사 패턴을 분석하여 건강 상태를 예측합니다.
 */
object HealthAnalyzer {

    data class HealthReport(
        val overallScore: Int,              // 0-100 전체 건강 점수
        val weightScore: Int,               // 체중 점수
        val activityScore: Int,             // 활동량 점수
        val nutritionScore: Int,            // 영양 점수
        val alerts: List<HealthAlert>,      // 건강 알림
        val recommendations: List<String>,  // 추천사항
        val weightTrend: WeightTrend,       // 체중 트렌드
        val activityTrend: ActivityTrend    // 활동량 트렌드
    )

    data class HealthAlert(
        val type: AlertType,
        val severity: Severity,
        val title: String,
        val message: String,
        val icon: String
    )

    enum class AlertType {
        WEIGHT_GAIN, WEIGHT_LOSS, LOW_ACTIVITY, IRREGULAR_MEALS,
        MISSED_VACCINATION, MEDICATION_DUE, GOOD_PROGRESS
    }

    enum class Severity {
        INFO, WARNING, CRITICAL
    }

    enum class WeightTrend {
        INCREASING, STABLE, DECREASING, FLUCTUATING, INSUFFICIENT_DATA
    }

    enum class ActivityTrend {
        VERY_ACTIVE, ACTIVE, MODERATE, LOW, INSUFFICIENT_DATA
    }

    /**
     * 종합 건강 리포트 생성
     */
    fun analyzeHealth(
        puppy: PuppyData,
        weightRecords: List<WeightRecord>,
        walkRecords: List<WalkRecord>,
        mealRecords: List<MealRecord>,
        vaccinations: List<Vaccination>,
        medicationRecords: List<MedicationRecord>
    ): HealthReport {
        val alerts = mutableListOf<HealthAlert>()
        val recommendations = mutableListOf<String>()

        // 체중 분석
        val weightAnalysis = analyzeWeight(weightRecords, puppy)
        alerts.addAll(weightAnalysis.alerts)
        recommendations.addAll(weightAnalysis.recommendations)

        // 활동량 분석
        val activityAnalysis = analyzeActivity(walkRecords)
        alerts.addAll(activityAnalysis.alerts)
        recommendations.addAll(activityAnalysis.recommendations)

        // 영양 분석
        val nutritionAnalysis = analyzeNutrition(mealRecords)
        alerts.addAll(nutritionAnalysis.alerts)
        recommendations.addAll(nutritionAnalysis.recommendations)

        // 예방접종 체크
        val vaccineAlerts = checkVaccinations(vaccinations)
        alerts.addAll(vaccineAlerts)

        // 투약 체크
        val medicationAlerts = checkMedications(medicationRecords)
        alerts.addAll(medicationAlerts)

        // 좋은 상태면 칭찬 알림 추가
        if (alerts.none { it.severity == Severity.WARNING || it.severity == Severity.CRITICAL }) {
            alerts.add(
                HealthAlert(
                    type = AlertType.GOOD_PROGRESS,
                    severity = Severity.INFO,
                    title = "잘 하고 있어요!",
                    message = "${puppy.name}의 건강 상태가 양호합니다.",
                    icon = "🎉"
                )
            )
        }

        // 전체 점수 계산
        val overallScore = calculateOverallScore(
            weightAnalysis.score,
            activityAnalysis.score,
            nutritionAnalysis.score
        )

        return HealthReport(
            overallScore = overallScore,
            weightScore = weightAnalysis.score,
            activityScore = activityAnalysis.score,
            nutritionScore = nutritionAnalysis.score,
            alerts = alerts.sortedByDescending { it.severity.ordinal },
            recommendations = recommendations.distinct(),
            weightTrend = weightAnalysis.trend,
            activityTrend = activityAnalysis.trend
        )
    }

    // ==================== 체중 분석 ====================

    private data class WeightAnalysisResult(
        val score: Int,
        val trend: WeightTrend,
        val alerts: List<HealthAlert>,
        val recommendations: List<String>
    )

    private fun analyzeWeight(records: List<WeightRecord>, puppy: PuppyData): WeightAnalysisResult {
        val alerts = mutableListOf<HealthAlert>()
        val recommendations = mutableListOf<String>()

        if (records.size < 2) {
            return WeightAnalysisResult(
                score = 50,
                trend = WeightTrend.INSUFFICIENT_DATA,
                alerts = listOf(
                    HealthAlert(
                        type = AlertType.WEIGHT_LOSS,
                        severity = Severity.INFO,
                        title = "체중 기록 부족",
                        message = "정확한 분석을 위해 체중을 주기적으로 기록해주세요.",
                        icon = "📊"
                    )
                ),
                recommendations = listOf("일주일에 한 번 체중을 측정해주세요")
            )
        }

        val recentRecords = records.takeLast(10)
        val latestWeight = recentRecords.last().weight
        val previousWeight = recentRecords.first().weight
        val weightChange = latestWeight - previousWeight
        val changePercent = (weightChange / previousWeight) * 100

        // 체중 트렌드 분석
        val trend = when {
            recentRecords.size < 3 -> WeightTrend.INSUFFICIENT_DATA
            abs(changePercent) < 2 -> WeightTrend.STABLE
            changePercent > 5 -> WeightTrend.INCREASING
            changePercent < -5 -> WeightTrend.DECREASING
            else -> {
                // 변동성 체크
                val fluctuation = calculateFluctuation(recentRecords.map { it.weight })
                if (fluctuation > 0.5) WeightTrend.FLUCTUATING else WeightTrend.STABLE
            }
        }

        // 점수 계산
        var score = 80

        when (trend) {
            WeightTrend.STABLE -> {
                score = 100
                recommendations.add("${puppy.name}의 체중이 안정적으로 유지되고 있어요!")
            }
            WeightTrend.INCREASING -> {
                if (changePercent > 10) {
                    score = 50
                    alerts.add(
                        HealthAlert(
                            type = AlertType.WEIGHT_GAIN,
                            severity = Severity.WARNING,
                            title = "급격한 체중 증가",
                            message = "최근 ${String.format("%.1f", changePercent)}% 체중이 증가했습니다.",
                            icon = "⚠️"
                        )
                    )
                    recommendations.add("산책 시간을 늘려보세요")
                    recommendations.add("간식 양을 줄여보세요")
                } else {
                    score = 75
                    recommendations.add("체중이 조금씩 늘고 있어요. 식단 관리에 신경써주세요.")
                }
            }
            WeightTrend.DECREASING -> {
                if (changePercent < -10) {
                    score = 40
                    alerts.add(
                        HealthAlert(
                            type = AlertType.WEIGHT_LOSS,
                            severity = Severity.CRITICAL,
                            title = "급격한 체중 감소",
                            message = "최근 ${String.format("%.1f", abs(changePercent))}% 체중이 감소했습니다. 수의사 상담을 권장합니다.",
                            icon = "🚨"
                        )
                    )
                    recommendations.add("동물병원 방문을 권장합니다")
                } else {
                    score = 65
                    recommendations.add("체중이 조금씩 줄고 있어요. 식사량을 확인해주세요.")
                }
            }
            WeightTrend.FLUCTUATING -> {
                score = 60
                alerts.add(
                    HealthAlert(
                        type = AlertType.WEIGHT_GAIN,
                        severity = Severity.WARNING,
                        title = "불안정한 체중",
                        message = "체중 변동이 심합니다. 규칙적인 식사가 필요합니다.",
                        icon = "📈"
                    )
                )
                recommendations.add("규칙적인 시간에 일정량의 식사를 제공해주세요")
            }
            WeightTrend.INSUFFICIENT_DATA -> score = 50
        }

        return WeightAnalysisResult(score, trend, alerts, recommendations)
    }

    // ==================== 활동량 분석 ====================

    private data class ActivityAnalysisResult(
        val score: Int,
        val trend: ActivityTrend,
        val alerts: List<HealthAlert>,
        val recommendations: List<String>
    )

    private fun analyzeActivity(walkRecords: List<WalkRecord>): ActivityAnalysisResult {
        val alerts = mutableListOf<HealthAlert>()
        val recommendations = mutableListOf<String>()

        // 최근 7일 산책 기록
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time
        val sevenDaysAgoStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sevenDaysAgo)

        val recentWalks = walkRecords.filter { it.date >= sevenDaysAgoStr }
        val totalMinutes = recentWalks.sumOf { it.durationMinutes }
        val avgMinutesPerDay = totalMinutes / 7f
        val walkDays = recentWalks.map { it.date }.distinct().size

        val trend = when {
            avgMinutesPerDay >= 60 -> ActivityTrend.VERY_ACTIVE
            avgMinutesPerDay >= 40 -> ActivityTrend.ACTIVE
            avgMinutesPerDay >= 20 -> ActivityTrend.MODERATE
            else -> ActivityTrend.LOW
        }

        var score = when (trend) {
            ActivityTrend.VERY_ACTIVE -> 100
            ActivityTrend.ACTIVE -> 85
            ActivityTrend.MODERATE -> 65
            ActivityTrend.LOW -> 40
            ActivityTrend.INSUFFICIENT_DATA -> 50
        }

        if (trend == ActivityTrend.LOW) {
            alerts.add(
                HealthAlert(
                    type = AlertType.LOW_ACTIVITY,
                    severity = Severity.WARNING,
                    title = "활동량 부족",
                    message = "최근 7일간 하루 평균 ${avgMinutesPerDay.toInt()}분만 산책했어요.",
                    icon = "🚶"
                )
            )
            recommendations.add("하루 30분 이상 산책을 권장합니다")
            recommendations.add("실내 놀이 시간을 늘려보세요")
        }

        if (walkDays < 4) {
            recommendations.add("산책 횟수를 늘려보세요 (주 5일 이상 권장)")
        }

        if (trend == ActivityTrend.VERY_ACTIVE) {
            recommendations.add("산책을 열심히 하고 있어요! 계속 유지해주세요 💪")
        }

        return ActivityAnalysisResult(score, trend, alerts, recommendations)
    }

    // ==================== 영양 분석 ====================

    private data class NutritionAnalysisResult(
        val score: Int,
        val alerts: List<HealthAlert>,
        val recommendations: List<String>
    )

    private fun analyzeNutrition(mealRecords: List<MealRecord>): NutritionAnalysisResult {
        val alerts = mutableListOf<HealthAlert>()
        val recommendations = mutableListOf<String>()

        // 최근 7일 식사 기록
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time
        val sevenDaysAgoStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sevenDaysAgo)

        val recentMeals = mealRecords.filter { it.date >= sevenDaysAgoStr }
        val mealDays = recentMeals.map { it.date }.distinct().size
        val avgMealsPerDay = if (mealDays > 0) recentMeals.size.toFloat() / mealDays else 0f

        var score = 70

        when {
            mealDays == 0 -> {
                score = 50
                recommendations.add("식사 기록을 시작해보세요")
            }
            avgMealsPerDay < 1.5 -> {
                score = 55
                alerts.add(
                    HealthAlert(
                        type = AlertType.IRREGULAR_MEALS,
                        severity = Severity.WARNING,
                        title = "불규칙한 식사",
                        message = "하루 평균 ${String.format("%.1f", avgMealsPerDay)}회 식사 기록이 있어요.",
                        icon = "🍽️"
                    )
                )
                recommendations.add("하루 2~3회 규칙적인 식사를 권장합니다")
            }
            avgMealsPerDay in 1.5..3.0 -> {
                score = 90
                recommendations.add("식사가 규칙적이에요! 잘 하고 있어요 👍")
            }
            avgMealsPerDay > 3.5 -> {
                score = 70
                recommendations.add("간식이 너무 잦을 수 있어요. 식사량을 조절해보세요.")
            }
        }

        return NutritionAnalysisResult(score, alerts, recommendations)
    }

    // ==================== 예방접종 체크 ====================

    private fun checkVaccinations(vaccinations: List<Vaccination>): List<HealthAlert> {
        val alerts = mutableListOf<HealthAlert>()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        vaccinations.filter { !it.completed && it.nextDate.isNotBlank() }.forEach { vaccine ->
            try {
                val nextDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(vaccine.nextDate)
                val daysUntil = ((nextDate?.time ?: 0) - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)

                when {
                    daysUntil < 0 -> {
                        alerts.add(
                            HealthAlert(
                                type = AlertType.MISSED_VACCINATION,
                                severity = Severity.CRITICAL,
                                title = "접종 기한 지남",
                                message = "${vaccine.vaccine} 접종일이 지났습니다!",
                                icon = "💉"
                            )
                        )
                    }
                    daysUntil <= 7 -> {
                        alerts.add(
                            HealthAlert(
                                type = AlertType.MISSED_VACCINATION,
                                severity = Severity.WARNING,
                                title = "접종 예정",
                                message = "${vaccine.vaccine} 접종이 ${daysUntil.toInt()}일 남았습니다.",
                                icon = "💉"
                            )
                        )
                    }
                }
            } catch (e: Exception) { }
        }

        return alerts
    }

    // ==================== 투약 체크 ====================

    private fun checkMedications(medications: List<MedicationRecord>): List<HealthAlert> {
        val alerts = mutableListOf<HealthAlert>()

        medications.filter { it.nextDate != null }.forEach { med ->
            try {
                val nextDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(med.nextDate!!)
                val daysUntil = ((nextDate?.time ?: 0) - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)

                when {
                    daysUntil < 0 -> {
                        alerts.add(
                            HealthAlert(
                                type = AlertType.MEDICATION_DUE,
                                severity = Severity.CRITICAL,
                                title = "투약 기한 지남",
                                message = "${med.medicationName} 투약일이 지났습니다!",
                                icon = "💊"
                            )
                        )
                    }
                    daysUntil <= 3 -> {
                        alerts.add(
                            HealthAlert(
                                type = AlertType.MEDICATION_DUE,
                                severity = Severity.WARNING,
                                title = "투약 예정",
                                message = "${med.medicationName} 투약이 ${daysUntil.toInt()}일 남았습니다.",
                                icon = "💊"
                            )
                        )
                    }
                }
            } catch (e: Exception) { }
        }

        return alerts
    }

    // ==================== 유틸리티 ====================

    private fun calculateFluctuation(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val avg = values.average().toFloat()
        return values.map { abs(it - avg) }.average().toFloat()
    }

    private fun calculateOverallScore(weightScore: Int, activityScore: Int, nutritionScore: Int): Int {
        // 가중치: 체중 40%, 활동량 35%, 영양 25%
        return ((weightScore * 0.4) + (activityScore * 0.35) + (nutritionScore * 0.25)).toInt()
    }

    /**
     * 건강 점수에 따른 등급 반환
     */
    fun getHealthGrade(score: Int): String {
        return when {
            score >= 90 -> "A+"
            score >= 80 -> "A"
            score >= 70 -> "B+"
            score >= 60 -> "B"
            score >= 50 -> "C"
            else -> "D"
        }
    }

    /**
     * 건강 점수에 따른 색상 반환
     */
    fun getHealthColor(score: Int): Long {
        return when {
            score >= 80 -> 0xFF4CAF50  // Green
            score >= 60 -> 0xFFFFC107  // Yellow
            score >= 40 -> 0xFFFF9800  // Orange
            else -> 0xFFF44336         // Red
        }
    }
}
