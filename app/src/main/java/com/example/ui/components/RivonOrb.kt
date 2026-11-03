package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.RivonOrbState
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonErrorRed
import com.example.ui.theme.RivonNeonPurple
import com.example.ui.theme.RivonSoftCyan
import com.example.ui.theme.RivonWarningAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RivonOrb(
    state: RivonOrbState,
    rmsAudioLevel: Float = 0f,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfiniteTransition")

    // Idle breathing
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    // Continuous rotation for thinking & cyber rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                when (state) {
                    RivonOrbState.THINKING -> 1200
                    RivonOrbState.EXECUTING -> 2000
                    RivonOrbState.LISTENING -> 4000
                    else -> 10000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                when (state) {
                    RivonOrbState.THINKING -> 1800
                    RivonOrbState.EXECUTING -> 2400
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterRotation"
    )

    // Speaking wave pulsing
    val speakingWave by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakingWave"
    )

    // Audio level animated spring
    val animatedRms = remember { Animatable(0f) }
    LaunchedEffect(rmsAudioLevel) {
        animatedRms.animateTo(rmsAudioLevel, tween(100))
    }

    Box(
        modifier = Modifier
            .size(size)
            .testTag("rivon_ai_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.4f

            val primaryColor: Color
            val secondaryColor: Color
            val glowColor: Color

            when (state) {
                RivonOrbState.IDLE -> {
                    primaryColor = RivonCyan
                    secondaryColor = RivonElectricBlue
                    glowColor = Color(0x3300F0FF)
                }
                RivonOrbState.LISTENING -> {
                    primaryColor = RivonCyan
                    secondaryColor = RivonSoftCyan
                    glowColor = Color(0x6600F0FF)
                }
                RivonOrbState.THINKING -> {
                    primaryColor = RivonElectricBlue
                    secondaryColor = RivonNeonPurple
                    glowColor = Color(0x558B5CF6)
                }
                RivonOrbState.SPEAKING -> {
                    primaryColor = RivonCyan
                    secondaryColor = RivonElectricBlue
                    glowColor = Color(0x662979FF)
                }
                RivonOrbState.EXECUTING -> {
                    primaryColor = RivonElectricBlue
                    secondaryColor = RivonCyan
                    glowColor = Color(0x6600E5FF)
                }
                RivonOrbState.ERROR -> {
                    primaryColor = RivonErrorRed
                    secondaryColor = RivonWarningAmber
                    glowColor = Color(0x44EF4444)
                }
            }

            // Outer soft atmospheric glow
            val scaleModifier = when (state) {
                RivonOrbState.IDLE -> idlePulse
                RivonOrbState.LISTENING -> 1.0f + (animatedRms.value * 0.45f)
                RivonOrbState.SPEAKING -> speakingWave
                RivonOrbState.THINKING -> 1.0f + ((sin(Math.toRadians(rotationAngle.toDouble())) * 0.08).toFloat())
                else -> 1.0f
            }

            val currentCoreRadius = baseRadius * scaleModifier

            // Layer 1: Ambient radial aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = currentCoreRadius * 2.2f
                ),
                radius = currentCoreRadius * 2.2f,
                center = center
            )

            // Layer 2: Outer segmented cyber rings
            val ringRadius = currentCoreRadius * 1.45f
            val segments = 4
            for (i in 0 until segments) {
                val startArc = rotationAngle + (i * (360f / segments))
                drawArc(
                    color = primaryColor.copy(alpha = 0.7f),
                    startAngle = startArc,
                    sweepAngle = 45f,
                    useCenter = false,
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                    size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Layer 3: Inner counter-rotating ring
            val innerRingRadius = currentCoreRadius * 1.2f
            for (i in 0 until 3) {
                val startArc = counterRotation + (i * 120f)
                drawArc(
                    color = secondaryColor.copy(alpha = 0.85f),
                    startAngle = startArc,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - innerRingRadius, center.y - innerRingRadius),
                    size = androidx.compose.ui.geometry.Size(innerRingRadius * 2, innerRingRadius * 2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Layer 4: Floating orbital nodes
            val nodeCount = 5
            for (i in 0 until nodeCount) {
                val angle = Math.toRadians((rotationAngle + (i * 360f / nodeCount)).toDouble())
                val nodeOffset = Offset(
                    center.x + (cos(angle) * ringRadius).toFloat(),
                    center.y + (sin(angle) * ringRadius).toFloat()
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = nodeOffset
                )
            }

            // Layer 5: Luminous sphere core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        secondaryColor,
                        Color(0xFF060D20)
                    ),
                    center = Offset(center.x - currentCoreRadius * 0.25f, center.y - currentCoreRadius * 0.25f),
                    radius = currentCoreRadius
                ),
                radius = currentCoreRadius,
                center = center
            )

            // Layer 6: Geometric central quantum nexus
            val diamondSize = currentCoreRadius * 0.35f
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y - diamondSize)
                lineTo(center.x + diamondSize, center.y)
                lineTo(center.x, center.y + diamondSize)
                lineTo(center.x - diamondSize, center.y)
                close()
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
