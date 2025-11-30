package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries WHERE puppyId = :puppyId ORDER BY date DESC")
    fun getEntriesByPuppy(puppyId: Long): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getDiaryEntryById(id: Long): DiaryEntryEntity?

    // LIKE 검색
    @Query("SELECT * FROM diary_entries WHERE puppyId = :puppyId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY date DESC")
    fun searchByPuppy(puppyId: Long, query: String): Flow<List<DiaryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diaryEntry: DiaryEntryEntity): Long

    @Update
    suspend fun update(diaryEntry: DiaryEntryEntity)

    @Delete
    suspend fun delete(diaryEntry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM diary_entries WHERE puppyId = :puppyId")
    suspend fun deleteByPuppy(puppyId: Long)
}
