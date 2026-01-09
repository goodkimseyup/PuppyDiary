package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.MealRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealRecordDao {
    @Query("SELECT * FROM meal_records WHERE puppyId = :puppyId ORDER BY date DESC, time DESC")
    fun getRecordsByPuppy(puppyId: Long): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records WHERE puppyId = :puppyId ORDER BY date DESC, time DESC")
    suspend fun getRecordsByPuppyOnce(puppyId: Long): List<MealRecordEntity>

    @Query("SELECT * FROM meal_records WHERE puppyId = :puppyId AND date = :date ORDER BY time DESC")
    fun getRecordsByDate(puppyId: Long, date: String): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records WHERE puppyId = :puppyId AND (foodName LIKE '%' || :query || '%' OR foodType LIKE '%' || :query || '%' OR date LIKE '%' || :query || '%')")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<MealRecordEntity>>

    @Query("SELECT COUNT(*) FROM meal_records WHERE puppyId = :puppyId")
    suspend fun getCountByPuppy(puppyId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MealRecordEntity): Long

    @Update
    suspend fun update(record: MealRecordEntity)

    @Delete
    suspend fun delete(record: MealRecordEntity)

    @Query("DELETE FROM meal_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meal_records WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
