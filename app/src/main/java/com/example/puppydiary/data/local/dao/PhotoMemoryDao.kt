package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.PhotoMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoMemoryDao {
    @Query("SELECT * FROM photo_memories WHERE puppyId = :puppyId ORDER BY date DESC")
    fun getPhotosByPuppy(puppyId: Long): Flow<List<PhotoMemoryEntity>>

    @Query("SELECT * FROM photo_memories ORDER BY date DESC")
    fun getAllPhotoMemories(): Flow<List<PhotoMemoryEntity>>

    @Query("SELECT * FROM photo_memories WHERE puppyId = :puppyId ORDER BY date DESC")
    suspend fun getPhotosByPuppyOnce(puppyId: Long): List<PhotoMemoryEntity>

    // LIKE 검색
    @Query("SELECT * FROM photo_memories WHERE puppyId = :puppyId AND description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<PhotoMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photoMemory: PhotoMemoryEntity): Long

    @Delete
    suspend fun delete(photoMemory: PhotoMemoryEntity)

    @Query("DELETE FROM photo_memories WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
