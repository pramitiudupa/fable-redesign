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
    val brushEnd: Color = Color.Unspecified,
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

/*
 * The five global themes use exactly the palettes, heading fonts, letter spacing, corner radii,
 * card borders and page gradients from the original Fable repository. Only extra design-system
 * tokens (hairlines, raised surfaces, gilt accents) are derived from those palettes.
 */
fun fableThemeDefinition(name: FableThemeName): FableThemeDefinition = when (name) {
    FableThemeName.GOTHIC -> definition(
        name,
        FablePalette(
            primary = Color(0xFFE4D8BD), onPrimary = Color(0xFF201D19),
            secondary = Color(0xFFB9AD98), onSecondary = Color(0xFF24211C),
            tertiary = Color(0xFFC2A56B), onTertiary = Color(0xFF241E12),
            background = Color(0xFF11100F), onBackground = Color(0xFFECE6DA),
            surface = Color(0xFF1D1B18), onSurface = Color(0xFFECE6DA),
            surfaceVariant = Color(0xFF302D28), onSurfaceVariant = Color(0xFFCCC4B7),
            outline = Color(0xFF71695B), error = Color(0xFFFFB4AB), dark = true,
        ),
        headingFont = FontFamily.Serif,
        headingSpacing = 0.35.sp,
        corner = 4.dp,
        ornament = "♜",
        brushEnd = Color(0xFF191714),
    )
    FableThemeName.GOTHIC_ROMANCE -> definition(
        name,
        FablePalette(
            primary = Color(0xFFDDA1B1), onPrimary = Color(0xFF31151F),
            secondary = Color(0xFF6F3647), onSecondary = Color(0xFFFFE9EF),
            tertiary = Color(0xFFE4D4C9), onTertiary = Color(0xFF2B1B1F),
            background = Color(0xFF170F13), onBackground = Color(0xFFF2E5E8),
            surface = Color(0xFF24171D), onSurface = Color(0xFFF2E5E8),
            surfaceVariant = Color(0xFF38252E), onSurfaceVariant = Color(0xFFD9C1C8),
            outline = Color(0xFF80616C), error = Color(0xFFFFB4AB), dark = true,
        ),
        headingFont = FontFamily.Serif,
        headingSpacing = 0.25.sp,
        corner = 14.dp,
        ornament = "❦",
        brushEnd = Color(0xFF21151A),
    )
    FableThemeName.DARK_ACADEMIA -> definition(
        name,
        FablePalette(
            primary = Color(0xFF3C5843), onPrimary = Color(0xFFF7F0DF),
            secondary = Color(0xFF775C37), onSecondary = Color(0xFFFFF3DE),
            tertiary = Color(0xFF846D3F), onTertiary = Color(0xFFFFF4D9),
            background = Color(0xFFE9DFC8), onBackground = Color(0xFF30281F),
            surface = Color(0xFFF5EDD9), onSurface = Color(0xFF30281F),
            surfaceVariant = Color(0xFFE2D5BD), onSurfaceVariant = Color(0xFF514637),
            outline = Color(0xFF81745E), error = Color(0xFF9C3932), dark = false,
        ),
        headingFont = FontFamily.Serif,
        headingSpacing = 0.15.sp,
        corner = 8.dp,
        ornament = "❧",
        brushEnd = Color(0xFFF0E7D4),
    )
    FableThemeName.DREAMSCAPE -> definition(
        name,
        FablePalette(
            primary = Color(0xFF4A527C), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF707AA5), onSecondary = Color(0xFFFFFFFF),
            tertiary = Color(0xFF79638E), onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFECEAF5), onBackground = Color(0xFF292D48),
            surface = Color(0xFFF7F6FC), onSurface = Color(0xFF292D48),
            surfaceVariant = Color(0xFFDFDEED), onSurfaceVariant = Color(0xFF4A4D67),
            outline = Color(0xFF777B99), error = Color(0xFF9C3E4E), dark = false,
        ),
        headingFont = FontFamily.SansSerif,
        headingSpacing = 0.1.sp,
        corner = 20.dp,
        ornament = "☾",
        brushEnd = Color(0xFFF3F1FA),
    )
    FableThemeName.MINIMALIST -> definition(
        name,
        FablePalette(
            primary = Color(0xFF405A48), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF626B61), onSecondary = Color(0xFFFFFFFF),
            tertiary = Color(0xFF7A6848), onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFF4F1E8), onBackground = Color(0xFF292D29),
            surface = Color(0xFFFBF9F2), onSurface = Color(0xFF292D29),
            surfaceVariant = Color(0xFFE7E3D8), onSurfaceVariant = Color(0xFF4F544D),
            outline = Color(0xFF77796F), error = Color(0xFFB3261E), dark = false,
        ),
        headingFont = FontFamily.SansSerif,
        headingSpacing = 0.sp,
        corner = 10.dp,
        ornament = "—",
        brushEnd = Color(0xFFF8F6F0),
    )
}

private fun colorSchemeFor(palette: FablePalette): ColorScheme = if (palette.dark) {
    darkColorScheme(
        primary = palette.primary, onPrimary = palette.onPrimary,
        secondary = palette.secondary, onSecondary = palette.onSecondary,
        tertiary = palette.tertiary, onTertiary = palette.onTertiary,
        background = palette.background, onBackground = palette.onBackground,
        surface = palette.surface, onSurface = palette.onSurface,
        surfaceVariant = palette.surfaceVariant, onSurfaceVariant = palette.onSurfaceVariant,
        outline = palette.outline, error = palette.error,
    )
} else {
    lightColorScheme(
        primary = palette.primary, onPrimary = palette.onPrimary,
        secondary = palette.secondary, onSecondary = palette.onSecondary,
        tertiary = palette.tertiary, onTertiary = palette.onTertiary,
        background = palette.background, onBackground = palette.onBackground,
        surface = palette.surface, onSurface = palette.onSurface,
        surfaceVariant = palette.surfaceVariant, onSurfaceVariant = palette.onSurfaceVariant,
        outline = palette.outline, error = palette.error,
    )
}

/** Derived tokens. The page gradient and card border are exactly the original theme values. */
private fun FableThemeTokens.withPalette(palette: FablePalette): FableThemeTokens = copy(
    pageBrush = Brush.verticalGradient(listOf(palette.background, if (brushEnd == Color.Unspecified) palette.background else brushEnd)),
    cardBorder = palette.outline.copy(alpha = 0.48f),
    dark = palette.dark,
    glow = Color.Transparent,
    glowSecondary = Color.Transparent,
    gilt = palette.tertiary,
    hairline = palette.onSurface.copy(alpha = if (palette.dark) 0.10f else 0.08f),
    raisedSurface = palette.surface,
    sunkenSurface = palette.surfaceVariant.copy(alpha = 0.6f),
)

private fun definition(
    name: FableThemeName,
    palette: FablePalette,
    headingFont: FontFamily,
    headingSpacing: TextUnit,
    corner: Dp,
    ornament: String,
    brushEnd: Color,
): FableThemeDefinition {
    val defaultTypography = Typography()
    val typography = defaultTypography.copy(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = headingFont, letterSpacing = headingSpacing),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = headingFont, letterSpacing = headingSpacing),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = headingFont, letterSpacing = headingSpacing),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = headingFont, letterSpacing = headingSpacing),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = headingFont, letterSpacing = headingSpacing),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = headingFont, letterSpacing = headingSpacing),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = headingFont, fontWeight = FontWeight.SemiBold),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = headingFont, fontWeight = FontWeight.Medium),
    )
    val shape = RoundedCornerShape(corner)
    val tokens = FableThemeTokens(
        pageBrush = Brush.verticalGradient(listOf(palette.background, brushEnd)),
        cardBorder = palette.outline.copy(alpha = 0.48f),
        ornament = ornament,
        cardElevation = if (name == FableThemeName.MINIMALIST) 0.dp else 2.dp,
        brushEnd = brushEnd,
        displayFont = headingFont,
        bookTitleFont = FontFamily.Serif,
        cornerRadius = corner,
        themeName = name,
    ).withPalette(palette)
    return FableThemeDefinition(
        name = name,
        palette = palette,
        colorScheme = colorSchemeFor(palette),
        typography = typography,
        shapes = Shapes(small = shape, medium = shape, large = shape),
        tokens = tokens,
    )
}
