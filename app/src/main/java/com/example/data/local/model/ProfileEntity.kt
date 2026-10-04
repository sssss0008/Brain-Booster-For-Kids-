package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nickname: String = "",
    val age: Int = 7,
    val grade: String = "2nd Grade",
    val avatarId: String = "brain_hero",
    val coins: Int = 150,
    val gems: Int = 10,
    val stars: Int = 0,
    val trophies: Int = 0,
    val currentRankIndex: Int = 0,
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val memoryScore: Int = 60,
    val logicScore: Int = 60,
    val focusScore: Int = 60,
    val iqScore: Int = 60,
    val mathScore: Int = 60,
    val gamesPlayed: Int = 0,
    val totalTimeSeconds: Long = 0,
    val dailyGoalMinutes: Int = 15,
    val todayMinutesPlayed: Int = 5
)
