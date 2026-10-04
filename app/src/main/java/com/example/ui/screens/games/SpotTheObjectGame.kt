package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sound.SoundManager
import com.example.model.DifficultyLevel
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class OddPuzzle(
    val commonEmoji: String,
    val oddEmoji: String,
    val prompt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotTheObjectGame(
    difficulty: DifficultyLevel,
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val puzzles = remember {
        listOf(
            OddPuzzle("🐶", "🐱", "Find the Kitten hidden among Puppies!"),
            OddPuzzle("⭐", "🌟", "Spot the Sparkling Star!"),
            OddPuzzle("🍎", "🍓", "Find the Strawberry in the Apple basket!"),
            OddPuzzle("🚗", "🏎️", "Find the Fast Race Car!"),
            OddPuzzle("🎈", "🎁", "Spot the Mystery Gift Box!"),
            OddPuzzle("🦁", "🐯", "Find the Tiger among Lions!"),
            OddPuzzle("⚽", "🏀", "Find the Basketball among Soccer balls!")
        ).shuffled()
    }

    val totalRounds = 5
    var currentRoundIndex by remember { mutableIntStateOf(0) }
    val gridSize = when (difficulty) {
        DifficultyLevel.EASY -> 9 // 3x3
        DifficultyLevel.MEDIUM -> 12 // 3x4
        DifficultyLevel.HARD -> 16 // 4x4
        DifficultyLevel.EXPERT -> 20 // 4x5
    }

    val currentPuzzle = puzzles[currentRoundIndex % puzzles.size]
    val oddIndex = remember(currentRoundIndex, difficulty) {
        Random.nextInt(gridSize)
    }

    var secondsElapsed by remember { mutableIntStateOf(0) }
    var errorsCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        soundManager.speak("Find the odd object as fast as you can!")
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    fun onTileClick(index: Int) {
        if (index == oddIndex) {
            soundManager.playSuccess()
            if (currentRoundIndex + 1 < totalRounds) {
                currentRoundIndex++
            } else {
                // Game finished
                val accuracy = (100 - (errorsCount * 15)).coerceIn(40, 100)
                val baseScore = 200
                val speedBonus = (60 - secondsElapsed).coerceAtLeast(10) * 3
                val totalScore = ((baseScore + speedBonus) * difficulty.multiplier).toInt()
                val stars = when {
                    errorsCount == 0 && secondsElapsed <= 20 -> 3
                    errorsCount <= 2 -> 2
                    else -> 1
                }
                onFinish(totalScore, accuracy, secondsElapsed, stars)
            }
        } else {
            soundManager.playError()
            errorsCount++
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
                    text = "Spot The Object 🔍",
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
                        text = "Round ${currentRoundIndex + 1}/$totalRounds",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Prompt message
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = currentPuzzle.prompt,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of objects
        val cols = if (gridSize <= 9) 3 else 4
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(gridSize) { i ->
                val emoji = if (i == oddIndex) currentPuzzle.oddEmoji else currentPuzzle.commonEmoji
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTileClick(i) }
                        .testTag("spot_tile_$i")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = emoji,
                            fontSize = if (gridSize > 12) 32.sp else 40.sp
                        )
                    }
                }
            }
        }
    }
}
