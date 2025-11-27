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
        PhotoMemoryEntity::class
    ],
    version = 2,
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
