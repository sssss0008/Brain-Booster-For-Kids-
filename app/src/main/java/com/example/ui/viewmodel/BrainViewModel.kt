package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.model.AchievementEntity
import com.example.data.local.model.GameSessionEntity
import com.example.data.local.model.ProfileEntity
import com.example.data.preferences.AppPreferences
import com.example.data.repository.BackupManager
import com.example.data.repository.BrainRepository
import com.example.data.sound.SoundManager
import com.example.model.BrainRank
import com.example.model.BrainRanks
import com.example.model.DifficultyLevel
import com.example.model.GameCatalog
import com.example.model.GameItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MainTab {
    HOME, LEARN, PRACTICE, ABOUT
}

data class RewardEvent(
    val title: String,
    val score: Int,
    val stars: Int,
    val coinsEarned: Int,
    val gemsEarned: Int,
    val rankUp: BrainRank? = null,
    val unlockedAchievements: List<AchievementEntity> = emptyList()
)

class BrainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = BrainRepository(database)
    val preferences = AppPreferences(application)
    val soundManager = SoundManager(application)
    val backupManager = BackupManager(database)

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeProfile = MutableStateFlow<ProfileEntity?>(null)
    val activeProfile: StateFlow<ProfileEntity?> = _activeProfile.asStateFlow()

    private val _allProfiles = MutableStateFlow<List<ProfileEntity>>(emptyList())
    val allProfiles: StateFlow<List<ProfileEntity>> = _allProfiles.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val achievements: StateFlow<List<AchievementEntity>> = _achievements.asStateFlow()

    private val _recentSessions = MutableStateFlow<List<GameSessionEntity>>(emptyList())
    val recentSessions: StateFlow<List<GameSessionEntity>> = _recentSessions.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(preferences.hasCompletedOnboarding)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isDarkMode = MutableStateFlow(preferences.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _activeGame = MutableStateFlow<GameItem?>(null)
    val activeGame: StateFlow<GameItem?> = _activeGame.asStateFlow()

    private val _activeDifficulty = MutableStateFlow(
        try {
            DifficultyLevel.valueOf(preferences.selectedDifficulty)
        } catch (_: Exception) {
            DifficultyLevel.MEDIUM
        }
    )
    val activeDifficulty: StateFlow<DifficultyLevel> = _activeDifficulty.asStateFlow()

    private val _rewardEvent = MutableStateFlow<RewardEvent?>(null)
    val rewardEvent: StateFlow<RewardEvent?> = _rewardEvent.asStateFlow()

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    private val _showParentDashboard = MutableStateFlow(false)
    val showParentDashboard: StateFlow<Boolean> = _showParentDashboard.asStateFlow()

    private val _showCertificate = MutableStateFlow(false)
    val showCertificate: StateFlow<Boolean> = _showCertificate.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    init {
        soundManager.soundEnabled = preferences.soundEffectsEnabled
        soundManager.voiceEnabled = preferences.voiceInstructionsEnabled

        viewModelScope.launch {
            repository.getAllProfiles().collect { list ->
                _allProfiles.value = list
                if (list.isNotEmpty()) {
                    val currentId = preferences.activeProfileId
                    val matched = list.firstOrNull { it.id == currentId } ?: list.first()
                    _activeProfile.value = matched
                    preferences.activeProfileId = matched.id
                    loadProfileData(matched.id)
                }
            }
        }
    }

    private fun loadProfileData(profileId: Long) {
        viewModelScope.launch {
            repository.getAchievementsForProfile(profileId).collect {
                _achievements.value = it
            }
        }
        viewModelScope.launch {
            repository.getSessionsForProfile(profileId).collect {
                _recentSessions.value = it
            }
        }
    }

    fun completeOnboarding() {
        preferences.hasCompletedOnboarding = true
        _isOnboardingCompleted.value = true
        soundManager.playSuccess()
    }

    fun selectTab(tab: MainTab) {
        soundManager.playClick()
        _currentTab.value = tab
    }

    fun switchProfile(profileId: Long) {
        preferences.activeProfileId = profileId
        val target = _allProfiles.value.firstOrNull { it.id == profileId }
        if (target != null) {
            _activeProfile.value = target
            loadProfileData(profileId)
            soundManager.playClick()
        }
    }

    fun createProfile(name: String, nickname: String, age: Int, grade: String, avatarId: String) {
        viewModelScope.launch {
            val newId = repository.createProfile(name, nickname, age, grade, avatarId)
            preferences.activeProfileId = newId
            soundManager.playSuccess()
            soundManager.speak("Welcome, $name! Let's boost your brain!")
        }
    }

    fun updateCurrentProfile(name: String, nickname: String, age: Int, grade: String, avatarId: String) {
        val current = _activeProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name,
                nickname = nickname,
                age = age,
                grade = grade,
                avatarId = avatarId
            )
            repository.updateProfile(updated)
            _activeProfile.value = updated
            soundManager.playSuccess()
        }
    }

    fun setDifficulty(level: DifficultyLevel) {
        preferences.selectedDifficulty = level.name
        _activeDifficulty.value = level
        soundManager.playClick()
    }

    fun setDarkMode(dark: Boolean) {
        preferences.isDarkMode = dark
        _isDarkMode.value = dark
        soundManager.playClick()
    }

    fun setSoundEnabled(enabled: Boolean) {
        preferences.soundEffectsEnabled = enabled
        soundManager.soundEnabled = enabled
    }

    fun setVoiceEnabled(enabled: Boolean) {
        preferences.voiceInstructionsEnabled = enabled
        soundManager.voiceEnabled = enabled
        if (enabled) {
            soundManager.speak("Voice guidance enabled!")
        }
    }

    fun startGame(game: GameItem) {
        soundManager.playClick()
        soundManager.speak("Starting ${game.title}!")
        _activeGame.value = game
    }

    fun startDailyChallenge() {
        val challenge = GameCatalog.allGames.firstOrNull { it.isChallenge } ?: GameCatalog.allGames[0]
        startGame(challenge)
    }

    fun closeGame() {
        soundManager.playClick()
        _activeGame.value = null
    }

    fun recordGameFinished(
        gameId: String,
        gameTitle: String,
        category: String,
        score: Int,
        accuracy: Int,
        timeSeconds: Int,
        starsEarned: Int
    ) {
        val profile = _activeProfile.value ?: return
        viewModelScope.launch {
            val prevRankIndex = profile.currentRankIndex
            val (updatedProfile, unlocked) = repository.recordGameResult(
                profileId = profile.id,
                gameId = gameId,
                gameTitle = gameTitle,
                category = category,
                difficulty = _activeDifficulty.value.label,
                score = score,
                accuracyPercentage = accuracy,
                timeSeconds = timeSeconds,
                starsEarned = starsEarned
            )
            _activeProfile.value = updatedProfile

            val rankUp = if (updatedProfile.currentRankIndex > prevRankIndex) {
                BrainRanks.all.getOrNull(updatedProfile.currentRankIndex)
            } else null

            val coinsEarned = score / 5 + (starsEarned * 10)
            val gemsEarned = if (starsEarned == 3) 2 else 0

            _rewardEvent.value = RewardEvent(
                title = gameTitle,
                score = score,
                stars = starsEarned,
                coinsEarned = coinsEarned,
                gemsEarned = gemsEarned,
                rankUp = rankUp,
                unlockedAchievements = unlocked
            )
            _showConfetti.value = true

            soundManager.playFanfare()
            val cheerMessage = when (starsEarned) {
                3 -> "Incredible! Three golden stars!"
                2 -> "Great job! Two stars earned!"
                else -> "Good effort! Keep practicing!"
            }
            soundManager.speak(cheerMessage)
        }
    }

    fun dismissReward() {
        _rewardEvent.value = null
        _showConfetti.value = false
        _activeGame.value = null
    }

    fun openParentDashboard(show: Boolean) {
        _showParentDashboard.value = show
        soundManager.playClick()
    }

    fun openCertificate(show: Boolean) {
        _showCertificate.value = show
        soundManager.playClick()
    }

    fun openProfileDialog(show: Boolean) {
        _showProfileDialog.value = show
        soundManager.playClick()
    }

    fun openSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
        soundManager.playClick()
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
