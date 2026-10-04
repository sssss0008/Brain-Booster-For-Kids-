package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sound.SoundManager
import com.example.model.DifficultyLevel
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorySequenceGame(
    difficulty: DifficultyLevel,
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val padColors = listOf(
        Pair(Color(0xFFEF4444), Color(0xFFFCA5A5)), // Red
        Pair(Color(0xFF3B82F6), Color(0xFF93C5FD)), // Blue
        Pair(Color(0xFF10B981), Color(0xFF6EE7B7)), // Green
        Pair(Color(0xFFF59E0B), Color(0xFFFDE68A))  // Yellow
    )

    val targetRounds = when (difficulty) {
        DifficultyLevel.EASY -> 4
        DifficultyLevel.MEDIUM -> 5
        DifficultyLevel.HARD -> 7
        DifficultyLevel.EXPERT -> 9
    }

    val sequence = remember { mutableStateListOf<Int>() }
    val userSequence = remember { mutableStateListOf<Int>() }
    var flashingPadIndex by remember { mutableStateOf<Int?>(null) }
    var isPlayingSequence by remember { mutableStateOf(false) }
    var currentLevel by remember { mutableIntStateOf(1) }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    // Start sequence
    LaunchedEffect(currentLevel) {
        isPlayingSequence = true
        userSequence.clear()
        // Add random pad
        sequence.add(Random.nextInt(4))
        delay(600)

        // Flash sequence
        for (pad in sequence) {
            flashingPadIndex = pad
            soundManager.playClick()
            delay(500)
            flashingPadIndex = null
            delay(250)
        }
        isPlayingSequence = false
        soundManager.speak("Your turn! Tap the colors in order.")
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    fun onPadTapped(index: Int) {
        if (isPlayingSequence) return
        soundManager.playClick()
        userSequence.add(index)

        val currentStep = userSequence.size - 1
        if (userSequence[currentStep] != sequence[currentStep]) {
            // Mistake
            soundManager.playError()
            val stars = when {
                currentLevel >= targetRounds -> 3
                currentLevel >= 3 -> 2
                else -> 1
            }
            val score = ((currentLevel * 40) * difficulty.multiplier).toInt()
            onFinish(score, 75, secondsElapsed, stars)
            return
        }

        // Finished sequence for this level
        if (userSequence.size == sequence.size) {
            soundManager.playSuccess()
            if (currentLevel >= targetRounds) {
                // Won complete game!
                val totalScore = ((targetRounds * 50) * difficulty.multiplier).toInt()
                onFinish(totalScore, 100, secondsElapsed, 3)
            } else {
                currentLevel++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            title = {
                Text(
                    text = "Memory Sequence 🌈",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose, modifier = Modifier.testTag("game_close_button")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Game")
                }
            },
            actions = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Level $currentLevel/$targetRounds",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isPlayingSequence) "👀 Watch the flashing sequence carefully..." else "👆 Your turn! Repeat the pattern!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPlayingSequence) MaterialTheme.colorScheme.primary else Color(0xFF10B981),
                textAlign = TextAlign.Center
            )

            // 2x2 Big Pads
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    PadButton(
                        index = 0,
                        baseColor = padColors[0].first,
                        highlightColor = padColors[0].second,
                        isFlashing = flashingPadIndex == 0,
                        enabled = !isPlayingSequence,
                        onTap = { onPadTapped(0) },
                        modifier = Modifier.weight(1f)
                    )
                    PadButton(
                        index = 1,
                        baseColor = padColors[1].first,
                        highlightColor = padColors[1].second,
                        isFlashing = flashingPadIndex == 1,
                        enabled = !isPlayingSequence,
                        onTap = { onPadTapped(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    PadButton(
                        index = 2,
                        baseColor = padColors[2].first,
                        highlightColor = padColors[2].second,
                        isFlashing = flashingPadIndex == 2,
                        enabled = !isPlayingSequence,
                        onTap = { onPadTapped(2) },
                        modifier = Modifier.weight(1f)
                    )
                    PadButton(
                        index = 3,
                        baseColor = padColors[3].first,
                        highlightColor = padColors[3].second,
                        isFlashing = flashingPadIndex == 3,
                        enabled = !isPlayingSequence,
                        onTap = { onPadTapped(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PadButton(
    index: Int,
    baseColor: Color,
    highlightColor: Color,
    isFlashing: Boolean,
    enabled: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isFlashing) highlightColor else baseColor,
        animationSpec = tween(150),
        label = "pad_color"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(animatedBg)
            .clickable(enabled = enabled) { onTap() }
            .testTag("memory_pad_$index")
    )
}
