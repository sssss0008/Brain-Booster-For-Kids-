package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
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

private data class VisualIqQuestion(
    val mainItem: String,
    val options: List<String>,
    val correctIndex: Int,
    val title: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualIqGame(
    difficulty: DifficultyLevel,
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val questions = remember {
        listOf(
            VisualIqQuestion(
                mainItem = "🚀",
                options = listOf("🛸", "✈️", "🚀", "🚁"),
                correctIndex = 2,
                title = "Match the Rocket silhouette!"
            ),
            VisualIqQuestion(
                mainItem = "🦁",
                options = listOf("🦁", "🐯", "🐱", "🐻"),
                correctIndex = 0,
                title = "Identify the exact Mighty Lion!"
            ),
            VisualIqQuestion(
                mainItem = "⭐",
                options = listOf("🌙", "☀️", "⚡", "⭐"),
                correctIndex = 3,
                title = "Spot the identical Golden Star!"
            ),
            VisualIqQuestion(
                mainItem = "🦄",
                options = listOf("🐴", "🦄", "🦓", "🦌"),
                correctIndex = 1,
                title = "Find the Magical Unicorn!"
            ),
            VisualIqQuestion(
                mainItem = "🧩",
                options = listOf("🎲", "🎯", "🧩", "🎳"),
                correctIndex = 2,
                title = "Which puzzle piece fits perfectly?"
            )
        )
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var errorsCount by remember { mutableIntStateOf(0) }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        soundManager.speak("Visual IQ Test! Find the matching shape or silhouette!")
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    val currentQ = questions[currentQuestionIndex]

    fun onOptionSelected(index: Int) {
        if (index == currentQ.correctIndex) {
            soundManager.playSuccess()
            if (currentQuestionIndex + 1 < questions.size) {
                currentQuestionIndex++
            } else {
                val accuracy = (100 - (errorsCount * 20)).coerceIn(40, 100)
                val baseScore = 200
                val stars = when {
                    errorsCount == 0 -> 3
                    errorsCount <= 2 -> 2
                    else -> 1
                }
                val totalScore = (baseScore * difficulty.multiplier).toInt()
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
                    text = "Visual IQ Kids 💡",
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
                        text = "Question ${currentQuestionIndex + 1}/${questions.size}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
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
                text = currentQ.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Main object card
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.size(160.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = currentQ.mainItem, fontSize = 72.sp)
                }
            }

            Text(
                text = "Pick the exact match below:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 4 Options Grid (2x2)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VisualOptionCard(
                        emoji = currentQ.options[0],
                        onClick = { onOptionSelected(0) },
                        modifier = Modifier.weight(1f),
                        tag = "visual_opt_0"
                    )
                    VisualOptionCard(
                        emoji = currentQ.options[1],
                        onClick = { onOptionSelected(1) },
                        modifier = Modifier.weight(1f),
                        tag = "visual_opt_1"
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VisualOptionCard(
                        emoji = currentQ.options[2],
                        onClick = { onOptionSelected(2) },
                        modifier = Modifier.weight(1f),
                        tag = "visual_opt_2"
                    )
                    VisualOptionCard(
                        emoji = currentQ.options[3],
                        onClick = { onOptionSelected(3) },
                        modifier = Modifier.weight(1f),
                        tag = "visual_opt_3"
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualOptionCard(
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 42.sp)
        }
    }
}
