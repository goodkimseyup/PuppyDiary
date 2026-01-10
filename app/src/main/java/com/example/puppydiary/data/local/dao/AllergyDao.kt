package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.AllergyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergyDao {
    @Query("SELECT * FROM allergies WHERE puppyId = :puppyId ORDER BY diagnosedDate DESC")
    fun getAllergiesByPuppyId(puppyId: Long): Flow<List<AllergyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(allergy: AllergyEntity)

    @Update
    suspend fun update(allergy: AllergyEntity)

    @Delete
    suspend fun delete(allergy: AllergyEntity)

    @Query("DELETE FROM allergies WHERE id = :id")
    suspend fun deleteById(id: Long)
}
