package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Particle(
    val xRatio: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val initialYOffset: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFFEF4444),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFFFFD700)
    )

    val particles = remember {
        List(40) {
            Particle(
                xRatio = Random.nextFloat(),
                speed = 0.5f + Random.nextFloat() * 0.8f,
                size = 12f + Random.nextFloat() * 16f,
                color = colors[Random.nextInt(colors.size)],
                initialYOffset = Random.nextFloat()
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_fall"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val yPos = ((p.initialYOffset + progress * p.speed) % 1.0f) * h
            val xPos = (p.xRatio * w + kotlin.math.sin(progress * 6.28f * 2 + p.xRatio * 10) * 20f).toFloat()

            drawRoundRect(
                color = p.color,
                topLeft = Offset(xPos, yPos),
                size = Size(p.size, p.size * 0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}
