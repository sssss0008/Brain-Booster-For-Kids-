package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val achievementKey: String,
    val title: String,
    val description: String,
    val iconName: String,
    val progress: Int,
    val maxProgress: Int,
    val unlocked: Boolean = false,
    val unlockedTimestamp: Long? = null,
    val coinReward: Int = 50,
    val gemReward: Int = 2
)
