package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE profileId = :profileId ORDER BY id ASC")
    fun getAchievementsForProfile(profileId: Long): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements WHERE profileId = :profileId AND achievementKey = :key LIMIT 1")
    suspend fun getAchievement(profileId: Long, key: String): AchievementEntity?

    @Query("SELECT COUNT(*) FROM achievements WHERE profileId = :profileId AND unlocked = 1")
    suspend fun getUnlockedCount(profileId: Long): Int
}
