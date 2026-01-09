package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.HospitalVisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalVisitDao {
    @Query("SELECT * FROM hospital_visits WHERE puppyId = :puppyId ORDER BY date DESC")
    fun getVisitsByPuppy(puppyId: Long): Flow<List<HospitalVisitEntity>>

    @Query("SELECT * FROM hospital_visits WHERE puppyId = :puppyId ORDER BY date DESC")
    suspend fun getVisitsByPuppyOnce(puppyId: Long): List<HospitalVisitEntity>

    @Query("SELECT * FROM hospital_visits WHERE puppyId = :puppyId AND (hospitalName LIKE '%' || :query || '%' OR visitReason LIKE '%' || :query || '%' OR diagnosis LIKE '%' || :query || '%' OR date LIKE '%' || :query || '%')")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<HospitalVisitEntity>>

    @Query("SELECT COUNT(*) FROM hospital_visits WHERE puppyId = :puppyId")
    suspend fun getCountByPuppy(puppyId: Long): Int

    @Query("SELECT SUM(cost) FROM hospital_visits WHERE puppyId = :puppyId")
    suspend fun getTotalCostByPuppy(puppyId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: HospitalVisitEntity): Long

    @Update
    suspend fun update(visit: HospitalVisitEntity)

    @Delete
    suspend fun delete(visit: HospitalVisitEntity)

    @Query("DELETE FROM hospital_visits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM hospital_visits WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
