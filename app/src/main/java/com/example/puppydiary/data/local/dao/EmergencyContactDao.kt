package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts WHERE puppyId = :puppyId ORDER BY contactType, name")
    fun getContactsByPuppy(puppyId: Long): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE puppyId = :puppyId ORDER BY contactType, name")
    suspend fun getContactsByPuppyOnce(puppyId: Long): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE puppyId = :puppyId AND contactType = :type")
    fun getContactsByType(puppyId: Long, type: String): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE puppyId = :puppyId AND (name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%')")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: EmergencyContactEntity): Long

    @Update
    suspend fun update(contact: EmergencyContactEntity)

    @Delete
    suspend fun delete(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM emergency_contacts WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
