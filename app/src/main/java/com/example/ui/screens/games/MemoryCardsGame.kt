package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sound.SoundManager
import com.example.model.DifficultyLevel
import kotlinx.coroutines.delay

private data class CardItem(
    val id: Int,
    val emoji: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryCardsGame(
    difficulty: DifficultyLevel,
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val emojisPool = listOf(
        Pair("🦁", Color(0xFFFED7AA)),
        Pair("🦊", Color(0xFFFFEDD5)),
        Pair("🚀", Color(0xFFBAE6FD)),
        Pair("🦄", Color(0xFFFCE7F3)),
        Pair("🦖", Color(0xFFD1FAE5)),
        Pair("🤖", Color(0xFFEDE9FE)),
        Pair("🦉", Color(0xFFFEF3C7)),
        Pair("👑", Color(0xFFFEF08A))
    )

    val pairsCount = when (difficulty) {
        DifficultyLevel.EASY -> 3 // 6 cards
        DifficultyLevel.MEDIUM -> 4 // 8 cards
        DifficultyLevel.HARD -> 6 // 12 cards
        DifficultyLevel.EXPERT -> 8 // 16 cards
    }

    val cards = remember(difficulty) {
        val selected = emojisPool.shuffled().take(pairsCount)
        val combined = selected + selected
        combined.shuffled().mapIndexed { index, pair ->
            CardItem(index, pair.first, pair.second)
        }
    }

    val flippedIndices = remember { mutableStateListOf<Int>() }
    val matchedIndices = remember { mutableStateListOf<Int>() }
    var movesCount by remember { mutableIntStateOf(0) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(Unit) {
        soundManager.speak("Find the matching pairs!")
        while (matchedIndices.size < cards.size) {
            delay(1000)
            secondsElapsed++
        }
    }

    // Check completion
    LaunchedEffect(matchedIndices.size) {
        if (matchedIndices.size == cards.size && cards.isNotEmpty()) {
            val perfectMoves = pairsCount * 2
            val accuracy = ((perfectMoves.toFloat() / movesCount.coerceAtLeast(perfectMoves)) * 100).toInt().coerceIn(40, 100)
            val stars = when {
                movesCount <= pairsCount + 2 -> 3
                movesCount <= pairsCount * 2 -> 2
                else -> 1
            }
            val baseScore = pairsCount * 40
            val speedBonus = (100 - secondsElapsed).coerceAtLeast(10)
            val totalScore = ((baseScore + speedBonus) * difficulty.multiplier).toInt()

            delay(600)
            onFinish(totalScore, accuracy, secondsElapsed, stars)
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
                    text = "Find The Pair 🃏",
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
                // Timer & moves badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "⏱️ ${secondsElapsed}s", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Moves: $movesCount", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of cards
        val columns = if (cards.size <= 8) 2 else if (cards.size <= 12) 3 else 4
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            itemsIndexed(cards) { index, card ->
                val isFlipped = flippedIndices.contains(index) || matchedIndices.contains(index)
                val isMatched = matchedIndices.contains(index)

                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(350),
                    label = "card_flip"
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlipped) card.color else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .aspectRatio(0.85f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable(enabled = !isFlipped && !isChecking) {
                            soundManager.playClick()
                            flippedIndices.add(index)
                            movesCount++

                            if (flippedIndices.size == 2) {
                                isChecking = true
                                val first = cards[flippedIndices[0]]
                                val second = cards[flippedIndices[1]]

                                if (first.emoji == second.emoji) {
                                    // Matched!
                                    soundManager.playSuccess()
                                    matchedIndices.add(flippedIndices[0])
                                    matchedIndices.add(flippedIndices[1])
                                    flippedIndices.clear()
                                    isChecking = false
                                } else {
                                    // Not matched
                                    soundManager.playError()
                                    // Wait and flip back
                                }
                            }
                        }
                        .testTag("memory_card_$index")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation > 90f) {
                            Text(
                                text = card.emoji,
                                fontSize = if (cards.size > 12) 32.sp else 44.sp,
                                modifier = Modifier.graphicsLayer { rotationY = 180f }
                            )
                        } else {
                            Text(
                                text = "❓",
                                fontSize = 32.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Auto flip back if 2 cards selected and not matched
    LaunchedEffect(flippedIndices.size) {
        if (flippedIndices.size == 2) {
            val first = cards[flippedIndices[0]]
            val second = cards[flippedIndices[1]]
            if (first.emoji != second.emoji) {
                delay(900)
                flippedIndices.clear()
                isChecking = false
            }
        }
    }
}
