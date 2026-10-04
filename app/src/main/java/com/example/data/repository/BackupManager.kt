package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.model.ProfileEntity
import org.json.JSONObject

class BackupManager(private val database: AppDatabase) {

    suspend fun exportBackupJson(profileId: Long): String {
        val profile = database.profileDao().getProfileByIdOnce(profileId) ?: return "{}"

        val root = JSONObject()
        root.put("app", "Brain Booster Kids")
        root.put("version", 1)
        root.put("exportTime", System.currentTimeMillis())

        val pObj = JSONObject().apply {
            put("name", profile.name)
            put("nickname", profile.nickname)
            put("age", profile.age)
            put("grade", profile.grade)
            put("avatarId", profile.avatarId)
            put("coins", profile.coins)
            put("gems", profile.gems)
            put("stars", profile.stars)
            put("trophies", profile.trophies)
            put("currentRankIndex", profile.currentRankIndex)
            put("streakDays", profile.streakDays)
            put("memoryScore", profile.memoryScore)
            put("logicScore", profile.logicScore)
            put("focusScore", profile.focusScore)
            put("iqScore", profile.iqScore)
            put("mathScore", profile.mathScore)
            put("gamesPlayed", profile.gamesPlayed)
            put("totalTimeSeconds", profile.totalTimeSeconds)
        }
        root.put("profile", pObj)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("profile")) return false
            val pObj = root.getJSONObject("profile")

            val name = pObj.optString("name", "Child Hero")
            val nickname = pObj.optString("nickname", "")
            val age = pObj.optInt("age", 7)
            val grade = pObj.optString("grade", "2nd Grade")
            val avatarId = pObj.optString("avatarId", "brain_hero")
            val coins = pObj.optInt("coins", 150)
            val gems = pObj.optInt("gems", 10)
            val stars = pObj.optInt("stars", 0)
            val trophies = pObj.optInt("trophies", 0)
            val currentRankIndex = pObj.optInt("currentRankIndex", 0)
            val streakDays = pObj.optInt("streakDays", 1)
            val memoryScore = pObj.optInt("memoryScore", 60)
            val logicScore = pObj.optInt("logicScore", 60)
            val focusScore = pObj.optInt("focusScore", 60)
            val iqScore = pObj.optInt("iqScore", 60)
            val mathScore = pObj.optInt("mathScore", 60)
            val gamesPlayed = pObj.optInt("gamesPlayed", 0)
            val totalTimeSeconds = pObj.optLong("totalTimeSeconds", 0L)

            val newProfile = ProfileEntity(
                name = name,
                nickname = nickname,
                age = age,
                grade = grade,
                avatarId = avatarId,
                coins = coins,
                gems = gems,
                stars = stars,
                trophies = trophies,
                currentRankIndex = currentRankIndex,
                streakDays = streakDays,
                memoryScore = memoryScore,
                logicScore = logicScore,
                focusScore = focusScore,
                iqScore = iqScore,
                mathScore = mathScore,
                gamesPlayed = gamesPlayed,
                totalTimeSeconds = totalTimeSeconds
            )
            val profileId = database.profileDao().insertProfile(newProfile)
            BrainRepository(database).seedAchievementsForProfile(profileId)
            true
        } catch (_: Exception) {
            false
        }
    }
}
