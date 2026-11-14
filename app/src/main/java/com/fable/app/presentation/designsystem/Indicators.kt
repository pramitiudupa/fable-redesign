package com.fable.app.presentation.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableThemeTokens
import kotlinx.coroutines.launch

/** Read-only star rating that supports fractional values (e.g. 3.7). */
@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 14.dp,
    color: Color = FableThemeTokens.current.gilt,
    emptyColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
) {
    val clamped = rating.coerceIn(0.0, 5.0).toFloat()
    Row(
        modifier.semantics { contentDescription = "Rated ${"%.1f".format(clamped)} out of 5" },
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        repeat(5) { index ->
            val fill = (clamped - index).coerceIn(0f, 1f)
            Box(Modifier.size(starSize)) {
                Icon(FableIcons.Star, null, tint = emptyColor, modifier = Modifier.fillMaxSize())
                if (fill > 0f) {
                    Icon(
                        FableIcons.Star,
                        null,
                        tint = color,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                clipRect(right = size.width * fill) { this@drawWithContent.drawContent() }
                            },
                    )
                }
            }
        }
    }
}

/**
 * Interactive rating: each star springs when chosen and the selection triggers a haptic tick.
 * Stars meet the 48dp touch-target guideline.
 */
@Composable
fun RatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = FableThemeTokens.current.gilt,
    starSize: Dp = 34.dp,
) {
    val haptics = rememberFableHaptics()
    val words = listOf("", "Not for me", "It was fine", "Liked it", "Really liked it", "Loved it")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            (1..5).forEach { value ->
                val selected = value <= rating
                val scale = remember { Animatable(1f) }
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.RadioButton,
                        ) {
                            haptics.tick()
                            onRatingChange(value)
                            scope.launch {
                                scale.snapTo(0.6f)
                                scale.animateTo(1f, FableMotion.bouncySpring())
                            }
                        }
                        .semantics {
                            contentDescription = "$value star${if (value == 1) "" else "s"}"
                            stateDescription = if (value == rating) "Selected" else "Not selected"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (selected) FableIcons.Star else FableIcons.StarOutline,
                        null,
                        tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(starSize)
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            },
                    )
                }
            }
        }
        Text(
            if (rating in 1..5) words[rating] else "Tap a star to rate",
            style = MaterialTheme.typography.labelMedium,
            color = if (rating in 1..5) color else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Animated circular progress with a soft track — used for reading progress. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    stroke: Dp = 5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.14f),
    content: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FableMotion.Emphasized),
        label = "ring",
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(trackColor, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(strokePx))
            drawArc(
                color, -90f, 360f * animated, false, Offset(inset, inset), arcSize,
                style = Stroke(strokePx, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/** Animated linear progress with a gradient fill. */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = color,
    trackColor: Color = color.copy(alpha = 0.14f),
    height: Dp = 6.dp,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FableMotion.Emphasized),
        label = "bar",
    )
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
            .semantics { stateDescription = "${(progress * 100).toInt()} percent" },
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .size(width = maxWidth * animated, height = height)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color, secondaryColor))),
        )
    }
}

/**
 * Reader avatar: the profile photo when provided, otherwise a monogram on a gradient medallion.
 * A thin ring in the accent colour frames both.
 */
@Composable
fun ReaderAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    accent: Color = MaterialTheme.colorScheme.primary,
    ring: Boolean = true,
) {
    val tokens = FableThemeTokens.current
    val initials = name.split(' ', '_', '.').filter(String::isNotBlank).take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "R" }
    Box(
        modifier
            .size(size)
            .then(if (ring) Modifier.border(1.5.dp, Brush.sweepGradient(listOf(accent, tokens.gilt, accent)), CircleShape) else Modifier)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.85f), tokens.gilt.copy(alpha = 0.75f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            style = if (size >= 72.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
            color = com.fable.app.presentation.theme.readableOn(accent),
        )
        if (!imageUrl.isNullOrBlank()) {
            coil3.compose.AsyncImage(
                model = imageUrl,
                contentDescription = "$name's profile picture",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        }
    }
}
