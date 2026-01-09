package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.MedicationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationRecordDao {
    @Query("SELECT * FROM medication_records WHERE puppyId = :puppyId ORDER BY date DESC")
    fun getRecordsByPuppy(puppyId: Long): Flow<List<MedicationRecordEntity>>

    @Query("SELECT * FROM medication_records WHERE puppyId = :puppyId ORDER BY date DESC")
    suspend fun getRecordsByPuppyOnce(puppyId: Long): List<MedicationRecordEntity>

    @Query("SELECT * FROM medication_records WHERE puppyId = :puppyId AND medicationType = :type ORDER BY date DESC")
    fun getRecordsByType(puppyId: Long, type: String): Flow<List<MedicationRecordEntity>>

    @Query("SELECT * FROM medication_records WHERE puppyId = :puppyId AND (medicationName LIKE '%' || :query || '%' OR medicationType LIKE '%' || :query || '%' OR date LIKE '%' || :query || '%')")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<MedicationRecordEntity>>

    @Query("SELECT COUNT(*) FROM medication_records WHERE puppyId = :puppyId")
    suspend fun getCountByPuppy(puppyId: Long): Int

    @Query("SELECT * FROM medication_records WHERE puppyId = :puppyId AND nextDate IS NOT NULL ORDER BY nextDate ASC LIMIT 1")
    suspend fun getNextMedication(puppyId: Long): MedicationRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicationRecordEntity): Long

    @Update
    suspend fun update(record: MedicationRecordEntity)

    @Delete
    suspend fun delete(record: MedicationRecordEntity)

    @Query("DELETE FROM medication_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM medication_records WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
