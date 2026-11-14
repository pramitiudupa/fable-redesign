package com.fable.app.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/** Spacing scale — a 4dp base grid with editorial generosity at the top end. */
object FableSpacing {
    val hairline = 1.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val section = 40.dp
    val hero = 56.dp

    /** Horizontal page gutter used by every screen. */
    val gutter = 20.dp

    /** Minimum interactive touch target. */
    val touchTarget = 48.dp
}

/** Elevation scale. Fable prefers hairlines and soft ambient shadow over heavy material depth. */
object FableElevation {
    val flat = 0.dp
    val resting = 2.dp
    val raised = 8.dp
    val floating = 18.dp
    val cover = 14.dp
}

/** Motion specifications shared across the app. */
object FableMotion {
    const val THEME_CROSSFADE_MS = 650
    const val SHORT_MS = 180
    const val MEDIUM_MS = 320
    const val LONG_MS = 520
    const val STAGGER_MS = 55

    /** Material "emphasized" easing — fast start, gentle settle. */
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> gentleSpring(): SpringSpec<T> = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
    fun <T> bouncySpring(): SpringSpec<T> = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)
    fun <T> snappySpring(): SpringSpec<T> = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium)

    val offsetSpring: SpringSpec<IntOffset> = spring(
        dampingRatio = 0.86f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset(1, 1),
    )
}

/** Book cover proportions — a standard trade paperback ratio. */
object FableProportions {
    const val COVER_ASPECT = 0.66f
    const val HERO_ASPECT = 0.66f
}
