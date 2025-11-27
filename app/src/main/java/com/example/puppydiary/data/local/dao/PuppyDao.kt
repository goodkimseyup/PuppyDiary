package com.example.puppydiary.data.local.dao

import androidx.room.*
import com.example.puppydiary.data.local.entity.PuppyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuppyDao {
    @Query("SELECT * FROM puppy WHERE id = 1")
    fun getPuppy(): Flow<PuppyEntity?>

    @Query("SELECT * FROM puppy WHERE id = 1")
    suspend fun getPuppyOnce(): PuppyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(puppy: PuppyEntity)

    @Delete
    suspend fun delete(puppy: PuppyEntity)
}
