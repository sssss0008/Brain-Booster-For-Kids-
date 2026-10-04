package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameItem
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.KidBottomBar
import com.example.ui.components.KidDrawer
import com.example.ui.components.ParentGateDialog
import com.example.ui.components.RewardDialog
import com.example.ui.components.TopBrainBar
import com.example.ui.screens.about.AboutScreen
import com.example.ui.screens.achievements.AchievementsScreen
import com.example.ui.screens.backup.BackupRestoreDialog
import com.example.ui.screens.certificate.CertificateScreen
import com.example.ui.screens.games.LogicPathGame
import com.example.ui.screens.games.MemoryCardsGame
import com.example.ui.screens.games.MemorySequenceGame
import com.example.ui.screens.games.MentalMathGame
import com.example.ui.screens.games.MultiplayerGame
import com.example.ui.screens.games.SpeedMatchGame
import com.example.ui.screens.games.SpotTheObjectGame
import com.example.ui.screens.games.VisualIqGame
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.learn.LearnScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.parent.ParentDashboardScreen
import com.example.ui.screens.practice.PracticeScreen
import com.example.ui.screens.profile.ProfileManagerDialog
import com.example.ui.theme.BrainBoosterTheme
import com.example.ui.viewmodel.BrainViewModel
import com.example.ui.viewmodel.MainTab
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BrainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            BrainBoosterTheme(darkTheme = isDarkMode) {
                BrainBoosterApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BrainBoosterApp(viewModel: BrainViewModel) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()
    val difficulty by viewModel.activeDifficulty.collectAsStateWithLifecycle()
    val rewardEvent by viewModel.rewardEvent.collectAsStateWithLifecycle()
    val showConfetti by viewModel.showConfetti.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val showParentDashboard by viewModel.showParentDashboard.collectAsStateWithLifecycle()
    val showCertificate by viewModel.showCertificate.collectAsStateWithLifecycle()
    val showProfileDialog by viewModel.showProfileDialog.collectAsStateWithLifecycle()

    var showAchievementsScreen by remember { mutableStateOf(false) }
    var showParentGate by remember { mutableStateOf(false) }
    var parentGateAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var isBackupExportMode by remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // First time fallback if no profiles exist
    LaunchedEffect(isOnboardingCompleted, allProfiles.size) {
        if (isOnboardingCompleted && allProfiles.isEmpty()) {
            viewModel.createProfile(
                name = "Brain Hero",
                nickname = "Super Kid",
                age = 7,
                grade = "2nd Grade",
                avatarId = "brain_hero"
            )
        }
    }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            soundManager = viewModel.soundManager,
            onComplete = { name, nickname, age, grade, avatarId ->
                viewModel.createProfile(name, nickname, age, grade, avatarId)
                viewModel.completeOnboarding()
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            KidDrawer(
                profile = activeProfile,
                isDarkMode = isDarkMode,
                soundEnabled = viewModel.soundManager.soundEnabled,
                onToggleDarkMode = { viewModel.setDarkMode(it) },
                onToggleSound = { viewModel.setSoundEnabled(it) },
                onNavigateProfile = { viewModel.openProfileDialog(true) },
                onNavigateAchievements = { showAchievementsScreen = true },
                onNavigateRankings = { showAchievementsScreen = true },
                onNavigateCertificate = { viewModel.openCertificate(true) },
                onNavigateParentDashboard = {
                    parentGateAction = { viewModel.openParentDashboard(true) }
                    showParentGate = true
                },
                onNavigateSettings = {
                    parentGateAction = { viewModel.openSettingsDialog(true) }
                    showParentGate = true
                },
                onBackupData = {
                    isBackupExportMode = true
                    showBackupDialog = true
                },
                onRestoreData = {
                    isBackupExportMode = false
                    showBackupDialog = true
                },
                onNavigateAbout = { viewModel.selectTab(MainTab.ABOUT) },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (activeGame == null && !showAchievementsScreen && !showParentDashboard && !showCertificate) {
                    TopBrainBar(
                        profile = activeProfile,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onOpenParentGate = {
                            parentGateAction = { viewModel.openParentDashboard(true) }
                            showParentGate = true
                        },
                        onProfileClick = { viewModel.openProfileDialog(true) }
                    )
                }
            },
            bottomBar = {
                if (activeGame == null && !showAchievementsScreen && !showParentDashboard && !showCertificate) {
                    KidBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Main Tab Content
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            profile = activeProfile,
                            achievements = achievements,
                            onStartGame = { viewModel.startGame(it) },
                            onDailyChallenge = { viewModel.startDailyChallenge() },
                            onViewRankings = { showAchievementsScreen = true },
                            onViewAchievements = { showAchievementsScreen = true }
                        )
                    }
                    MainTab.LEARN -> {
                        LearnScreen(soundManager = viewModel.soundManager)
                    }
                    MainTab.PRACTICE -> {
                        PracticeScreen(
                            currentDifficulty = difficulty,
                            onSelectDifficulty = { viewModel.setDifficulty(it) },
                            onStartGame = { viewModel.startGame(it) }
                        )
                    }
                    MainTab.ABOUT -> {
                        AboutScreen()
                    }
                }

                // Active Game Overlay
                activeGame?.let { game ->
                    GameContainer(
                        game = game,
                        viewModel = viewModel,
                        onClose = { viewModel.closeGame() }
                    )
                }

                // Secondary Screens Overlays
                if (showAchievementsScreen) {
                    AchievementsScreen(
                        profile = activeProfile,
                        achievements = achievements,
                        onClose = { showAchievementsScreen = false }
                    )
                }

                if (showParentDashboard) {
                    ParentDashboardScreen(
                        profile = activeProfile,
                        sessions = sessions,
                        onClose = { viewModel.openParentDashboard(false) }
                    )
                }

                if (showCertificate) {
                    CertificateScreen(
                        profile = activeProfile,
                        onClose = { viewModel.openCertificate(false) }
                    )
                }

                // Celebratory Confetti Effect
                AnimatedVisibility(
                    visible = showConfetti,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ConfettiEffect()
                }

                // Reward Dialog
                rewardEvent?.let { reward ->
                    RewardDialog(
                        reward = reward,
                        onClaim = { viewModel.dismissReward() }
                    )
                }

                // Parent Gate Dialog
                if (showParentGate) {
                    ParentGateDialog(
                        onSuccess = {
                            showParentGate = false
                            parentGateAction?.invoke()
                            parentGateAction = null
                        },
                        onDismiss = {
                            showParentGate = false
                            parentGateAction = null
                        }
                    )
                }

                // Multi-Profile Dialog
                if (showProfileDialog) {
                    ProfileManagerDialog(
                        currentProfile = activeProfile,
                        allProfiles = allProfiles,
                        onSwitchProfile = { viewModel.switchProfile(it) },
                        onCreateProfile = { name, nickname, age, grade, avatarId ->
                            viewModel.createProfile(name, nickname, age, grade, avatarId)
                        },
                        onUpdateProfile = { name, nickname, age, grade, avatarId ->
                            viewModel.updateCurrentProfile(name, nickname, age, grade, avatarId)
                        },
                        onDismiss = { viewModel.openProfileDialog(false) }
                    )
                }

                // Backup / Restore Progress Dialog
                if (showBackupDialog) {
                    BackupRestoreDialog(
                        profileId = activeProfile?.id ?: 1L,
                        backupManager = viewModel.backupManager,
                        isExportMode = isBackupExportMode,
                        onSuccessRestore = {
                            viewModel.switchProfile(activeProfile?.id ?: 1L)
                        },
                        onDismiss = { showBackupDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameContainer(
    game: GameItem,
    viewModel: BrainViewModel,
    onClose: () -> Unit
) {
    val difficulty by viewModel.activeDifficulty.collectAsStateWithLifecycle()

    val onFinishHandler: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit = { score, accuracy, timeSec, stars ->
        viewModel.recordGameFinished(
            gameId = game.id,
            gameTitle = game.title,
            category = game.category.displayName.replace(" Games", "").replace(" Training", ""),
            score = score,
            accuracy = accuracy,
            timeSeconds = timeSec,
            starsEarned = stars
        )
    }

    when (game.id) {
        "memory_cards" -> {
            MemoryCardsGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        "speed_match", "focus_challenge" -> {
            SpeedMatchGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        "spot_object", "odd_one_out", "missing_object" -> {
            SpotTheObjectGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        "mental_math", "quick_counting", "missing_number" -> {
            MentalMathGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        "memory_sequence", "color_memory" -> {
            MemorySequenceGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        "visual_iq", "shadow_match", "pattern_logic" -> {
            VisualIqGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        "logic_path", "maze_challenge" -> {
            LogicPathGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        "multiplayer_battle" -> {
            MultiplayerGame(
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
        else -> {
            SpeedMatchGame(
                difficulty = difficulty,
                soundManager = viewModel.soundManager,
                onFinish = onFinishHandler,
                onClose = onClose
            )
        }
    }
}
