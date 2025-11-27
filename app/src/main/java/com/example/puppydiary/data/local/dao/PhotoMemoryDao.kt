package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.PhotoMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoMemoryDao {
    @Query("SELECT * FROM photo_memories ORDER BY date DESC")
    fun getAllPhotoMemories(): Flow<List<PhotoMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photoMemory: PhotoMemoryEntity): Long

    @Delete
    suspend fun delete(photoMemory: PhotoMemoryEntity)

    @Query("DELETE FROM photo_memories")
    suspend fun deleteAll()
}
