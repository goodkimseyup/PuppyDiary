package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.VaccinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations ORDER BY date DESC")
    fun getAllVaccinations(): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations ORDER BY date DESC")
    suspend fun getAllVaccinationsOnce(): List<VaccinationEntity>

    @Query("SELECT * FROM vaccinations WHERE date >= :startDate ORDER BY date DESC")
    fun getVaccinationsAfter(startDate: String): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE completed = 0 ORDER BY nextDate ASC")
    fun getPendingVaccinations(): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vaccination: VaccinationEntity): Long

    @Update
    suspend fun update(vaccination: VaccinationEntity)

    @Delete
    suspend fun delete(vaccination: VaccinationEntity)

    @Query("DELETE FROM vaccinations")
    suspend fun deleteAll()
}
