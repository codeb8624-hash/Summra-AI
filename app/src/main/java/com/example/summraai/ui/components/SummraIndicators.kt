package com.example.summraai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.example.summraai.ui.theme.CornerRadius
import com.example.summraai.ui.theme.Spacing

@Composable
fun SummraLoadingIndicator(
    modifier: Modifier = Modifier,
    contentDescription: String = "Loading"
) {
    Box(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun SummraLinearProgress(
    modifier: Modifier = Modifier,
    contentDescription: String = "Progress"
) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.contentDescription = contentDescription
            },
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    lines: Int = 3
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val shimmerTranslate by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant
    )

    Column(modifier = modifier.padding(Spacing.md)) {
        repeat(lines) { index ->
            val width = when (index) {
                0 -> 0.9f
                lines - 1 -> 0.6f
                else -> 1.0f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(width)
                    .height(if (index == 0) 20.dp else 14.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        brush = Brush.linearGradient(
                            colors = shimmerColors,
                            start = Offset(shimmerTranslate, 0f),
                            end = Offset(shimmerTranslate + 300f, 0f)
                        )
                    )
            )
            if (index < lines - 1) {
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }
    }
}

@Composable
fun PulseLoader(
    modifier: Modifier = Modifier,
    dotCount: Int = 3
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "Loading"
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val transition = rememberInfiniteTransition(label = "pulse_$index")
            val scale by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = index * 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            Box(
                modifier = Modifier
                    .size(8.dp * scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (index < dotCount - 1) {
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
        }
    }
}
