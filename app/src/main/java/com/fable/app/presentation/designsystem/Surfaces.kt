package com.fable.app.presentation.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.readableOn

/** A hairline-bordered surface. Clickable variants get a tactile press. */
@Composable
fun FableCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = FableThemeTokens.current.raisedSurface,
    border: Color = FableThemeTokens.current.cardBorder.copy(alpha = 0.55f),
    elevation: Dp = FableThemeTokens.current.cardElevation,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = modifier.pressScale(interaction, 0.98f),
            shape = shape,
            color = color,
            border = BorderStroke(1.dp, border),
            shadowElevation = elevation,
            interactionSource = interaction,
        ) { Column(content = content) }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            border = BorderStroke(1.dp, border),
            shadowElevation = elevation,
        ) { Column(content = content) }
    }
}

/** Small-caps editorial kicker that sits above titles. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Magazine-style section header: an eyebrow, a display title, an optional dek and an optional
 * trailing action. Headers are marked as headings for accessibility services.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            eyebrow?.let { Eyebrow(it, color = accent) }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (actionLabel != null && onAction != null) {
            FableTextButton(actionLabel, onAction, icon = FableIcons.ChevronRight, color = accent)
        }
    }
}

/** A display-size page title with an eyebrow and dek. */
@Composable
fun PageTitle(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        eyebrow?.let { Eyebrow(it) }
        Text(
            title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Hairline rule with the theme's ornament centred — a quiet section transition. */
@Composable
fun OrnamentDivider(
    modifier: Modifier = Modifier,
    ornament: String = FableThemeTokens.current.ornament,
    color: Color = FableThemeTokens.current.gilt,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(color.copy(alpha = 0.28f)))
        Text(
            ornament,
            color = color,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        Box(Modifier.weight(1f).height(1.dp).background(color.copy(alpha = 0.28f)))
    }
}

enum class BannerTone { Info, Success, Warning, Error }

/** Inline status line for messages, sync notices and errors. Animates in and out. */
@Composable
fun StatusBanner(
    message: String?,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Info,
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val tokens = FableThemeTokens.current
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val accent = when (tone) {
            BannerTone.Info -> scheme.primary
            BannerTone.Success -> scheme.primary
            BannerTone.Warning -> tokens.gilt
            BannerTone.Error -> scheme.error
        }
        val glyph = icon ?: when (tone) {
            BannerTone.Info -> FableIcons.Info
            BannerTone.Success -> FableIcons.Check
            BannerTone.Warning -> FableIcons.Clock
            BannerTone.Error -> FableIcons.Alert
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(lerp(tokens.raisedSurface, accent, if (tokens.dark) 0.18f else 0.09f))
                .border(1.dp, accent.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(glyph, null, tint = accent, modifier = Modifier.size(18.dp))
            Text(
                message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (onDismiss != null) {
                FableIconButton(FableIcons.Close, "Dismiss", onDismiss, filled = false, size = 32.dp, tint = scheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Selectable pill. When [accent] is given (e.g. a genre colour), the selected state adopts it,
 * which is how genre filters visibly carry their identity.
 */
@Composable
fun FableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = readableOn(accent),
    leading: String? = null,
    icon: ImageVector? = null,
    count: Int? = null,
) {
    val tokens = FableThemeTokens.current
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (selected) accent else tokens.raisedSurface.copy(alpha = 0.7f),
        animationSpec = FableMotion.snappySpring(),
        label = "chipContainer",
    )
    val content by animateColorAsState(if (selected) onAccent else scheme.onSurface, label = "chipContent")
    val borderColor by animateColorAsState(
        if (selected) accent else scheme.outline.copy(alpha = 0.35f),
        label = "chipBorder",
    )
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberFableHaptics()
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .pressScale(interaction, 0.94f)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, borderColor, CircleShape)
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab) {
                haptics.tick()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        leading?.let { Text(it, color = if (selected) content else accent, style = MaterialTheme.typography.labelLarge) }
        icon?.let { Icon(it, null, tint = content, modifier = Modifier.size(16.dp)) }
        Text(label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
        count?.let {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(if (selected) content.copy(alpha = 0.18f) else scheme.onSurface.copy(alpha = 0.07f))
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text(it.toString(), style = MaterialTheme.typography.labelMedium, color = content)
            }
        }
    }
}

/**
 * Segmented control with a sliding indicator. Used for the library shelves and other
 * small, mutually exclusive choices.
 */
@Composable
fun <T> SegmentedTabs(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    count: ((T) -> Int)? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val tokens = FableThemeTokens.current
    val scheme = MaterialTheme.colorScheme
    val haptics = rememberFableHaptics()
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(tokens.sunkenSurface)
            .border(1.dp, tokens.hairline, CircleShape)
            .padding(4.dp),
    ) {
        val segment = maxWidth / options.size.coerceAtLeast(1)
        val offset by animateDpAsState(segment * selectedIndex, animationSpec = FableMotion.gentleSpring(), label = "segmentOffset")
        Box(
            Modifier
                .offset(x = offset)
                .width(segment)
                .height(44.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Row(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val color by animateColorAsState(
                    if (isSelected) readableOn(accent) else scheme.onSurfaceVariant,
                    label = "segmentText",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Tab) {
                            if (!isSelected) haptics.tick()
                            onSelect(option)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            label(option),
                            style = MaterialTheme.typography.labelLarge,
                            color = color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        count?.let {
                            Text(it(option).toString(), style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.75f))
                        }
                    }
                }
            }
        }
    }
}

/** Large numeral statistic with a caption — editorial infographic style. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
) {
    FableCard(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            icon?.let {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(it, null, tint = accent, modifier = Modifier.size(17.dp)) }
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Pull-quote styling for review text and bios. */
@Composable
fun PullQuote(text: String, modifier: Modifier = Modifier, accent: Color = FableThemeTokens.current.gilt, maxLines: Int = Int.MAX_VALUE) {
    Row(modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.width(2.dp).fillMaxHeight().background(accent.copy(alpha = 0.6f)))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FableThemeTokens.current.bookTitleFont, fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A row in a settings-style list: icon tile, title, subtitle, chevron. */
@Composable
fun ListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    trailing: (@Composable () -> Unit)? = null,
    destructive: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val color = if (destructive) scheme.error else scheme.onSurface
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        icon?.let {
            Box(
                Modifier.size(40.dp).clip(MaterialTheme.shapes.medium)
                    .background((if (destructive) scheme.error else accent).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Icon(it, null, tint = if (destructive) scheme.error else accent, modifier = Modifier.size(20.dp)) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = color)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant) }
        }
        if (trailing != null) trailing() else if (!destructive) {
            Icon(FableIcons.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

/** Vertical breathing room in the editorial rhythm. */
@Composable
fun SectionSpacer(height: Dp = FableSpacing.section) = Spacer(Modifier.height(height))

/** Centered caption used under empty lists and for gentle hints. */
@Composable
fun Caption(text: String, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Start) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        textAlign = align,
    )
}
