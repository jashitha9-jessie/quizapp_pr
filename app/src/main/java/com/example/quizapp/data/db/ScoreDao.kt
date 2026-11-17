package com.example.quizapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Insert
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT * FROM scores WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeScores(userId: String): Flow<List<ScoreEntity>>

    @Query("DELETE FROM scores WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
