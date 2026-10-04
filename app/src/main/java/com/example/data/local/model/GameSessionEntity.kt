package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val gameId: String,
    val gameTitle: String,
    val category: String,
    val difficulty: String,
    val score: Int,
    val accuracyPercentage: Int,
    val timeSeconds: Int,
    val starsEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)
