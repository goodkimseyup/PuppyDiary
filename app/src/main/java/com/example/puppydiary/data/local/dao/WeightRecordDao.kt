package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightRecordDao {
    @Query("SELECT * FROM weight_records WHERE puppyId = :puppyId ORDER BY date ASC")
    fun getRecordsByPuppy(puppyId: Long): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records ORDER BY date ASC")
    fun getAllRecords(): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE puppyId = :puppyId AND date >= :startDate ORDER BY date ASC")
    fun getWeightRecordsAfter(puppyId: Long, startDate: String): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE puppyId = :puppyId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeightRecord(puppyId: Long): WeightRecordEntity?

    @Query("SELECT * FROM weight_records WHERE puppyId = :puppyId ORDER BY date ASC")
    suspend fun getRecordsByPuppyOnce(puppyId: Long): List<WeightRecordEntity>

    // LIKE 검색 (날짜로 검색)
    @Query("SELECT * FROM weight_records WHERE puppyId = :puppyId AND date LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<WeightRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weightRecord: WeightRecordEntity)

    @Update
    suspend fun update(weightRecord: WeightRecordEntity)

    @Delete
    suspend fun delete(weightRecord: WeightRecordEntity)

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM weight_records WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
