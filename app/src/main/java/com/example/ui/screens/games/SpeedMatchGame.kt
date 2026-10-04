package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun SpeedMatchGame(
    difficulty: DifficultyLevel,
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val pool = listOf("⭐", "🦁", "🚀", "🍎", "🦄", "⚽", "🎈", "💎")
    val totalTime = when (difficulty) {
        DifficultyLevel.EASY -> 30
        DifficultyLevel.MEDIUM -> 25
        DifficultyLevel.HARD -> 20
        DifficultyLevel.EXPERT -> 15
    }

    var secondsLeft by remember { mutableIntStateOf(totalTime) }
    var currentItem by remember { mutableStateOf(pool.random()) }
    var previousItem by remember { mutableStateOf<String?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var totalAttempts by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        soundManager.speak("Does this symbol match the previous one? Be fast!")
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }

        // Game over
        val accuracy = if (totalAttempts > 0) ((correctCount.toFloat() / totalAttempts) * 100).toInt() else 0
        val stars = when {
            score >= 200 && accuracy >= 80 -> 3
            score >= 100 -> 2
            else -> 1
        }
        val finalScore = (score * difficulty.multiplier).toInt()
        onFinish(finalScore, accuracy, totalTime, stars)
    }

    fun handleAnswer(userSaidMatch: Boolean) {
        if (previousItem == null) return
        val actuallyMatched = currentItem == previousItem
        val isCorrect = userSaidMatch == actuallyMatched

        totalAttempts++
        if (isCorrect) {
            streak++
            val pts = 10 + (streak * 2)
            score += pts
            soundManager.playSuccess()
        } else {
            streak = 0
            soundManager.playError()
        }

        // Next item
        previousItem = currentItem
        val shouldMatch = Random.nextFloat() < 0.45f
        currentItem = if (shouldMatch) previousItem!! else (pool - previousItem!!).random()
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
                    text = "Speed Match ⏱️",
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
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Score: $score",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        )

        // Progress bar for timer
        LinearProgressIndicator(
            progress = { (secondsLeft.toFloat() / totalTime) },
            color = if (secondsLeft <= 5) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header instructions
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (previousItem == null) "Remember this first symbol!" else "Does this match the PREVIOUS symbol?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (streak > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔥 Combo Streak: $streak!",
                        color = Color(0xFFF97316),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Big Item Display Box
            AnimatedContent(
                targetState = currentItem,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "symbol_switch"
            ) { item ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(text = item, fontSize = 84.sp)
                }
            }

            // Answer Buttons
            if (previousItem == null) {
                Button(
                    onClick = {
                        previousItem = currentItem
                        currentItem = pool.random()
                        soundManager.playClick()
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("speed_match_ready")
                ) {
                    Text(text = "I'm Ready! Let's Go!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // NO button
                    Button(
                        onClick = { handleAnswer(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("speed_match_no")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "DIFFERENT", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }

                    // YES button
                    Button(
                        onClick = { handleAnswer(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("speed_match_yes")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "MATCH!", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
