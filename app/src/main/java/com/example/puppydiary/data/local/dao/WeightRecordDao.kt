package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightRecordDao {
    @Query("SELECT * FROM weight_records ORDER BY date ASC")
    fun getAllRecords(): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE date >= :startDate ORDER BY date ASC")
    fun getWeightRecordsAfter(startDate: String): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeightRecord(): WeightRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weightRecord: WeightRecordEntity)

    @Delete
    suspend fun delete(weightRecord: WeightRecordEntity)

    @Query("DELETE FROM weight_records")
    suspend fun deleteAll()
}
