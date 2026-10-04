package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sound.SoundManager
import kotlin.random.Random

private data class BattleQuestion(
    val prompt: String,
    val correctValue: String,
    val options: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerGame(
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var p1Score by remember { mutableIntStateOf(0) }
    var p2Score by remember { mutableIntStateOf(0) }
    var winner by remember { mutableStateOf<String?>(null) }

    fun generateQuestion(): BattleQuestion {
        val type = Random.nextInt(2)
        return if (type == 0) {
            val a = Random.nextInt(1, 9)
            val b = Random.nextInt(1, 9)
            val sum = a + b
            BattleQuestion(
                prompt = "$a + $b = ?",
                correctValue = "$sum",
                options = listOf("$sum", "${sum + 1}", "${sum - 1}").shuffled()
            )
        } else {
            val emojis = listOf("🦁", "🦊", "🚀", "🦄", "⭐").shuffled()
            val target = emojis[0]
            BattleQuestion(
                prompt = "Tap the $target!",
                correctValue = target,
                options = listOf(target, emojis[1], emojis[2]).shuffled()
            )
        }
    }

    var question by remember { mutableStateOf(generateQuestion()) }

    fun checkAnswer(player: Int, chosen: String) {
        if (winner != null) return
        if (chosen == question.correctValue) {
            soundManager.playSuccess()
            if (player == 1) p1Score++ else p2Score++

            if (p1Score >= 5) {
                winner = "Player 1 (Top)"
                soundManager.playFanfare()
            } else if (p2Score >= 5) {
                winner = "Player 2 (Bottom)"
                soundManager.playFanfare()
            } else {
                question = generateQuestion()
            }
        } else {
            soundManager.playError()
            // Deduct
            if (player == 1) p1Score = (p1Score - 1).coerceAtLeast(0)
            else p2Score = (p2Score - 1).coerceAtLeast(0)
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
                    text = "Local 2-Player Battle ⚔️",
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
                Text(
                    text = "P1: $p1Score  vs  P2: $p2Score",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        )

        // Split screen: Top Player (Player 1 rotated 180 degrees)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE0E7FF))
                .rotate(180f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Player 1 (Score: $p1Score/5)", fontWeight = FontWeight.Bold, color = Color(0xFF3730A3))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = question.prompt, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.options.forEach { opt ->
                        BattleButton(label = opt, onClick = { checkAnswer(1, opt) })
                    }
                }
            }
        }

        HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.primary)

        // Split screen: Bottom Player (Player 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFFEF3C7))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Player 2 (Score: $p2Score/5)", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = question.prompt, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.options.forEach { opt ->
                        BattleButton(label = opt, onClick = { checkAnswer(2, opt) })
                    }
                }
            }
        }

        if (winner != null) {
            Surface(
                color = Color(0xFF10B981),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏆 $winner Wins the Brain Battle!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BattleButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .size(80.dp, 60.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
