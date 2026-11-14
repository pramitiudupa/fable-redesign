package com.fable.app.presentation.books.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fable.app.domain.model.Book
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.GenreBadge
import com.fable.app.presentation.designsystem.GenreBadgeRow
import com.fable.app.presentation.designsystem.RatingStars
import com.fable.app.presentation.designsystem.bookCoverSharedKey
import com.fable.app.presentation.designsystem.genreAtmosphere
import com.fable.app.presentation.designsystem.drawCoverMotif
import com.fable.app.presentation.designsystem.pressScale
import com.fable.app.presentation.designsystem.rememberFableHaptics
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.genreColors
import com.fable.app.presentation.theme.rememberGenreMatch

/**
 * Editorial book tile: the cover floats on the page above a genre-coloured glow, with a
 * genre badge and serif title beneath — no boxy card chrome.
 */
@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedKey: String? = bookCoverSharedKey(book.id),
    showGenre: Boolean = true,
) {
    val match = rememberGenreMatch(book)
    val colors = genreColors(match.primary)
    val tokens = FableThemeTokens.current
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberFableHaptics()
    Column(
        modifier
            .pressScale(interaction, 0.96f)
            .clip(MaterialTheme.shapes.small)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button) {
                haptics.tap()
                onClick()
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.fillMaxWidth().coverGlow(colors.glow, 1.3f).padding(bottom = 10.dp)) {
            BookCover(
                url = book.thumbnailUrl,
                title = book.title,
                author = book.authors.firstOrNull(),
                genre = match.primary,
                sharedKey = sharedKey,
                modifier = Modifier.fillMaxWidth().aspectRatio(FableProportions.COVER_ASPECT),
            )
        }
        if (showGenre && match.primary != null) {
            GenreBadge(match.primary, compact = true, modifier = Modifier.padding(bottom = 2.dp))
        }
        Text(
            book.title,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = tokens.bookTitleFont),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            book.authors.firstOrNull() ?: "Author unavailable",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        book.averageRating?.let { rating ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RatingStars(rating, starSize = 11.dp)
                Text(
                    "%.1f".format(rating) + (book.ratingsCount?.let { " · ${compactCount(it)}" } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Horizontal list row with a small cover — used in search lists, history and shelves. */
@Composable
fun BookListRow(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverWidth: Dp = 72.dp,
    sharedKey: String? = bookCoverSharedKey(book.id),
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val match = rememberGenreMatch(book)
    val tokens = FableThemeTokens.current
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .pressScale(interaction, 0.98f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(
            url = book.thumbnailUrl,
            title = book.title,
            author = book.authors.firstOrNull(),
            genre = match.primary,
            sharedKey = sharedKey,
            elevation = 5.dp,
            modifier = Modifier.width(coverWidth).aspectRatio(FableProportions.COVER_ASPECT),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            GenreBadgeRow(match, compact = true)
            Text(
                book.title,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = tokens.bookTitleFont),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                book.authors.joinToString().ifBlank { "Author unavailable" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            supporting?.invoke()
        }
        trailing?.invoke()
    }
}

/**
 * The featured book: a full-bleed genre atmosphere with the cover tilted forward like a book
 * on display, an editorial headline and a single clear call to action.
 */
@Composable
fun FeaturedBookCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String = "Featured",
    sharedKey: String? = bookCoverSharedKey(book.id),
) {
    val match = rememberGenreMatch(book)
    val colors = genreColors(match.primary)
    val tokens = FableThemeTokens.current
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().pressScale(interaction, 0.985f),
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent,
        shadowElevation = 10.dp,
        interactionSource = interaction,
    ) {
        Box(
            Modifier
                .genreAtmosphere(colors)
                .then(
                    Modifier.drawBehindMotif(colors.motif, colors.text.copy(alpha = 0.06f), colors.accent, book.title.hashCode()),
                ),
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Eyebrow(eyebrow, color = colors.primary)
                        Text(
                            book.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                            color = colors.text,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            book.authors.joinToString().ifBlank { "Author unavailable" },
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.mutedText,
                            maxLines = 2,
                        )
                        book.averageRating?.let {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RatingStars(it, starSize = 13.dp, color = colors.accent, emptyColor = colors.text.copy(alpha = 0.18f))
                                Text("%.1f".format(it), style = MaterialTheme.typography.labelMedium, color = colors.mutedText)
                            }
                        }
                    }
                    Box(
                        Modifier
                            .width(118.dp)
                            .graphicsLayer { rotationZ = 3.5f }
                            .coverGlow(colors.glow, 1.4f)
                            .padding(bottom = 6.dp),
                    ) {
                        BookCover(
                            url = book.thumbnailUrl,
                            title = book.title,
                            author = book.authors.firstOrNull(),
                            genre = match.primary,
                            sharedKey = sharedKey,
                            elevation = 18.dp,
                            modifier = Modifier.fillMaxWidth().aspectRatio(FableProportions.COVER_ASPECT),
                        )
                    }
                }
                book.description?.takeIf(String::isNotBlank)?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text.copy(alpha = 0.86f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FableButton(
                        text = "Open book",
                        onClick = onClick,
                        icon = FableIcons.BookOpen,
                        accent = colors.primary,
                        onAccent = colors.onPrimary,
                    )
                    if (match.primary != null) GenreBadge(match.primary)
                }
            }
        }
    }
}

private fun Modifier.drawBehindMotif(
    motif: com.fable.app.presentation.theme.CoverMotif,
    color: Color,
    accent: Color,
    seed: Int,
): Modifier = this.drawBehind { drawCoverMotif(motif, color, accent.copy(alpha = 0.25f), seed) }

/** Formats large counts compactly, e.g. 12400 -> "12.4k". */
fun compactCount(count: Int): String = when {
    count >= 1_000_000 -> "%.1fm".format(count / 1_000_000f)
    count >= 10_000 -> "${count / 1000}k"
    count >= 1_000 -> "%.1fk".format(count / 1000f)
    else -> count.toString()
}

@Composable
internal fun VerticalSpace(height: Dp) = Spacer(Modifier.height(height))

@Composable
internal fun GenreDot(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.width(6.dp).height(6.dp).clip(MaterialTheme.shapes.extraSmall).background(color))
}
