package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionExecutionStatus
import com.example.model.ActionRiskLevel
import com.example.model.PlannedAction
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonErrorRed
import com.example.ui.theme.RivonGlassBorder
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonSurface
import com.example.ui.theme.RivonSurfaceElevated
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary
import com.example.ui.theme.RivonWarningAmber

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = RivonGlassBorder,
    backgroundColor: Color = RivonSurface.copy(alpha = 0.85f),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.border(
            BorderStroke(1.dp, borderColor),
            RoundedCornerShape(16.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        content()
    }
}

@Composable
fun RivonStatusBadge(
    label: String,
    icon: ImageVector? = null,
    color: Color = RivonCyan,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it },
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AudioWaveformIndicator(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "WaveformTransition")

    Row(
        modifier = modifier.height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 5
        for (i in 0 until barCount) {
            val heightPercent by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = if (isSpeaking) 1.0f else 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(250 + (i * 70), easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight_$i"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((24 * heightPercent).dp)
                    .background(
                        Brush.verticalGradient(listOf(RivonCyan, RivonElectricBlue)),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun ActionCardView(
    planned: PlannedAction,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val riskColor = when (planned.action.riskLevel) {
        ActionRiskLevel.LOW -> RivonNeonGreen
        ActionRiskLevel.MEDIUM -> RivonWarningAmber
        ActionRiskLevel.HIGH -> RivonErrorRed
    }

    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = when (planned.status) {
            ActionExecutionStatus.SUCCESS -> RivonNeonGreen.copy(alpha = 0.5f)
            ActionExecutionStatus.FAILED -> RivonErrorRed.copy(alpha = 0.5f)
            ActionExecutionStatus.PENDING_CONFIRMATION -> RivonWarningAmber.copy(alpha = 0.6f)
            else -> RivonBorder
        }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = planned.action.title,
                    color = RivonTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                RivonStatusBadge(
                    label = planned.action.riskLevel.name + " RISK",
                    color = riskColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = planned.action.description,
                color = RivonTextSecondary,
                fontSize = 13.sp
            )

            if (planned.resultMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = planned.resultMessage!!,
                    color = if (planned.status == ActionExecutionStatus.SUCCESS) RivonNeonGreen else RivonErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (planned.status == ActionExecutionStatus.PENDING_CONFIRMATION && onConfirm != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCancel?.invoke() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_cancel_button"),
                        border = BorderStroke(1.dp, RivonBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RivonTextSecondary)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onConfirm() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_confirm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RivonElectricBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirm", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
