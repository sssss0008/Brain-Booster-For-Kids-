package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

private data class MathProblem(
    val equationText: String,
    val visualEmoji: String = "",
    val visualCount: Int = 0,
    val correctAnswer: Int,
    val options: List<Int>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MentalMathGame(
    difficulty: DifficultyLevel,
    soundManager: SoundManager,
    onFinish: (score: Int, accuracy: Int, timeSec: Int, stars: Int) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val totalRounds = 5
    var currentRoundIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var errorsCount by remember { mutableIntStateOf(0) }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    fun generateProblem(): MathProblem {
        return when (difficulty) {
            DifficultyLevel.EASY -> {
                val isCounting = Random.nextBoolean()
                if (isCounting) {
                    val count = Random.nextInt(3, 10)
                    val wrong1 = (count + 1)
                    val wrong2 = (count - 1).coerceAtLeast(1)
                    val wrong3 = (count + 2)
                    MathProblem(
                        equationText = "Count the delicious apples! 🍎",
                        visualEmoji = "🍎",
                        visualCount = count,
                        correctAnswer = count,
                        options = listOf(count, wrong1, wrong2, wrong3).shuffled()
                    )
                } else {
                    val a = Random.nextInt(1, 6)
                    val b = Random.nextInt(1, 6)
                    val ans = a + b
                    val options = listOf(ans, ans + 1, (ans - 1).coerceAtLeast(0), ans + 2).distinct().shuffled()
                    MathProblem(
                        equationText = "$a + $b = ?",
                        visualEmoji = "⭐",
                        visualCount = a + b,
                        correctAnswer = ans,
                        options = options
                    )
                }
            }
            DifficultyLevel.MEDIUM -> {
                val isAdd = Random.nextBoolean()
                if (isAdd) {
                    val a = Random.nextInt(5, 18)
                    val b = Random.nextInt(3, 15)
                    val ans = a + b
                    val options = listOf(ans, ans + 2, (ans - 1), ans + 10).distinct().shuffled()
                    MathProblem("$a + $b = ?", correctAnswer = ans, options = options)
                } else {
                    val a = Random.nextInt(10, 25)
                    val b = Random.nextInt(2, a)
                    val ans = a - b
                    val options = listOf(ans, ans + 1, (ans - 2).coerceAtLeast(0), ans + 3).distinct().shuffled()
                    MathProblem("$a - $b = ?", correctAnswer = ans, options = options)
                }
            }
            DifficultyLevel.HARD, DifficultyLevel.EXPERT -> {
                val type = Random.nextInt(3)
                when (type) {
                    0 -> {
                        // Missing number: a + _ = c
                        val a = Random.nextInt(12, 40)
                        val b = Random.nextInt(5, 25)
                        val c = a + b
                        val options = listOf(b, b + 2, b - 1, b + 5).distinct().shuffled()
                        MathProblem("$a + [ ? ] = $c", correctAnswer = b, options = options)
                    }
                    1 -> {
                        // Quick multiplication: 3 * 7
                        val a = Random.nextInt(3, 9)
                        val b = Random.nextInt(2, 9)
                        val ans = a * b
                        val options = listOf(ans, ans + a, ans - b, ans + 4).distinct().shuffled()
                        MathProblem("$a × $b = ?", correctAnswer = ans, options = options)
                    }
                    else -> {
                        val a = Random.nextInt(20, 60)
                        val b = Random.nextInt(10, 30)
                        val ans = a - b
                        val options = listOf(ans, ans + 1, ans - 2, ans + 10).distinct().shuffled()
                        MathProblem("$a - $b = ?", correctAnswer = ans, options = options)
                    }
                }
            }
        }
    }

    var currentProblem by remember { mutableStateOf(generateProblem()) }

    LaunchedEffect(Unit) {
        soundManager.speak("Solve the quick math challenge!")
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    fun handleOptionSelected(chosen: Int) {
        if (chosen == currentProblem.correctAnswer) {
            soundManager.playSuccess()
            score += 40
            if (currentRoundIndex + 1 < totalRounds) {
                currentRoundIndex++
                currentProblem = generateProblem()
            } else {
                val accuracy = (100 - (errorsCount * 15)).coerceIn(40, 100)
                val stars = when {
                    errorsCount == 0 -> 3
                    errorsCount <= 2 -> 2
                    else -> 1
                }
                val totalScore = (score * difficulty.multiplier).toInt()
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
                    text = "Mental Math Kids 🔢",
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
                    color = Color(0xFFD1FAE5),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Question ${currentRoundIndex + 1}/$totalRounds",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46),
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
            // Problem Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentProblem.equationText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    // Optional visual aid for young kids
                    if (currentProblem.visualCount > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(currentProblem.visualCount) {
                                Text(
                                    text = currentProblem.visualEmoji,
                                    fontSize = 28.sp,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Options Bubbles 2x2
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val chunks = currentProblem.options.chunked(2)
                chunks.forEach { rowOptions ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowOptions.forEach { opt ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { handleOptionSelected(opt) }
                                    .testTag("math_option_$opt")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = "$opt",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
