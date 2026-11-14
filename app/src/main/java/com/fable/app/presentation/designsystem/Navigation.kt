package com.fable.app.presentation.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.readableOn

data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

/**
 * Floating bottom navigation: a frosted pill above the content. The selected destination grows
 * into a labelled capsule with a spring, others stay as quiet icons.
 */
@Composable
fun FableBottomBar(
    destinations: List<BottomDestination>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = FableThemeTokens.current
    val scheme = MaterialTheme.colorScheme
    val haptics = rememberFableHaptics()
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = tokens.raisedSurface.copy(alpha = 0.97f),
            shadowElevation = 14.dp,
            modifier = Modifier.widthIn(max = 520.dp).border(1.dp, tokens.hairline, CircleShape),
        ) {
            Row(
                Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                destinations.forEach { destination ->
                    val selected = destination.route == currentRoute
                    val container by animateColorAsState(
                        if (selected) scheme.primary else scheme.primary.copy(alpha = 0f),
                        animationSpec = FableMotion.gentleSpring(),
                        label = "navContainer",
                    )
                    val content by animateColorAsState(
                        if (selected) readableOn(scheme.primary) else scheme.onSurfaceVariant,
                        label = "navContent",
                    )
                    val iconScale by animateFloatAsState(if (selected) 1f else 0.94f, FableMotion.bouncySpring(), label = "navIcon")
                    val interaction = remember { MutableInteractionSource() }
                    Row(
                        Modifier
                            .height(48.dp)
                            .pressScale(interaction, 0.92f)
                            .clip(CircleShape)
                            .background(container)
                            .clickable(interactionSource = interaction, indication = null, role = Role.Tab) {
                                if (!selected) haptics.tick()
                                onNavigate(destination.route)
                            }
                            .semantics { this.selected = selected }
                            .padding(horizontal = if (selected) 16.dp else 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label,
                            tint = content,
                            modifier = Modifier.size(22.dp).graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        )
                        AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn() + expandHorizontally(FableMotion.gentleSpring()),
                            exit = fadeOut() + shrinkHorizontally(FableMotion.gentleSpring()),
                        ) {
                            Text(
                                destination.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = content,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Whether the current destination draws a floating back button (set by the nav host). */
val LocalHasBackButton = compositionLocalOf { false }

/** Top padding for a screen's first item: status bar plus room for the floating back button. */
@Composable
fun screenTopPadding(extra: Dp = 12.dp): Dp {
    val status = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return status + extra + if (LocalHasBackButton.current) 56.dp else 0.dp
}

/** Bottom padding for a screen's last item: navigation bar plus room for the floating tab bar. */
@Composable
fun screenBottomPadding(extra: Dp = 24.dp): Dp {
    val nav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return nav + extra + if (LocalHasBackButton.current) 0.dp else 84.dp
}

@Composable
fun FloatingBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    FableIconButton(
        icon = FableIcons.Back,
        contentDescription = "Back",
        onClick = onBack,
        modifier = modifier,
        size = 44.dp,
    )
}

/** Soft fade behind the status bar so scrolling content never collides with system icons. */
@Composable
fun StatusBarScrim(modifier: Modifier = Modifier) {
    val background = MaterialTheme.colorScheme.background
    val height = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 14.dp
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    0f to background.copy(alpha = 0.92f),
                    0.6f to background.copy(alpha = 0.7f),
                    1f to background.copy(alpha = 0f),
                ),
            ),
    )
}
