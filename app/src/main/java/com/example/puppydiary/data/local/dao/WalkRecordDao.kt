package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.WalkRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkRecordDao {
    @Query("SELECT * FROM walk_records WHERE puppyId = :puppyId ORDER BY date DESC, startTime DESC")
    fun getRecordsByPuppy(puppyId: Long): Flow<List<WalkRecordEntity>>

    @Query("SELECT * FROM walk_records WHERE puppyId = :puppyId ORDER BY date DESC, startTime DESC")
    suspend fun getRecordsByPuppyOnce(puppyId: Long): List<WalkRecordEntity>

    @Query("SELECT * FROM walk_records WHERE puppyId = :puppyId AND date = :date")
    fun getRecordsByDate(puppyId: Long, date: String): Flow<List<WalkRecordEntity>>

    @Query("SELECT * FROM walk_records WHERE puppyId = :puppyId AND (note LIKE '%' || :query || '%' OR date LIKE '%' || :query || '%')")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<WalkRecordEntity>>

    @Query("SELECT COUNT(*) FROM walk_records WHERE puppyId = :puppyId")
    suspend fun getCountByPuppy(puppyId: Long): Int

    @Query("SELECT SUM(durationMinutes) FROM walk_records WHERE puppyId = :puppyId")
    suspend fun getTotalDurationByPuppy(puppyId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WalkRecordEntity): Long

    @Update
    suspend fun update(record: WalkRecordEntity)

    @Delete
    suspend fun delete(record: WalkRecordEntity)

    @Query("DELETE FROM walk_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM walk_records WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
