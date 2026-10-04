package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.model.AchievementEntity
import com.example.data.local.model.GameSessionEntity
import com.example.data.local.model.ProfileEntity
import com.example.model.BrainRanks
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BrainRepository(private val database: AppDatabase) {
    private val profileDao = database.profileDao()
    private val sessionDao = database.gameSessionDao()
    private val achievementDao = database.achievementDao()

    fun getAllProfiles(): Flow<List<ProfileEntity>> = profileDao.getAllProfiles()

    fun getProfile(profileId: Long): Flow<ProfileEntity?> = profileDao.getProfileById(profileId)

    suspend fun getProfileOnce(profileId: Long): ProfileEntity? = profileDao.getProfileByIdOnce(profileId)

    fun getSessionsForProfile(profileId: Long): Flow<List<GameSessionEntity>> =
        sessionDao.getSessionsForProfile(profileId)

    fun getAchievementsForProfile(profileId: Long): Flow<List<AchievementEntity>> =
        achievementDao.getAchievementsForProfile(profileId)

    suspend fun createProfile(
        name: String,
        nickname: String,
        age: Int,
        grade: String,
        avatarId: String
    ): Long {
        val today = getTodayDateString()
        val newProfile = ProfileEntity(
            name = name,
            nickname = nickname,
            age = age,
            grade = grade,
            avatarId = avatarId,
            coins = 150,
            gems = 10,
            stars = 0,
            trophies = 0,
            currentRankIndex = 0,
            streakDays = 1,
            lastActiveDate = today,
            memoryScore = 60,
            logicScore = 60,
            focusScore = 60,
            iqScore = 60,
            mathScore = 60,
            gamesPlayed = 0,
            totalTimeSeconds = 0
        )
        val profileId = profileDao.insertProfile(newProfile)
        seedAchievementsForProfile(profileId)
        return profileId
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        profileDao.updateProfile(profile)
    }

    suspend fun recordGameResult(
        profileId: Long,
        gameId: String,
        gameTitle: String,
        category: String,
        difficulty: String,
        score: Int,
        accuracyPercentage: Int,
        timeSeconds: Int,
        starsEarned: Int
    ): Pair<ProfileEntity, List<AchievementEntity>> {
        val profile = profileDao.getProfileByIdOnce(profileId) ?: return Pair(
            ProfileEntity(name = "Player"),
            emptyList()
        )

        val session = GameSessionEntity(
            profileId = profileId,
            gameId = gameId,
            gameTitle = gameTitle,
            category = category,
            difficulty = difficulty,
            score = score,
            accuracyPercentage = accuracyPercentage,
            timeSeconds = timeSeconds,
            starsEarned = starsEarned
        )
        sessionDao.insertSession(session)

        val earnedCoins = score / 5 + (starsEarned * 10)
        val earnedGems = if (starsEarned == 3) 2 else 0

        var newMem = profile.memoryScore
        var newFocus = profile.focusScore
        var newLogic = profile.logicScore
        var newIq = profile.iqScore
        var newMath = profile.mathScore

        val delta = (score / 15).coerceIn(2, 20)
        when (category) {
            "Memory" -> newMem = (newMem + delta).coerceAtMost(250)
            "Focus" -> newFocus = (newFocus + delta).coerceAtMost(250)
            "Logic" -> newLogic = (newLogic + delta).coerceAtMost(250)
            "IQ" -> newIq = (newIq + delta).coerceAtMost(250)
            "Math" -> newMath = (newMath + delta).coerceAtMost(250)
        }

        val totalCombinedScore = newMem + newFocus + newLogic + newIq + newMath
        val newRank = BrainRanks.getRankForScore(totalCombinedScore)
        val newTrophies = if (newRank.rankIndex > profile.currentRankIndex) profile.trophies + 1 else profile.trophies

        val today = getTodayDateString()
        val updatedStreak = if (profile.lastActiveDate == today) {
            profile.streakDays
        } else {
            profile.streakDays + 1
        }

        val updatedProfile = profile.copy(
            coins = profile.coins + earnedCoins,
            gems = profile.gems + earnedGems,
            stars = profile.stars + starsEarned,
            trophies = newTrophies,
            currentRankIndex = newRank.rankIndex,
            streakDays = updatedStreak,
            lastActiveDate = today,
            gamesPlayed = profile.gamesPlayed + 1,
            totalTimeSeconds = profile.totalTimeSeconds + timeSeconds,
            memoryScore = newMem,
            focusScore = newFocus,
            logicScore = newLogic,
            iqScore = newIq,
            mathScore = newMath,
            todayMinutesPlayed = profile.todayMinutesPlayed + (timeSeconds / 60).coerceAtLeast(1)
        )
        profileDao.updateProfile(updatedProfile)

        val unlockedAchievements = checkAndProgressAchievements(
            profileId = profileId,
            gameId = gameId,
            category = category,
            accuracy = accuracyPercentage,
            stars = starsEarned,
            streak = updatedStreak,
            totalGames = updatedProfile.gamesPlayed,
            iqScore = newIq
        )

        return Pair(updatedProfile, unlockedAchievements)
    }

    private suspend fun checkAndProgressAchievements(
        profileId: Long,
        gameId: String,
        category: String,
        accuracy: Int,
        stars: Int,
        streak: Int,
        totalGames: Int,
        iqScore: Int
    ): List<AchievementEntity> {
        val freshlyUnlocked = mutableListOf<AchievementEntity>()

        suspend fun updateAch(key: String, progressIncrement: Int = 1, setAbsolute: Int? = null) {
            val ach = achievementDao.getAchievement(profileId, key) ?: return
            if (ach.unlocked) return

            val newProgress = setAbsolute ?: (ach.progress + progressIncrement)
            val isComplete = newProgress >= ach.maxProgress
            val updated = ach.copy(
                progress = newProgress.coerceAtMost(ach.maxProgress),
                unlocked = isComplete,
                unlockedTimestamp = if (isComplete) System.currentTimeMillis() else null
            )
            achievementDao.updateAchievement(updated)
            if (isComplete) {
                freshlyUnlocked.add(updated)
            }
        }

        updateAch("first_game", setAbsolute = totalGames)

        if (stars >= 3) {
            updateAch("first_win", progressIncrement = 1)
        }

        if (category == "Memory") {
            updateAch("memory_master", progressIncrement = 1)
        }
        if (category == "Logic") {
            updateAch("logic_genius", progressIncrement = 1)
        }
        if (category == "Math") {
            updateAch("quick_counter", progressIncrement = 1)
        }
        if ((category == "Focus" || gameId == "speed_match") && accuracy >= 85) {
            updateAch("fast_thinker", progressIncrement = 1)
        }

        updateAch("streak_7", setAbsolute = streak)
        updateAch("streak_30", setAbsolute = streak)

        if (iqScore >= 100) {
            updateAch("iq_champion", setAbsolute = 1)
        }

        return freshlyUnlocked
    }

    suspend fun seedAchievementsForProfile(profileId: Long) {
        val initialAchievements = listOf(
            AchievementEntity(
                profileId = profileId,
                achievementKey = "first_game",
                title = "First Brain Step",
                description = "Play your very first brain game!",
                iconName = "🎮",
                progress = 0,
                maxProgress = 1,
                coinReward = 50,
                gemReward = 2
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "first_win",
                title = "Golden Star",
                description = "Earn a perfect 3-star score in any game!",
                iconName = "⭐",
                progress = 0,
                maxProgress = 1,
                coinReward = 80,
                gemReward = 3
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "memory_master",
                title = "Memory Master",
                description = "Complete 5 memory training games",
                iconName = "🧠",
                progress = 0,
                maxProgress = 5,
                coinReward = 120,
                gemReward = 5
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "logic_genius",
                title = "Logic Genius",
                description = "Solve 5 logic puzzles or path labyrinths",
                iconName = "🧩",
                progress = 0,
                maxProgress = 5,
                coinReward = 120,
                gemReward = 5
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "fast_thinker",
                title = "Fast Thinker",
                description = "Achieve 85%+ accuracy on Focus or Speed Match",
                iconName = "⚡",
                progress = 0,
                maxProgress = 1,
                coinReward = 100,
                gemReward = 4
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "quick_counter",
                title = "Quick Counter",
                description = "Complete 5 Mental Math and counting drills",
                iconName = "🔢",
                progress = 0,
                maxProgress = 5,
                coinReward = 100,
                gemReward = 3
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "streak_7",
                title = "7-Day Streak Hero",
                description = "Train your brain for 7 consecutive days!",
                iconName = "🔥",
                progress = 0,
                maxProgress = 7,
                coinReward = 250,
                gemReward = 10
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "streak_30",
                title = "30-Day Legend",
                description = "Master brain training daily for a whole month!",
                iconName = "👑",
                progress = 0,
                maxProgress = 30,
                coinReward = 500,
                gemReward = 25
            ),
            AchievementEntity(
                profileId = profileId,
                achievementKey = "iq_champion",
                title = "IQ Champion",
                description = "Develop your visual IQ score to 100+",
                iconName = "💡",
                progress = 0,
                maxProgress = 1,
                coinReward = 200,
                gemReward = 8
            )
        )
        achievementDao.insertAchievements(initialAchievements)
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
}
