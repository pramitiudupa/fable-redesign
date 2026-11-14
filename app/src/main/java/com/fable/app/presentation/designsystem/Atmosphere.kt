package com.fable.app.presentation.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.GenreColors
import kotlin.math.max

/**
 * The page "atmosphere": the theme's paper gradient plus two soft pools of light. It gives every
 * screen depth without a single extra composable, and it is drawn once per size via a cache.
 */
@Composable
fun Modifier.fableAtmosphere(): Modifier {
    val tokens = FableThemeTokens.current
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val glow = tokens.glow
    val glowSecondary = tokens.glowSecondary
    val dark = tokens.dark
    return this.drawWithCache {
        val base = Brush.verticalGradient(listOf(background, androidx.compose.ui.graphics.lerp(background, surface, 0.4f), background))
        val warm = Brush.radialGradient(
            colors = listOf(glow.copy(alpha = if (dark) 0.55f else 0.7f), Color.Transparent),
            center = Offset(size.width * 0.05f, size.height * 0.02f),
            radius = max(size.width, 1f) * 0.95f,
        )
        val cool = Brush.radialGradient(
            colors = listOf(glowSecondary.copy(alpha = if (dark) 0.5f else 0.65f), Color.Transparent),
            center = Offset(size.width * 1.05f, size.height * 0.42f),
            radius = max(size.width, 1f) * 0.9f,
        )
        onDrawBehind {
            drawRect(base)
            drawRect(warm)
            drawRect(cool)
        }
    }
}

/** Atmospheric wash tinted by a genre — used for genre headers and the book-detail hero. */
fun Modifier.genreAtmosphere(colors: GenreColors, intensity: Float = 1f): Modifier = this.drawBehind {
    drawGenreWash(colors, intensity)
}

fun DrawScope.drawGenreWash(colors: GenreColors, intensity: Float = 1f) {
    drawRect(Brush.verticalGradient(colors.gradient))
    drawRect(
        Brush.radialGradient(
            colors = listOf(colors.glow.copy(alpha = colors.glow.alpha * intensity), Color.Transparent),
            center = Offset(size.width * 0.82f, size.height * 0.12f),
            radius = max(size.width, 1f) * 0.75f,
        ),
    )
    drawRect(
        Brush.radialGradient(
            colors = listOf(colors.accent.copy(alpha = 0.16f * intensity), Color.Transparent),
            center = Offset(size.width * 0.08f, size.height * 0.55f),
            radius = max(size.width, 1f) * 0.6f,
        ),
    )
}

@Composable
fun AtmosphericBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fableAtmosphere(), content = content)
}
