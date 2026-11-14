package com.fable.app.presentation.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableThemeTokens

enum class FableButtonStyle { Primary, Secondary, Tonal, Ghost }

/**
 * Fable's primary action component. The shape follows the active theme, the press is tactile,
 * and the loading state swaps label for a spinner without changing the button's footprint.
 */
@Composable
fun FableButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FableButtonStyle = FableButtonStyle.Primary,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String = "Please wait…",
    accent: Color? = null,
    onAccent: Color? = null,
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
) {
    val scheme = MaterialTheme.colorScheme
    val tokens = FableThemeTokens.current
    val base = accent ?: scheme.primary
    val onBase = onAccent ?: if (accent == null) scheme.onPrimary else com.fable.app.presentation.theme.readableOn(base)
    val container = when (style) {
        FableButtonStyle.Primary -> base
        FableButtonStyle.Tonal -> lerp(tokens.raisedSurface, base, if (tokens.dark) 0.24f else 0.13f)
        FableButtonStyle.Secondary, FableButtonStyle.Ghost -> Color.Transparent
    }
    val content = when (style) {
        FableButtonStyle.Primary -> onBase
        FableButtonStyle.Tonal -> if (tokens.dark) lerp(base, scheme.onSurface, 0.25f) else lerp(base, scheme.onSurface, 0.2f)
        FableButtonStyle.Secondary -> scheme.onSurface
        FableButtonStyle.Ghost -> base
    }
    val animatedContainer by animateColorAsState(container, label = "buttonContainer")
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberFableHaptics()
    val active = enabled && !loading
    Surface(
        modifier = modifier
            .heightIn(min = 52.dp)
            .alpha(if (enabled) 1f else 0.48f)
            .pressScale(interaction),
        shape = shape,
        color = animatedContainer,
        contentColor = content,
        border = if (style == FableButtonStyle.Secondary) BorderStroke(1.dp, scheme.outline.copy(alpha = 0.55f)) else null,
        shadowElevation = if (style == FableButtonStyle.Primary && enabled) 3.dp else 0.dp,
        onClick = {
            haptics.tap()
            onClick()
        },
        enabled = active,
        interactionSource = interaction,
    ) {
        Box(Modifier.padding(contentPadding), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = loading,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith fadeOut() },
                label = "buttonLoading",
            ) { isLoading ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = content,
                            trackColor = content.copy(alpha = 0.2f),
                        )
                        Text(loadingText, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    } else {
                        icon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(19.dp)) }
                        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        trailingIcon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(19.dp)) }
                    }
                }
            }
        }
    }
}

/** A quiet inline text action, e.g. "See all" or "Forgot password?". */
@Composable
fun FableTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true, color = color),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .alpha(if (enabled) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
        icon?.let { Icon(it, null, tint = color, modifier = Modifier.size(16.dp)) }
    }
}

/** Circular icon button with an optional frosted container — used for floating chrome. */
@Composable
fun FableIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    filled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    container: Color = FableThemeTokens.current.raisedSurface.copy(alpha = 0.92f),
    badge: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val tokens = FableThemeTokens.current
    Box(
        modifier = modifier
            .size(size.coerceAtLeast(44.dp))
            .pressScale(interaction, 0.9f)
            .clip(CircleShape)
            .then(if (filled) Modifier.background(container).border(1.dp, tokens.hairline, CircleShape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.48f))
        if (badge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = size * 0.2f, end = size * 0.2f)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
            )
        }
    }
}

/** Animated swap between two icon states, e.g. a bookmark becoming filled. */
@Composable
fun AnimatedIconSwap(
    checked: Boolean,
    checkedIcon: ImageVector,
    uncheckedIcon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = checked,
        transitionSpec = {
            (scaleIn(animationSpec = FableMotion.bouncySpring(), initialScale = 0.4f) + fadeIn()) togetherWith fadeOut()
        },
        label = "iconSwap",
    ) { isChecked ->
        Icon(if (isChecked) checkedIcon else uncheckedIcon, null, tint = tint, modifier = modifier)
    }
}
