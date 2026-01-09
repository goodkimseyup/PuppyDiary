package com.example.puppydiary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.puppydiary.data.local.dao.*
import com.example.puppydiary.data.local.entity.*

@Database(
    entities = [
        PuppyEntity::class,
        WeightRecordEntity::class,
        DiaryEntryEntity::class,
        VaccinationEntity::class,
        AchievementEntity::class,
        VaccinationReminderEntity::class,
        PhotoMemoryEntity::class,
        WalkRecordEntity::class,
        MealRecordEntity::class,
        HospitalVisitEntity::class,
        MedicationRecordEntity::class,
        EmergencyContactEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PuppyDatabase : RoomDatabase() {

    abstract fun puppyDao(): PuppyDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun achievementDao(): AchievementDao
    abstract fun vaccinationReminderDao(): VaccinationReminderDao
    abstract fun photoMemoryDao(): PhotoMemoryDao
    abstract fun walkRecordDao(): WalkRecordDao
    abstract fun mealRecordDao(): MealRecordDao
    abstract fun hospitalVisitDao(): HospitalVisitDao
    abstract fun medicationRecordDao(): MedicationRecordDao
    abstract fun emergencyContactDao(): EmergencyContactDao

    companion object {
        @Volatile
        private var INSTANCE: PuppyDatabase? = null

        fun getDatabase(context: Context): PuppyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuppyDatabase::class.java,
                    "puppy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
