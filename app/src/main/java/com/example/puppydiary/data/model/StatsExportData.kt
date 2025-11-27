package com.example.puppydiary.data.model

data class StatsExportData(
    val puppyInfo: PuppyData,
    val weightRecords: List<WeightRecord>,
    val vaccinations: List<Vaccination>,
    val diaryEntries: List<DiaryEntry>,
    val achievements: List<Achievement>,
    val exportDate: String,
    val dateRange: DateRange
)