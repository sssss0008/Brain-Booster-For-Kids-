package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.model.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getSessionsForProfile(profileId: Long): Flow<List<GameSessionEntity>>

    @Query("SELECT * FROM game_sessions WHERE profileId = :profileId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(profileId: Long, limit: Int = 10): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity): Long

    @Query("SELECT COUNT(*) FROM game_sessions WHERE profileId = :profileId")
    suspend fun getTotalGamesPlayed(profileId: Long): Int

    @Query("SELECT AVG(accuracyPercentage) FROM game_sessions WHERE profileId = :profileId")
    suspend fun getAverageAccuracy(profileId: Long): Double?

    @Query("SELECT * FROM game_sessions WHERE profileId = :profileId AND gameId = :gameId ORDER BY score DESC LIMIT 1")
    suspend fun getHighScore(profileId: Long, gameId: String): GameSessionEntity?

    @Query("DELETE FROM game_sessions WHERE profileId = :profileId")
    suspend fun deleteAllForProfile(profileId: Long)
}
