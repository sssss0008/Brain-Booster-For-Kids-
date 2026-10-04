package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sound.SoundManager
import com.example.model.DifficultyLevel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogicPathGame(
    difficulty: DifficultyLevel,
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val gridSize = 4
    val startPos = Pair(0, 0)
    val goalPos = Pair(3, 3)

    // Obstacles
    val obstacles = remember(difficulty) {
        when (difficulty) {
            DifficultyLevel.EASY -> setOf(Pair(1, 1), Pair(2, 2))
            DifficultyLevel.MEDIUM -> setOf(Pair(0, 2), Pair(2, 1), Pair(1, 3))
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> setOf(Pair(0, 2), Pair(2, 1), Pair(1, 3), Pair(3, 1))
        }
    }

    var robotPos by remember { mutableStateOf(startPos) }
    var movesCount by remember { mutableIntStateOf(0) }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        soundManager.speak("Guide the robot to the golden star without hitting the rocks!")
        while (robotPos != goalPos) {
            delay(1000)
            secondsElapsed++
        }
    }

    fun moveRobot(dx: Int, dy: Int) {
        val newX = (robotPos.first + dx).coerceIn(0, gridSize - 1)
        val newY = (robotPos.second + dy).coerceIn(0, gridSize - 1)
        val newPos = Pair(newX, newY)

        if (obstacles.contains(newPos)) {
            soundManager.playError()
            return
        }

        soundManager.playClick()
        robotPos = newPos
        movesCount++

        if (robotPos == goalPos) {
            soundManager.playSuccess()
            val minMoves = 6
            val stars = when {
                movesCount <= minMoves + 2 -> 3
                movesCount <= minMoves + 6 -> 2
                else -> 1
            }
            val baseScore = 220
            val totalScore = ((baseScore + (30 - secondsElapsed).coerceAtLeast(0) * 2) * difficulty.multiplier).toInt()
            onFinish(totalScore, 95, secondsElapsed, stars)
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
                    text = "Logic Path 🛤️",
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
                IconButton(onClick = {
                    robotPos = startPos
                    soundManager.playClick()
                }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset position")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Moves: $movesCount | Goal: Reach the ⭐",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                for (r in 0 until gridSize) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until gridSize) {
                            val pos = Pair(c, r)
                            val isRobot = robotPos == pos
                            val isGoal = goalPos == pos
                            val isRock = obstacles.contains(pos)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isRobot -> MaterialTheme.colorScheme.primaryContainer
                                            isGoal -> Color(0xFFFEF08A)
                                            isRock -> Color(0xFFE2E8F0)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .border(
                                        width = if (isRobot) 2.dp else 1.dp,
                                        color = if (isRobot) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                when {
                                    isRobot -> Text(text = "🤖", fontSize = 28.sp)
                                    isGoal -> Text(text = "⭐", fontSize = 28.sp)
                                    isRock -> Text(text = "🧱", fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }
            }

            // D-Pad Arrow Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { moveRobot(0, -1) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("logic_btn_up")
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Up", modifier = Modifier.size(32.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(
                        onClick = { moveRobot(-1, 0) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("logic_btn_left")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Left", modifier = Modifier.size(32.dp))
                    }
                    Button(
                        onClick = { moveRobot(0, 1) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("logic_btn_down")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Down", modifier = Modifier.size(32.dp))
                    }
                    Button(
                        onClick = { moveRobot(1, 0) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("logic_btn_right")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Right", modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
