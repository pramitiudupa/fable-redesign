package com.fable.app.presentation.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.theme.FableMotion

/** Scales an element down slightly while pressed — the tactile "give" of a premium control. */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.965f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = FableMotion.bouncySpring(),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * A one-time entrance: fade and rise, staggered by [index]. The "has played" flag survives
 * recomposition and configuration changes, so scrolling back never replays the animation.
 */
@Composable
fun Modifier.entrance(index: Int = 0, key: Any = Unit): Modifier {
    var played by rememberSaveable(key) { mutableStateOf(false) }
    val progress = remember(key) { Animatable(if (played) 1f else 0f) }
    val rise = with(LocalDensity.current) { 18.dp.toPx() }
    LaunchedEffect(key) {
        if (!played) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = FableMotion.LONG_MS,
                    delayMillis = (index.coerceAtMost(8)) * FableMotion.STAGGER_MS,
                    easing = FableMotion.EmphasizedDecelerate,
                ),
            )
            played = true
        }
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * rise
    }
}

/** Thin semantic wrapper over [HapticFeedback] so call sites read as intent, not mechanism. */
@Stable
class FableHaptics(private val feedback: HapticFeedback) {
    fun tap() = feedback.performHapticFeedback(HapticFeedbackType.ContextClick)
    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
    fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)
    fun toggleOn() = feedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
    fun toggleOff() = feedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
    fun reject() = feedback.performHapticFeedback(HapticFeedbackType.Reject)
    fun longPress() = feedback.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberFableHaptics(): FableHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { FableHaptics(feedback) }
}
