package com.example.puppydiary.utils

import com.example.puppydiary.data.local.entity.AchievementEntity
import com.example.puppydiary.data.model.AchievementCategory

/**
 * 업적 관리자 - 업적 정의 및 달성 조건 체크
 */
object AchievementManager {

    // 모든 업적 정의
    fun getDefaultAchievements(): List<AchievementEntity> = listOf(
        // 일기 관련 업적
        AchievementEntity(
            id = "diary_first",
            title = "첫 일기",
            description = "첫 번째 일기를 작성했어요!",
            icon = "diary",
            category = AchievementCategory.DIARY.name
        ),
        AchievementEntity(
            id = "diary_10",
            title = "일기 마스터",
            description = "일기 10개를 작성했어요!",
            icon = "diary",
            category = AchievementCategory.DIARY.name
        ),
        AchievementEntity(
            id = "diary_30",
            title = "일기 장인",
            description = "일기 30개를 작성했어요!",
            icon = "diary",
            category = AchievementCategory.DIARY.name
        ),
        AchievementEntity(
            id = "diary_100",
            title = "일기 달인",
            description = "일기 100개를 작성했어요!",
            icon = "diary",
            category = AchievementCategory.DIARY.name
        ),

        // 체중 관련 업적
        AchievementEntity(
            id = "weight_first",
            title = "첫 체중 기록",
            description = "첫 번째 체중을 기록했어요!",
            icon = "scale",
            category = AchievementCategory.WEIGHT.name
        ),
        AchievementEntity(
            id = "weight_10",
            title = "꾸준한 관리",
            description = "체중을 10번 기록했어요!",
            icon = "scale",
            category = AchievementCategory.WEIGHT.name
        ),
        AchievementEntity(
            id = "weight_30",
            title = "건강 지킴이",
            description = "체중을 30번 기록했어요!",
            icon = "scale",
            category = AchievementCategory.WEIGHT.name
        ),

        // 예방접종 관련 업적
        AchievementEntity(
            id = "vaccine_first",
            title = "첫 접종",
            description = "첫 번째 접종을 기록했어요!",
            icon = "vaccine",
            category = AchievementCategory.VACCINATION.name
        ),
        AchievementEntity(
            id = "vaccine_complete",
            title = "접종 완료",
            description = "접종을 완료했어요!",
            icon = "vaccine",
            category = AchievementCategory.VACCINATION.name
        ),
        AchievementEntity(
            id = "vaccine_5",
            title = "예방 전문가",
            description = "5개의 접종을 완료했어요!",
            icon = "vaccine",
            category = AchievementCategory.VACCINATION.name
        ),

        // 사진 관련 업적
        AchievementEntity(
            id = "photo_first",
            title = "첫 사진",
            description = "첫 번째 사진을 추가했어요!",
            icon = "photo",
            category = AchievementCategory.GENERAL.name
        ),
        AchievementEntity(
            id = "photo_10",
            title = "추억 수집가",
            description = "사진 10장을 추가했어요!",
            icon = "photo",
            category = AchievementCategory.GENERAL.name
        ),
        AchievementEntity(
            id = "photo_50",
            title = "포토그래퍼",
            description = "사진 50장을 추가했어요!",
            icon = "photo",
            category = AchievementCategory.GENERAL.name
        ),

        // 산책 관련 업적
        AchievementEntity(
            id = "walk_first",
            title = "첫 산책",
            description = "첫 번째 산책을 기록했어요!",
            icon = "walk",
            category = AchievementCategory.GENERAL.name
        ),
        AchievementEntity(
            id = "walk_10",
            title = "산책 애호가",
            description = "산책 10번을 기록했어요!",
            icon = "walk",
            category = AchievementCategory.GENERAL.name
        ),
        AchievementEntity(
            id = "walk_50",
            title = "산책 마니아",
            description = "산책 50번을 기록했어요!",
            icon = "walk",
            category = AchievementCategory.GENERAL.name
        ),

        // 식사 관련 업적
        AchievementEntity(
            id = "meal_first",
            title = "첫 식사 기록",
            description = "첫 번째 식사를 기록했어요!",
            icon = "meal",
            category = AchievementCategory.GENERAL.name
        ),
        AchievementEntity(
            id = "meal_30",
            title = "규칙적인 식사",
            description = "식사 30번을 기록했어요!",
            icon = "meal",
            category = AchievementCategory.GENERAL.name
        ),

        // 병원 관련 업적
        AchievementEntity(
            id = "hospital_first",
            title = "첫 병원 방문",
            description = "첫 번째 병원 방문을 기록했어요!",
            icon = "hospital",
            category = AchievementCategory.GENERAL.name
        ),

        // 일반 업적
        AchievementEntity(
            id = "week_streak",
            title = "7일 연속",
            description = "7일 연속으로 기록했어요!",
            icon = "star",
            category = AchievementCategory.GENERAL.name
        ),
        AchievementEntity(
            id = "month_streak",
            title = "30일 연속",
            description = "30일 연속으로 기록했어요!",
            icon = "star",
            category = AchievementCategory.GENERAL.name
        )
    )

    // 업적 달성 조건 체크
    data class AchievementCheckResult(
        val achievementId: String,
        val isUnlocked: Boolean,
        val progress: Float
    )

    fun checkDiaryAchievements(diaryCount: Int): List<AchievementCheckResult> {
        return listOf(
            AchievementCheckResult(
                achievementId = "diary_first",
                isUnlocked = diaryCount >= 1,
                progress = if (diaryCount >= 1) 1f else 0f
            ),
            AchievementCheckResult(
                achievementId = "diary_10",
                isUnlocked = diaryCount >= 10,
                progress = (diaryCount.coerceAtMost(10) / 10f)
            ),
            AchievementCheckResult(
                achievementId = "diary_30",
                isUnlocked = diaryCount >= 30,
                progress = (diaryCount.coerceAtMost(30) / 30f)
            ),
            AchievementCheckResult(
                achievementId = "diary_100",
                isUnlocked = diaryCount >= 100,
                progress = (diaryCount.coerceAtMost(100) / 100f)
            )
        )
    }

    fun checkWeightAchievements(weightCount: Int): List<AchievementCheckResult> {
        return listOf(
            AchievementCheckResult(
                achievementId = "weight_first",
                isUnlocked = weightCount >= 1,
                progress = if (weightCount >= 1) 1f else 0f
            ),
            AchievementCheckResult(
                achievementId = "weight_10",
                isUnlocked = weightCount >= 10,
                progress = (weightCount.coerceAtMost(10) / 10f)
            ),
            AchievementCheckResult(
                achievementId = "weight_30",
                isUnlocked = weightCount >= 30,
                progress = (weightCount.coerceAtMost(30) / 30f)
            )
        )
    }

    fun checkVaccinationAchievements(vaccineCount: Int, completedCount: Int): List<AchievementCheckResult> {
        return listOf(
            AchievementCheckResult(
                achievementId = "vaccine_first",
                isUnlocked = vaccineCount >= 1,
                progress = if (vaccineCount >= 1) 1f else 0f
            ),
            AchievementCheckResult(
                achievementId = "vaccine_complete",
                isUnlocked = completedCount >= 1,
                progress = if (completedCount >= 1) 1f else 0f
            ),
            AchievementCheckResult(
                achievementId = "vaccine_5",
                isUnlocked = completedCount >= 5,
                progress = (completedCount.coerceAtMost(5) / 5f)
            )
        )
    }

    fun checkPhotoAchievements(photoCount: Int): List<AchievementCheckResult> {
        return listOf(
            AchievementCheckResult(
                achievementId = "photo_first",
                isUnlocked = photoCount >= 1,
                progress = if (photoCount >= 1) 1f else 0f
            ),
            AchievementCheckResult(
                achievementId = "photo_10",
                isUnlocked = photoCount >= 10,
                progress = (photoCount.coerceAtMost(10) / 10f)
            ),
            AchievementCheckResult(
                achievementId = "photo_50",
                isUnlocked = photoCount >= 50,
                progress = (photoCount.coerceAtMost(50) / 50f)
            )
        )
    }

    fun checkWalkAchievements(walkCount: Int): List<AchievementCheckResult> {
        return listOf(
            AchievementCheckResult(
                achievementId = "walk_first",
                isUnlocked = walkCount >= 1,
                progress = if (walkCount >= 1) 1f else 0f
            ),
            AchievementCheckResult(
                achievementId = "walk_10",
                isUnlocked = walkCount >= 10,
                progress = (walkCount.coerceAtMost(10) / 10f)
            ),
            AchievementCheckResult(
                achievementId = "walk_50",
                isUnlocked = walkCount >= 50,
                progress = (walkCount.coerceAtMost(50) / 50f)
            )
        )
    }

    fun checkMealAchievements(mealCount: Int): List<AchievementCheckResult> {
        return listOf(
            AchievementCheckResult(
                achievementId = "meal_first",
                isUnlocked = mealCount >= 1,
                progress = if (mealCount >= 1) 1f else 0f
            ),
            AchievementCheckResult(
                achievementId = "meal_30",
                isUnlocked = mealCount >= 30,
                progress = (mealCount.coerceAtMost(30) / 30f)
            )
        )
    }

    fun checkHospitalAchievements(hospitalCount: Int): List<AchievementCheckResult> {
        return listOf(
            AchievementCheckResult(
                achievementId = "hospital_first",
                isUnlocked = hospitalCount >= 1,
                progress = if (hospitalCount >= 1) 1f else 0f
            )
        )
    }
}
