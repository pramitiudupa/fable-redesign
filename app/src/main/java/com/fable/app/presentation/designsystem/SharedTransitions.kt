package com.fable.app.presentation.designsystem

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect

/**
 * Shared-element plumbing. The authenticated navigation host provides both scopes; components
 * that want to morph between screens (book covers) read them and silently do nothing when they
 * are absent, e.g. inside the theme previews.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
private val CoverBoundsTransform = BoundsTransform { _: Rect, _: Rect ->
    spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
}

/** Key for the cover of a given book, shared between list items and the detail hero. */
fun bookCoverSharedKey(bookId: String): String = "book-cover:$bookId"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBookCover(key: String?): Modifier {
    if (key == null) return this
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@sharedBookCover.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = CoverBoundsTransform,
        )
    }
}
