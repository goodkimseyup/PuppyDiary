package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.VaccinationReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationReminderDao {
    @Query("SELECT * FROM vaccination_reminders ORDER BY scheduledDate ASC")
    fun getAllReminders(): Flow<List<VaccinationReminderEntity>>

    @Query("SELECT * FROM vaccination_reminders WHERE isActive = 1 ORDER BY scheduledDate ASC")
    fun getActiveReminders(): Flow<List<VaccinationReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: VaccinationReminderEntity)

    @Update
    suspend fun update(reminder: VaccinationReminderEntity)

    @Delete
    suspend fun delete(reminder: VaccinationReminderEntity)

    @Query("UPDATE vaccination_reminders SET isActive = 0 WHERE id = :id")
    suspend fun dismissReminder(id: String)
}
