package com.fable.app.presentation.books.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.fable.app.presentation.designsystem.FableChip
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.genreColors

/**
 * A chip that adopts the colour identity of the genre it names. Unknown labels fall back to
 * the global theme colours.
 */
@Composable
fun GenreChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false) {
    val genre = remember(label) { GenreThemes.fromName(label) }
    val colors = genreColors(genre)
    FableChip(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        accent = colors.primary,
        onAccent = colors.onPrimary,
        leading = colors.ornament,
    )
}
