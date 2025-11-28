package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.VaccinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations WHERE puppyId = :puppyId ORDER BY date DESC")
    fun getVaccinationsByPuppy(puppyId: Long): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations ORDER BY date DESC")
    fun getAllVaccinations(): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE puppyId = :puppyId ORDER BY date DESC")
    suspend fun getVaccinationsByPuppyOnce(puppyId: Long): List<VaccinationEntity>

    @Query("SELECT * FROM vaccinations WHERE puppyId = :puppyId AND completed = 0 ORDER BY nextDate ASC")
    fun getPendingVaccinations(puppyId: Long): Flow<List<VaccinationEntity>>

    // LIKE 검색
    @Query("SELECT * FROM vaccinations WHERE puppyId = :puppyId AND vaccine LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vaccination: VaccinationEntity): Long

    @Update
    suspend fun update(vaccination: VaccinationEntity)

    @Delete
    suspend fun delete(vaccination: VaccinationEntity)

    @Query("DELETE FROM vaccinations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vaccinations WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
