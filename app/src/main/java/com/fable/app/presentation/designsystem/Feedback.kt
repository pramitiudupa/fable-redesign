package com.fable.app.presentation.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableThemeTokens

/** Shimmering placeholder block. One infinite transition per block keeps it cheap and local. */
@Composable
fun Modifier.shimmer(shape: Shape = MaterialTheme.shapes.small, tint: Color? = null): Modifier {
    val tokens = FableThemeTokens.current
    val base = tint ?: MaterialTheme.colorScheme.onSurface.copy(alpha = if (tokens.dark) 0.08f else 0.06f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = if (tokens.dark) 0.16f else 0.12f)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    return this
        .clip(shape)
        .background(base)
        .drawWithContent {
            val x = size.width * progress
            drawRect(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = Offset(x - size.width * 0.6f, 0f),
                    end = Offset(x, size.height),
                ),
            )
            drawContent()
        }
}

@Composable
fun SkeletonLine(width: Dp, modifier: Modifier = Modifier, height: Dp = 12.dp) {
    Box(modifier.width(width).height(height).shimmer(CircleShape))
}

/** Skeleton matching [com.fable.app.presentation.books.components.BookCard]. */
@Composable
fun BookCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.semantics { contentDescription = "Loading book" }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(FableProportions.COVER_ASPECT).shimmer(MaterialTheme.shapes.small))
        SkeletonLine(120.dp, height = 13.dp)
        SkeletonLine(80.dp, height = 10.dp)
    }
}

/** Skeleton matching a horizontal list row with a small cover. */
@Composable
fun BookRowSkeleton(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(72.dp).aspectRatio(FableProportions.COVER_ASPECT).shimmer(MaterialTheme.shapes.small))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SkeletonLine(180.dp, height = 14.dp)
            SkeletonLine(110.dp, height = 11.dp)
            SkeletonLine(150.dp, height = 8.dp)
        }
    }
}

/** A breathing ornament — Fable's loading mark, used where a skeleton would be meaningless. */
@Composable
fun FableLoading(message: String, modifier: Modifier = Modifier) {
    val tokens = FableThemeTokens.current
    val transition = rememberInfiniteTransition(label = "loading")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "loadingPulse",
    )
    val turn by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "loadingTurn",
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp).semantics { contentDescription = message },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .graphicsLayer { rotationZ = turn }
                .border(1.dp, tokens.gilt.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                tokens.ornament.takeUnless { it == "—" } ?: "✦",
                style = MaterialTheme.typography.headlineSmall,
                color = tokens.gilt,
                modifier = Modifier.graphicsLayer {
                    alpha = pulse
                    scaleX = pulse
                    scaleY = pulse
                    rotationZ = -turn
                },
            )
        }
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Considered empty state: an ornamental medallion, a serif headline, a gentle explanation and an
 * optional next step. Never a dead end.
 */
@Composable
fun FableEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    ornament: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val tokens = FableThemeTokens.current
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.20f), accent.copy(alpha = 0.04f))))
                .border(1.dp, accent.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) Icon(icon, null, tint = accent, modifier = Modifier.size(32.dp))
            else Text(ornament ?: tokens.ornament, style = MaterialTheme.typography.headlineMedium, color = accent)
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            FableButton(actionLabel, onAction, style = FableButtonStyle.Tonal, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
fun FableErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    FableEmptyState(
        title = "Something went astray",
        message = message,
        modifier = modifier,
        icon = FableIcons.Alert,
        accent = MaterialTheme.colorScheme.error,
        actionLabel = "Try again",
        onAction = onRetry,
    )
}
