package com.example.quizapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,        // Firebase uid, so every account has its own history
    val timestamp: Long,       // date of the quiz (millis)
    val categoryName: String,
    val difficulty: String,
    val score: Int,
    val total: Int = 10
)
