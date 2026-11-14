package com.fable.app.presentation.books.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.designsystem.FableIconButton
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.theme.FableThemeTokens

/**
 * Pill search field. Focus lifts it with a soft shadow and warms the border; the trailing
 * action morphs between "search" and "clear".
 */
@Composable
fun BookSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Title, author or ISBN",
) {
    val tokens = FableThemeTokens.current
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val elevation by animateDpAsState(if (focused) 12.dp else 2.dp, label = "searchElevation")
    val border by animateColorAsState(if (focused) scheme.primary else tokens.hairline, label = "searchBorder")
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        interactionSource = interaction,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
        cursorBrush = SolidColor(scheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch()
            focusManager.clearFocus()
        }),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(elevation, CircleShape, clip = false, ambientColor = scheme.primary, spotColor = scheme.primary)
                    .clip(CircleShape)
                    .background(tokens.raisedSurface)
                    .border(1.dp, border, CircleShape)
                    .padding(start = 20.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    FableIcons.Search,
                    contentDescription = null,
                    tint = if (focused) scheme.primary else scheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp),
                )
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                        )
                    }
                    inner()
                }
                AnimatedContent(
                    targetState = query.isNotEmpty(),
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                    label = "searchTrailing",
                ) { hasQuery ->
                    if (hasQuery) {
                        FableIconButton(FableIcons.Close, "Clear search", onClear, filled = false, tint = scheme.onSurfaceVariant)
                    } else {
                        FableIconButton(
                            FableIcons.ChevronRight,
                            "Search",
                            onSearch,
                            container = scheme.primary,
                            tint = scheme.onPrimary,
                            modifier = Modifier.padding(2.dp),
                        )
                    }
                }
            }
        },
    )
}
