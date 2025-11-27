package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.PuppyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuppyDao {
    // 선택된 강아지 조회
    @Query("SELECT * FROM puppy WHERE isSelected = 1 LIMIT 1")
    fun getSelectedPuppy(): Flow<PuppyEntity?>

    @Query("SELECT * FROM puppy WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedPuppyOnce(): PuppyEntity?

    // 모든 강아지 조회
    @Query("SELECT * FROM puppy ORDER BY id ASC")
    fun getAllPuppies(): Flow<List<PuppyEntity>>

    @Query("SELECT * FROM puppy ORDER BY id ASC")
    suspend fun getAllPuppiesOnce(): List<PuppyEntity>

    // 강아지 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(puppy: PuppyEntity): Long

    // 강아지 업데이트
    @Update
    suspend fun update(puppy: PuppyEntity)

    // 모든 강아지 선택 해제
    @Query("UPDATE puppy SET isSelected = 0")
    suspend fun deselectAll()

    // 특정 강아지 선택
    @Query("UPDATE puppy SET isSelected = 1 WHERE id = :puppyId")
    suspend fun selectPuppy(puppyId: Long)

    @Delete
    suspend fun delete(puppy: PuppyEntity)

    @Query("DELETE FROM puppy WHERE id = :puppyId")
    suspend fun deleteById(puppyId: Long)
}
