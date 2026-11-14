package com.fable.app.presentation.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.fable.app.presentation.theme.FableMotion
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.theme.CoverMotif
import com.fable.app.presentation.theme.Genre
import com.fable.app.presentation.theme.GenreColors
import com.fable.app.presentation.theme.GenreMatch
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.genreColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** A small genre badge: ornament + label in the genre's own badge colours. */
@Composable
fun GenreBadge(
    genre: Genre?,
    modifier: Modifier = Modifier,
    label: String? = null,
    compact: Boolean = false,
    onCover: Boolean = false,
) {
    val colors = genreColors(genre)
    val text = label ?: genre?.label ?: return
    val container = if (onCover) Color.Black.copy(alpha = 0.32f) else colors.badgeContainer
    val content = if (onCover) Color.White else colors.badgeContent
    Row(
        modifier
            .clip(CircleShape)
            .background(container)
            .then(if (onCover) Modifier.border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape) else Modifier)
            .padding(horizontal = if (compact) 8.dp else 11.dp, vertical = if (compact) 3.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(colors.ornament, style = MaterialTheme.typography.labelMedium, color = if (onCover) Color.White else colors.primary)
        Text(
            text,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Primary genre badge followed by small colour dots for secondary genres. */
@Composable
fun GenreBadgeRow(match: GenreMatch, modifier: Modifier = Modifier, compact: Boolean = false) {
    if (match.primary == null) return
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        GenreBadge(match.primary, compact = compact)
        match.secondary.forEach { secondary ->
            val c = genreColors(secondary)
            Box(
                Modifier
                    .size(if (compact) 8.dp else 10.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(c.coverGradient))
                    .semantics { contentDescription = "Also ${secondary.label}" },
            )
        }
    }
}

/**
 * A genre tile built from real book covers: one cover fills the tile as a soft backdrop,
 * a fan of up to three covers from that genre stands in front, and the genre's own colour
 * gradient washes over the bottom so the serif label always reads. Covers come from Open
 * Library via [GenreArtworkViewModel]; until they arrive (or if they can't load) the tile
 * shows the genre's gradient and motif instead.
 */
@Composable
fun GenreTile(
    genre: Genre,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val colors = genreColors(genre)
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberFableHaptics()
    val tokens = FableThemeTokens.current
    val artwork = LocalGenreArtwork.current
    LaunchedEffect(artwork, genre) { artwork?.request(genre) }
    val allCovers = artwork?.covers?.collectAsState()?.value
    val covers = allCovers?.get(genre).orEmpty()
    val ink = colors.coverGradient.last()
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        modifier = modifier.pressScale(interaction, 0.96f),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        interactionSource = interaction,
        shadowElevation = 6.dp,
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(colors.coverBrush)
                .drawBehind {
                    if (covers.isEmpty()) drawCoverMotif(colors.motif, colors.onCover.copy(alpha = 0.16f), colors.accent, genre.ordinal)
                },
        ) {
            val tileWidth = maxWidth
            val tileHeight = maxHeight
            AnimatedVisibility(
                visible = covers.isNotEmpty(),
                enter = fadeIn(tween(FableMotion.LONG_MS)) + slideInVertically(tween(FableMotion.LONG_MS, easing = FableMotion.EmphasizedDecelerate)) { it / 8 },
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Full-bleed backdrop from the first cover, tinted by the genre.
                    AsyncImage(
                        model = covers.firstOrNull(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            alpha = 0.42f
                            scaleX = 1.15f
                            scaleY = 1.15f
                        },
                    )
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(ink.copy(alpha = 0.25f), ink.copy(alpha = 0.55f)))))
                    CoverFan(
                        covers = covers,
                        tileWidth = tileWidth,
                        tileHeight = tileHeight,
                        large = large,
                        accent = colors.accent,
                    )
                }
            }
            // Genre wash under the label.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(if (large) 0.75f else 0.55f)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, ink.copy(alpha = 0.85f), ink.copy(alpha = 0.97f)))),
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(if (large) 0.55f else 1f)
                    .padding(if (large) 18.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(colors.ornament, color = colors.accent, style = MaterialTheme.typography.titleMedium)
                Text(
                    genre.label,
                    style = (if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge)
                        .copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (large) {
                    Text(
                        genre.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

/** Up to three real covers fanned like books propped on a display table. */
@Composable
private fun CoverFan(
    covers: List<String>,
    tileWidth: androidx.compose.ui.unit.Dp,
    tileHeight: androidx.compose.ui.unit.Dp,
    large: Boolean,
    accent: Color,
) {
    val coverHeight = if (large) tileHeight * 0.80f else tileHeight * 0.50f
    val coverWidth = coverHeight * 0.66f
    // Small tiles fan across the top centre; wide tiles fan on the right-hand side.
    val centerX = if (large) tileWidth - coverWidth * 1.05f else tileWidth / 2
    val top = if (large) (tileHeight - coverHeight) / 2 else 12.dp
    val spread = coverWidth * 0.58f
    val slots = when (covers.size) {
        1 -> listOf(0)
        2 -> listOf(-1, 1)
        else -> listOf(-1, 1, 0) // the centre cover is drawn last, on top
    }
    Box(Modifier.fillMaxSize()) {
        slots.forEachIndexed { index, slot ->
            val url = covers.getOrNull(if (covers.size >= 3) listOf(1, 2, 0)[index] else index) ?: return@forEachIndexed
            val isCentre = slot == 0
            val shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 4.dp, bottomEnd = 4.dp)
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(
                        x = centerX - coverWidth / 2 + spread * slot,
                        y = top + if (isCentre) 0.dp else coverHeight * 0.06f,
                    )
                    .size(width = coverWidth, height = coverHeight)
                    .graphicsLayer {
                        rotationZ = slot * 7f
                        val s = if (isCentre) 1.04f else 0.94f
                        scaleX = s
                        scaleY = s
                    }
                    .shadow(if (isCentre) 14.dp else 8.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                    .clip(shape)
                    .border(0.5.dp, accent.copy(alpha = 0.35f), shape),
            )
        }
    }
}

/**
 * Draws a genre's signature motif. Deterministic for a given [seed] so covers never shimmer
 * between recompositions.
 */
fun DrawScope.drawCoverMotif(motif: CoverMotif, color: Color, accent: Color, seed: Int) {
    val w = size.width
    val h = size.height
    val random = Random(seed * 7919 + motif.ordinal)
    val thin = max(1f, min(w, h) * 0.006f)
    when (motif) {
        CoverMotif.STARS -> repeat(26) {
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h * 0.75f
            val r = (0.4f + random.nextFloat()) * thin * 1.6f
            drawCircle(color.boost((0.5f + random.nextFloat())), r, Offset(x, y))
            if (it % 6 == 0) drawSparkle(Offset(x, y), r * 5f, accent.copy(alpha = 0.55f))
        }
        CoverMotif.PETALS -> {
            val cx = w * 0.85f
            val cy = h * 0.15f
            repeat(7) { i ->
                val angle = (i / 7f) * 2 * PI
                val d = min(w, h) * 0.18f
                drawCircle(
                    color.boost(1.1f),
                    radius = min(w, h) * 0.16f,
                    center = Offset(cx + (cos(angle) * d).toFloat(), cy + (sin(angle) * d).toFloat()),
                )
            }
            drawCircle(accent.copy(alpha = 0.35f), min(w, h) * 0.07f, Offset(cx, cy))
        }
        CoverMotif.RAIN -> {
            var x = -h * 0.3f
            while (x < w) {
                drawLine(color, Offset(x, 0f), Offset(x + h * 0.3f, h), strokeWidth = thin)
                x += w / 11f + random.nextFloat() * 4f
            }
        }
        CoverMotif.MOON -> {
            val r = min(w, h) * 0.28f
            val c = Offset(w * 0.72f, h * 0.24f)
            drawCircle(accent.copy(alpha = 0.22f), r * 1.6f, c)
            drawCircle(color.boost(2.2f), r, c)
            drawCircle(color.boost(0.8f), r * 2.4f, c, style = Stroke(thin))
            repeat(12) {
                drawCircle(color, thin * 1.4f, Offset(random.nextFloat() * w, random.nextFloat() * h * 0.6f))
            }
        }
        CoverMotif.GRID -> {
            val horizon = h * 0.55f
            var y = horizon
            var gap = h * 0.02f
            while (y < h) {
                drawLine(color, Offset(0f, y), Offset(w, y), strokeWidth = thin)
                y += gap
                gap *= 1.35f
            }
            for (i in -6..12) {
                val bx = w / 2 + (i - 3) * w * 0.22f
                drawLine(color, Offset(w / 2, horizon), Offset(bx, h), strokeWidth = thin)
            }
            drawCircle(accent.copy(alpha = 0.35f), min(w, h) * 0.16f, Offset(w / 2, horizon))
        }
        CoverMotif.LINES -> {
            var y = h * 0.1f
            while (y < h * 0.62f) {
                drawLine(color, Offset(w * 0.1f, y), Offset(w * 0.9f, y), strokeWidth = thin)
                y += h * 0.045f
            }
            drawLine(accent.copy(alpha = 0.6f), Offset(w * 0.1f, h * 0.66f), Offset(w * 0.4f, h * 0.66f), strokeWidth = thin * 2.2f)
        }
        CoverMotif.FRAME -> {
            val inset = min(w, h) * 0.06f
            drawRoundRect(color.boost(1.6f), Offset(inset, inset), Size(w - inset * 2, h - inset * 2), style = Stroke(thin))
            val inner = inset * 1.6f
            drawRoundRect(accent.copy(alpha = 0.45f), Offset(inner, inner), Size(w - inner * 2, h - inner * 2), style = Stroke(thin))
            listOf(Offset(inner, inner), Offset(w - inner, inner), Offset(inner, h - inner), Offset(w - inner, h - inner)).forEach {
                drawSparkle(it, inset * 0.9f, accent.copy(alpha = 0.7f))
            }
        }
        CoverMotif.DOTS -> {
            val step = min(w, h) * 0.11f
            var y = step / 2
            var row = 0
            while (y < h) {
                var x = if (row % 2 == 0) step / 2 else step
                while (x < w) {
                    val tint = if ((x.toInt() + y.toInt()) % 3 == 0) accent.copy(alpha = 0.4f) else color
                    drawCircle(tint, step * 0.16f, Offset(x, y))
                    x += step
                }
                y += step
                row++
            }
        }
        CoverMotif.WAVES -> {
            for (line in 0 until 9) {
                val path = Path()
                val baseY = h * (0.12f + line * 0.07f)
                path.moveTo(0f, baseY)
                var x = 0f
                while (x <= w) {
                    path.lineTo(x, baseY + sin((x / w) * 4 * PI + line * 0.6).toFloat() * h * 0.018f)
                    x += w / 40f
                }
                drawPath(path, if (line == 4) accent.copy(alpha = 0.5f) else color, style = Stroke(thin, cap = StrokeCap.Round))
            }
        }
        CoverMotif.RAYS -> {
            val origin = Offset(w * 0.5f, h * 1.05f)
            for (i in 0 until 19) {
                val angle = PI + (i / 18.0) * PI
                val end = Offset(origin.x + (cos(angle) * h * 1.3).toFloat(), origin.y + (sin(angle) * h * 1.3).toFloat())
                drawLine(color, origin, end, strokeWidth = thin)
            }
            drawCircle(accent.copy(alpha = 0.35f), min(w, h) * 0.22f, origin)
        }
        CoverMotif.HALFTONE -> {
            val step = min(w, h) * 0.075f
            var y = 0f
            while (y < h) {
                var x = 0f
                while (x < w) {
                    val t = ((x / w) + (1f - y / h)) / 2f
                    drawCircle(color.boost(1.4f), step * 0.45f * t, Offset(x, y))
                    x += step
                }
                y += step
            }
            drawRect(accent.copy(alpha = 0.32f), Offset(w * 0.62f, 0f), Size(w * 0.06f, h))
        }
        CoverMotif.ORBIT -> {
            val c = Offset(w * 0.78f, h * 0.2f)
            for (i in 1..6) drawCircle(color, min(w, h) * 0.1f * i, c, style = Stroke(thin))
            drawCircle(accent.copy(alpha = 0.55f), min(w, h) * 0.05f, c)
            drawCircle(color.boost(2f), thin * 3f, Offset(c.x - min(w, h) * 0.3f, c.y + min(w, h) * 0.12f))
        }
        CoverMotif.SLASH -> {
            rotate(-24f, Offset(w / 2, h / 2)) {
                drawRect(color.boost(1.3f), Offset(-w, h * 0.18f), Size(w * 3, h * 0.05f))
                drawRect(accent.copy(alpha = 0.45f), Offset(-w, h * 0.27f), Size(w * 3, h * 0.012f))
                drawRect(color, Offset(-w, h * 0.33f), Size(w * 3, h * 0.022f))
            }
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}

/** Draws a subtle genre-coloured accent bar on the leading edge of a card. */
fun Modifier.genreEdge(colors: GenreColors): Modifier = this.drawBehind {
    drawRoundRect(
        brush = Brush.verticalGradient(colors.coverGradient),
        topLeft = Offset.Zero,
        size = Size(4.dp.toPx(), size.height),
        cornerRadius = CornerRadius(2.dp.toPx()),
    )
}

private fun Color.boost(factor: Float): Color = copy(alpha = (alpha * factor).coerceIn(0f, 1f))
