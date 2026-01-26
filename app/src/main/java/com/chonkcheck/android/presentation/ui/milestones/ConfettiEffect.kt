package com.chonkcheck.android.presentation.ui.milestones

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.chonkcheck.android.domain.model.MilestoneType
import com.chonkcheck.android.ui.theme.Amber
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple
import com.chonkcheck.android.ui.theme.Teal
import kotlin.random.Random

/**
 * Confetti particle data.
 */
private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val color: Color,
    val size: Float,
    val speedY: Float,
    val drift: Float,
    val rotationSpeed: Float
)

/**
 * Confetti animation effect for milestone celebrations.
 *
 * @param milestoneType Type of milestone (weekly has 30 particles, monthly has 50)
 * @param onAnimationEnd Callback when animation completes
 * @param modifier Modifier for styling
 */
@Composable
fun ConfettiEffect(
    milestoneType: MilestoneType,
    onAnimationEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val particleCount = when (milestoneType) {
        MilestoneType.WEEKLY -> 30
        MilestoneType.MONTHLY -> 50
    }

    val confettiColors = listOf(
        ChonkGreen,
        Coral,
        Amber,
        Purple,
        Teal
    )

    val particles = remember {
        (0 until particleCount).map {
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = -Random.nextFloat() * 0.3f, // Start above screen
                color = confettiColors.random(),
                size = Random.nextFloat() * 8f + 4f,
                speedY = Random.nextFloat() * 200f + 300f,
                drift = Random.nextFloat() * 100f - 50f,
                rotationSpeed = Random.nextFloat() * 360f - 180f
            )
        }
    }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        )
        onAnimationEnd()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val progress = animationProgress.value

        particles.forEach { particle ->
            val x = particle.startX * canvasWidth + particle.drift * progress
            val y = particle.startY * canvasHeight + particle.speedY * progress * (canvasHeight / 500f)

            // Only draw if on screen
            if (y < canvasHeight + 50f && x > -50f && x < canvasWidth + 50f) {
                val rotation = particle.rotationSpeed * progress

                rotate(degrees = rotation, pivot = Offset(x, y)) {
                    // Draw confetti piece as a small rectangle
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(x - particle.size / 2, y - particle.size),
                        size = androidx.compose.ui.geometry.Size(particle.size, particle.size * 2)
                    )
                }
            }
        }
    }
}
