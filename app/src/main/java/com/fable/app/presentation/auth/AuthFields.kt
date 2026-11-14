package com.fable.app.presentation.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FablePasswordField
import com.fable.app.presentation.designsystem.LocalHasBackButton
import com.fable.app.presentation.designsystem.FableTextField
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.fableAtmosphere
import com.fable.app.presentation.designsystem.pastelBackdrop
import androidx.compose.ui.graphics.Color
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.Genre
import com.fable.app.presentation.theme.genreColors
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Shared shell for every account screen: an atmospheric hero with the Fable wordmark and a
 * shelf of genre-coloured spines, then a raised sheet that holds the form.
 */
@Composable
internal fun AuthPage(
    title: String,
    subtitle: String,
    firebaseConfigured: Boolean,
    state: AuthUiState,
    heroLine: String = "Every story\nfinds its reader.",
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = FableThemeTokens.current
    Column(
        Modifier
            .fillMaxSize()
            .then(if (tokens.dark) Modifier.fableAtmosphere() else Modifier.pastelBackdrop())
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 28.dp)
                .padding(top = if (LocalHasBackButton.current) 72.dp else 28.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            BrandMark(Modifier.entrance(0))
            Text(
                heroLine,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = tokens.bookTitleFont,
                    fontStyle = FontStyle.Italic,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.entrance(1).semantics { heading() },
            )
            SpineShelf(Modifier.fillMaxWidth().height(92.dp).entrance(2))
        }
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .entrance(3),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = if (tokens.dark) tokens.raisedSurface else Color(0xFFFDFCFF),
            shadowElevation = 24.dp,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 26.dp)
                    .padding(top = 30.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!firebaseConfigured) {
                    StatusBanner(
                        "Firebase setup required — add 'google-services.json' to the 'app/' directory to enable account actions.",
                        tone = BannerTone.Warning,
                        icon = FableIcons.Alert,
                    )
                }
                StatusBanner(state.error, tone = BannerTone.Error)
                StatusBanner(state.message, tone = BannerTone.Success)
                content()
            }
        }
    }
}

/** The Fable wordmark: an ornament medallion and a serif logotype. */
@Composable
internal fun BrandMark(modifier: Modifier = Modifier) {
    val tokens = FableThemeTokens.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, tokens.gilt)))
                .border(1.dp, tokens.gilt.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("F", style = MaterialTheme.typography.titleLarge.copy(fontFamily = tokens.bookTitleFont), color = MaterialTheme.colorScheme.onPrimary)
        }
        Column {
            Text(
                "Fable",
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "A READING LIFE, BEAUTIFULLY KEPT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val shelfGenres = listOf(
    Genre.FANTASY, Genre.ROMANCE, Genre.MYSTERY, Genre.CLASSICS, Genre.SCIENCE_FICTION, Genre.POETRY,
    Genre.HORROR, Genre.ADVENTURE, Genre.HISTORICAL_FICTION, Genre.PARANORMAL, Genre.LITERARY_FICTION,
    Genre.YOUNG_ADULT, Genre.PHILOSOPHY, Genre.DARK_ROMANCE,
)
private val shelfHeights = listOf(0.82f, 1f, 0.74f, 0.92f, 0.86f, 0.68f, 0.96f, 0.78f, 0.9f, 0.72f, 0.88f, 0.8f, 0.94f, 0.7f)
private val shelfWidths = listOf(1f, 0.8f, 1.15f, 0.9f, 1.05f, 0.75f, 1f, 1.2f, 0.85f, 0.95f, 1.1f, 0.8f, 1f, 0.9f)

/**
 * Decorative shelf of spines in the genre palettes — an abstract preview of Fable's genre
 * identities. The spines rise into place one after another.
 */
@Composable
internal fun SpineShelf(modifier: Modifier = Modifier) {
    val gradients = shelfGenres.map { genreColors(it).coverGradient }
    val accents = shelfGenres.map { genreColors(it).accent }
    val tokens = FableThemeTokens.current
    val rise = remember { Animatable(0f) }
    LaunchedEffect(Unit) { rise.animateTo(1f, tween(1100, delayMillis = 200, easing = FableMotion.EmphasizedDecelerate)) }
    Box(
        modifier
            .graphicsLayer { alpha = 0.35f + 0.65f * rise.value }
            .drawBehind {
                val totalWeight = shelfWidths.sum()
                val gap = 3.dp.toPx()
                val unit = (size.width - gap * (shelfWidths.size - 1)) / totalWeight
                var x = 0f
                shelfWidths.forEachIndexed { i, weight ->
                    val w = unit * weight
                    val local = ((rise.value * shelfWidths.size) - i * 0.6f).coerceIn(0f, 1f)
                    val h = size.height * shelfHeights[i] * local
                    val top = size.height - h
                    drawRoundRect(
                        Brush.verticalGradient(gradients[i], startY = top, endY = size.height),
                        topLeft = Offset(x, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                    if (h > 0f) {
                        drawRect(accents[i].copy(alpha = 0.75f), Offset(x + w * 0.18f, top + h * 0.16f), Size(w * 0.64f, 1.5.dp.toPx()))
                        drawRect(accents[i].copy(alpha = 0.45f), Offset(x + w * 0.18f, top + h * 0.78f), Size(w * 0.64f, 1.dp.toPx()))
                    }
                    x += w + gap
                }
                drawRect(tokens.gilt.copy(alpha = 0.5f), Offset(0f, size.height - 1.dp.toPx()), Size(size.width, 1.dp.toPx()))
            },
    )
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    icon: ImageVector? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    FableTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        leadingIcon = icon,
        placeholder = placeholder,
        keyboardType = keyboardType,
        imeAction = imeAction,
        isError = isError,
    )
}

@Composable
internal fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    FablePasswordField(value = value, onValueChange = onValueChange, label = label, isError = isError, imeAction = imeAction)
}

@Composable
internal fun AuthSubmitButton(label: String, isLoading: Boolean, onClick: () -> Unit) {
    FableButton(
        text = label,
        onClick = onClick,
        loading = isLoading,
        trailingIcon = FableIcons.ArrowRight,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    )
}

/** True when the current error message is about a field described by any of [keywords]. */
internal fun AuthUiState.errorMentions(vararg keywords: String): Boolean {
    val text = error?.lowercase() ?: return false
    return keywords.any { text.contains(it) }
}

@Composable
internal fun AuthDivider(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(FableThemeTokens.current.hairline))
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f).height(1.dp).background(FableThemeTokens.current.hairline))
    }
}

@Composable
internal fun AuthSpacer() = Spacer(Modifier.height(2.dp))
