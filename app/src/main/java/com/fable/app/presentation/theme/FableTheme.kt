package com.fable.app.presentation.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.fable.app.domain.model.FableThemeName

/**
 * Global Fable theme.
 *
 * Fable has two cooperating design systems:
 *  A. this global theme — atmosphere, surfaces, typography and shape for the whole app;
 *  B. [GenreThemes] — genre-specific accents layered on top of book presentation.
 *
 * Screens read colours from [MaterialTheme] and the extended [FableThemeTokens], so theme
 * switching is animated and consistent everywhere.
 */
@Immutable
data class FablePalette(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val error: Color,
    val dark: Boolean,
    /** Warm atmospheric light used for background glows. */
    val glow: Color = Color.Transparent,
    /** Secondary, cooler atmospheric light. */
    val glowSecondary: Color = Color.Transparent,
    /** Metallic accent used for ornaments, rules and highlights. */
    val gilt: Color = tertiary,
)

@Immutable
data class FableThemeTokens(
    val pageBrush: Brush,
    val cardBorder: Color,
    val ornament: String,
    val cardElevation: Dp,
    val dark: Boolean = false,
    val glow: Color = Color.Transparent,
    val glowSecondary: Color = Color.Transparent,
    val gilt: Color = Color(0xFFB8913F),
    val hairline: Color = Color(0x1F000000),
    val raisedSurface: Color = Color.White,
    val sunkenSurface: Color = Color(0xFFF0EDE6),
    val displayFont: FontFamily = FontFamily.Serif,
    val bookTitleFont: FontFamily = FontFamily.Serif,
    val displayItalic: Boolean = false,
    val cornerRadius: Dp = 12.dp,
    val themeName: FableThemeName = FableThemeName.DEFAULT,
) {
    companion object {
        val current: FableThemeTokens
            @Composable get() = LocalFableThemeTokens.current
    }
}

data class FableThemeDefinition(
    val name: FableThemeName,
    val palette: FablePalette,
    val colorScheme: ColorScheme,
    val typography: Typography,
    val shapes: Shapes,
    val tokens: FableThemeTokens,
)

private val LocalFableThemeTokens = staticCompositionLocalOf {
    fableThemeDefinition(FableThemeName.DEFAULT).tokens
}

@Composable
fun FableTheme(theme: FableThemeName = FableThemeName.DEFAULT, content: @Composable () -> Unit) {
    val definition = remember(theme) { fableThemeDefinition(theme) }
    val palette = definition.palette.animated()
    val tokens = remember(definition, palette) { definition.tokens.withPalette(palette) }
    val scheme = remember(palette) { colorSchemeFor(palette) }
    SystemBarsEffect(darkTheme = palette.dark)
    CompositionLocalProvider(LocalFableThemeTokens provides tokens) {
        MaterialTheme(
            colorScheme = scheme,
            typography = definition.typography,
            shapes = definition.shapes,
            content = content,
        )
    }
}

/** Renders [content] in a theme without animation — used by the live theme previews. */
@Composable
fun FableThemePreview(theme: FableThemeName, content: @Composable () -> Unit) {
    val definition = remember(theme) { fableThemeDefinition(theme) }
    CompositionLocalProvider(LocalFableThemeTokens provides definition.tokens) {
        MaterialTheme(
            colorScheme = definition.colorScheme,
            typography = definition.typography,
            shapes = definition.shapes,
            content = content,
        )
    }
}

@Composable
private fun SystemBarsEffect(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

@Composable
private fun FablePalette.animated(): FablePalette {
    val spec = tween<Color>(durationMillis = FableMotion.THEME_CROSSFADE_MS)
    val primary by animateColorAsState(primary, spec, label = "primary")
    val onPrimary by animateColorAsState(onPrimary, spec, label = "onPrimary")
    val secondary by animateColorAsState(secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(onSecondary, spec, label = "onSecondary")
    val tertiary by animateColorAsState(tertiary, spec, label = "tertiary")
    val onTertiary by animateColorAsState(onTertiary, spec, label = "onTertiary")
    val background by animateColorAsState(background, spec, label = "background")
    val onBackground by animateColorAsState(onBackground, spec, label = "onBackground")
    val surface by animateColorAsState(surface, spec, label = "surface")
    val onSurface by animateColorAsState(onSurface, spec, label = "onSurface")
    val surfaceVariant by animateColorAsState(surfaceVariant, spec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(onSurfaceVariant, spec, label = "onSurfaceVariant")
    val outline by animateColorAsState(outline, spec, label = "outline")
    val glow by animateColorAsState(glow, spec, label = "glow")
    val glowSecondary by animateColorAsState(glowSecondary, spec, label = "glowSecondary")
    val gilt by animateColorAsState(gilt, spec, label = "gilt")
    return FablePalette(
        primary = primary, onPrimary = onPrimary,
        secondary = secondary, onSecondary = onSecondary,
        tertiary = tertiary, onTertiary = onTertiary,
        background = background, onBackground = onBackground,
        surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
        outline = outline, error = error, dark = dark,
        glow = glow, glowSecondary = glowSecondary, gilt = gilt,
    )
}

fun fableThemeDefinition(name: FableThemeName): FableThemeDefinition = when (name) {
    FableThemeName.GOTHIC -> definition(
        name,
        FablePalette(
            primary = Color(0xFFE9DDC3), onPrimary = Color(0xFF1A1714),
            secondary = Color(0xFFA99F8C), onSecondary = Color(0xFF1C1915),
            tertiary = Color(0xFFCBA65D), onTertiary = Color(0xFF221A0B),
            background = Color(0xFF0E0D0C), onBackground = Color(0xFFEDE6D8),
            surface = Color(0xFF181614), onSurface = Color(0xFFEDE6D8),
            surfaceVariant = Color(0xFF26221E), onSurfaceVariant = Color(0xFFB9AF9F),
            outline = Color(0xFF615A4E), error = Color(0xFFFFB4AB), dark = true,
            glow = Color(0xFF3B2E1C), glowSecondary = Color(0xFF1C2130), gilt = Color(0xFFCBA65D),
        ),
        type = ThemeType(
            display = FontFamily.Serif, displayWeight = FontWeight.Normal,
            displayTracking = (-0.01).em, labelTracking = 0.18.em,
        ),
        corner = 6.dp,
        ornament = "✠",
    )
    FableThemeName.GOTHIC_ROMANCE -> definition(
        name,
        FablePalette(
            primary = Color(0xFFE7A9BA), onPrimary = Color(0xFF2E0F1A),
            secondary = Color(0xFF93405B), onSecondary = Color(0xFFFFEAF0),
            tertiary = Color(0xFFEAD4B2), onTertiary = Color(0xFF2B1B14),
            background = Color(0xFF130A0F), onBackground = Color(0xFFF5E7EB),
            surface = Color(0xFF1E1117), onSurface = Color(0xFFF5E7EB),
            surfaceVariant = Color(0xFF2E1B24), onSurfaceVariant = Color(0xFFD5BCC4),
            outline = Color(0xFF735060), error = Color(0xFFFFB4AB), dark = true,
            glow = Color(0xFF52162C), glowSecondary = Color(0xFF2B1734), gilt = Color(0xFFE2C38E),
        ),
        type = ThemeType(
            display = FontFamily.Serif, displayWeight = FontWeight.Normal, displayItalic = true,
            displayTracking = 0.em, labelTracking = 0.14.em,
        ),
        corner = 18.dp,
        ornament = "❦",
    )
    FableThemeName.DARK_ACADEMIA -> definition(
        name,
        FablePalette(
            primary = Color(0xFF34503B), onPrimary = Color(0xFFF7F0DF),
            secondary = Color(0xFF6B4D2D), onSecondary = Color(0xFFFFF4E2),
            tertiary = Color(0xFF8C6D30), onTertiary = Color(0xFFFFF6E0),
            background = Color(0xFFEDE3CD), onBackground = Color(0xFF2B231A),
            surface = Color(0xFFF7F0DF), onSurface = Color(0xFF2B231A),
            surfaceVariant = Color(0xFFE2D4B8), onSurfaceVariant = Color(0xFF52463A),
            outline = Color(0xFF8C7C62), error = Color(0xFF9C3932), dark = false,
            glow = Color(0xFFDCC495), glowSecondary = Color(0xFFC9D4BC), gilt = Color(0xFF9A7A35),
        ),
        type = ThemeType(
            display = FontFamily.Serif, displayWeight = FontWeight.Medium,
            displayTracking = (-0.005).em, labelTracking = 0.16.em,
        ),
        corner = 8.dp,
        ornament = "❧",
    )
    FableThemeName.DREAMSCAPE -> definition(
        name,
        FablePalette(
            primary = Color(0xFF474E86), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF6E77AD), onSecondary = Color(0xFFFFFFFF),
            tertiary = Color(0xFF8E62A0), onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFEDEAF7), onBackground = Color(0xFF242845),
            surface = Color(0xFFF8F6FE), onSurface = Color(0xFF242845),
            surfaceVariant = Color(0xFFE0DDF0), onSurfaceVariant = Color(0xFF4C4F6E),
            outline = Color(0xFF8A8DAE), error = Color(0xFF9C3E4E), dark = false,
            glow = Color(0xFFDCD0F7), glowSecondary = Color(0xFFCDE2F6), gilt = Color(0xFF8C74BD),
        ),
        type = ThemeType(
            display = FontFamily.SansSerif, displayWeight = FontWeight.Light,
            displayTracking = (-0.02).em, labelTracking = 0.12.em,
        ),
        corner = 24.dp,
        ornament = "☾",
    )
    FableThemeName.MINIMALIST -> definition(
        name,
        FablePalette(
            primary = Color(0xFF2E4A3A), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF5B625B), onSecondary = Color(0xFFFFFFFF),
            tertiary = Color(0xFF8E6A3E), onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFF6F4EE), onBackground = Color(0xFF1D201E),
            surface = Color(0xFFFDFCF8), onSurface = Color(0xFF1D201E),
            surfaceVariant = Color(0xFFECE9E0), onSurfaceVariant = Color(0xFF575C56),
            outline = Color(0xFF8E8C82), error = Color(0xFFB3261E), dark = false,
            glow = Color(0xFFEDE6D6), glowSecondary = Color(0xFFE2EAE3), gilt = Color(0xFF8E6A3E),
        ),
        type = ThemeType(
            display = FontFamily.SansSerif, displayWeight = FontWeight.Medium,
            displayTracking = (-0.025).em, labelTracking = 0.14.em,
        ),
        corner = 12.dp,
        ornament = "—",
    )
}

private data class ThemeType(
    val display: FontFamily,
    val displayWeight: FontWeight,
    val displayItalic: Boolean = false,
    val displayTracking: TextUnit,
    val labelTracking: TextUnit,
)

private fun colorSchemeFor(palette: FablePalette): ColorScheme {
    val container = lerp(palette.surface, palette.primary, if (palette.dark) 0.22f else 0.12f)
    val secondaryContainer = lerp(palette.surface, palette.secondary, if (palette.dark) 0.26f else 0.14f)
    val tertiaryContainer = lerp(palette.surface, palette.tertiary, if (palette.dark) 0.24f else 0.14f)
    val errorContainer = lerp(palette.surface, palette.error, if (palette.dark) 0.22f else 0.12f)
    val low = lerp(palette.background, palette.surface, 0.5f)
    val high = lerp(palette.surface, palette.surfaceVariant, 0.5f)
    return if (palette.dark) {
        darkColorScheme(
            primary = palette.primary, onPrimary = palette.onPrimary,
            primaryContainer = container, onPrimaryContainer = palette.onSurface,
            secondary = palette.secondary, onSecondary = palette.onSecondary,
            secondaryContainer = secondaryContainer, onSecondaryContainer = palette.onSurface,
            tertiary = palette.tertiary, onTertiary = palette.onTertiary,
            tertiaryContainer = tertiaryContainer, onTertiaryContainer = palette.onSurface,
            background = palette.background, onBackground = palette.onBackground,
            surface = palette.surface, onSurface = palette.onSurface,
            surfaceVariant = palette.surfaceVariant, onSurfaceVariant = palette.onSurfaceVariant,
            surfaceContainerLowest = palette.background, surfaceContainerLow = low,
            surfaceContainer = palette.surface, surfaceContainerHigh = high,
            surfaceContainerHighest = palette.surfaceVariant,
            outline = palette.outline, outlineVariant = palette.outline.copy(alpha = 0.4f),
            error = palette.error, errorContainer = errorContainer, onErrorContainer = palette.onSurface,
        )
    } else {
        lightColorScheme(
            primary = palette.primary, onPrimary = palette.onPrimary,
            primaryContainer = container, onPrimaryContainer = palette.onSurface,
            secondary = palette.secondary, onSecondary = palette.onSecondary,
            secondaryContainer = secondaryContainer, onSecondaryContainer = palette.onSurface,
            tertiary = palette.tertiary, onTertiary = palette.onTertiary,
            tertiaryContainer = tertiaryContainer, onTertiaryContainer = palette.onSurface,
            background = palette.background, onBackground = palette.onBackground,
            surface = palette.surface, onSurface = palette.onSurface,
            surfaceVariant = palette.surfaceVariant, onSurfaceVariant = palette.onSurfaceVariant,
            surfaceContainerLowest = palette.surface, surfaceContainerLow = low,
            surfaceContainer = palette.surface, surfaceContainerHigh = high,
            surfaceContainerHighest = palette.surfaceVariant,
            outline = palette.outline, outlineVariant = palette.outline.copy(alpha = 0.35f),
            error = palette.error, errorContainer = errorContainer, onErrorContainer = palette.onSurface,
        )
    }
}

private fun FableThemeTokens.withPalette(palette: FablePalette): FableThemeTokens = copy(
    pageBrush = Brush.verticalGradient(listOf(palette.background, lerp(palette.background, palette.surface, 0.45f))),
    cardBorder = palette.outline.copy(alpha = if (palette.dark) 0.42f else 0.30f),
    dark = palette.dark,
    glow = palette.glow,
    glowSecondary = palette.glowSecondary,
    gilt = palette.gilt,
    hairline = palette.onSurface.copy(alpha = if (palette.dark) 0.10f else 0.08f),
    raisedSurface = if (palette.dark) lerp(palette.surface, palette.surfaceVariant, 0.55f) else palette.surface,
    sunkenSurface = if (palette.dark) lerp(palette.background, palette.surface, 0.5f)
    else lerp(palette.background, palette.surfaceVariant, 0.45f),
)

private fun definition(
    name: FableThemeName,
    palette: FablePalette,
    type: ThemeType,
    corner: Dp,
    ornament: String,
): FableThemeDefinition {
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(corner * 0.4f),
        small = RoundedCornerShape(corner * 0.65f),
        medium = RoundedCornerShape(corner),
        large = RoundedCornerShape(corner * 1.35f),
        extraLarge = RoundedCornerShape(corner * 1.8f),
    )
    val tokens = FableThemeTokens(
        pageBrush = Brush.verticalGradient(listOf(palette.background, palette.surface)),
        cardBorder = palette.outline,
        ornament = ornament,
        cardElevation = if (name == FableThemeName.MINIMALIST) 0.dp else 2.dp,
        displayFont = type.display,
        bookTitleFont = FontFamily.Serif,
        displayItalic = type.displayItalic,
        cornerRadius = corner,
        themeName = name,
    ).withPalette(palette)
    return FableThemeDefinition(
        name = name,
        palette = palette,
        colorScheme = colorSchemeFor(palette),
        typography = fableTypography(type),
        shapes = shapes,
        tokens = tokens,
    )
}

private fun fableTypography(type: ThemeType): Typography {
    val sans = FontFamily.SansSerif
    val displayStyle = if (type.displayItalic) FontStyle.Italic else FontStyle.Normal
    fun display(size: Int, line: Int) = TextStyle(
        fontFamily = type.display,
        fontWeight = type.displayWeight,
        fontStyle = displayStyle,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = type.displayTracking,
    )
    fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: TextUnit = 0.01.em) = TextStyle(
        fontFamily = sans,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = tracking,
    )
    return Typography(
        displayLarge = display(54, 58),
        displayMedium = display(42, 48),
        displaySmall = display(34, 40),
        headlineLarge = display(30, 36),
        headlineMedium = display(26, 32),
        headlineSmall = display(22, 28),
        titleLarge = TextStyle(
            fontFamily = type.display,
            fontWeight = if (type.display == FontFamily.Serif) FontWeight.Medium else FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
        ),
        titleMedium = body(16, 22, FontWeight.SemiBold, 0.005.em),
        titleSmall = body(14, 20, FontWeight.SemiBold, 0.005.em),
        bodyLarge = body(16, 25),
        bodyMedium = body(14, 21),
        bodySmall = body(12, 17, tracking = 0.015.em),
        labelLarge = body(14, 18, FontWeight.SemiBold, 0.02.em),
        labelMedium = body(12, 16, FontWeight.Medium, 0.04.em),
        labelSmall = body(10, 14, FontWeight.SemiBold, type.labelTracking),
    )
}
