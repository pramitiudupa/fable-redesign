package com.fable.app.presentation.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fable.app.domain.model.ReadingStatus
import com.fable.app.domain.model.UserBook
import com.fable.app.presentation.books.components.BookCover
import com.fable.app.presentation.books.components.coverGlow
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.ProgressBar
import com.fable.app.presentation.designsystem.ProgressRing
import com.fable.app.presentation.designsystem.bookCoverSharedKey
import com.fable.app.presentation.designsystem.genreAtmosphere
import com.fable.app.presentation.designsystem.pressScale
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.genreColors
import com.fable.app.presentation.theme.rememberGenreMatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * "Continue reading" card: the book's genre atmosphere behind its cover, with real page
 * progress. When the total page count is unknown, only the current page is shown.
 */
@Composable
fun ContinueReadingCard(
    entry: UserBook,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 312.dp,
    sharedCover: Boolean = true,
) {
    val match = rememberGenreMatch(entry.book)
    val colors = genreColors(match.primary)
    val tokens = FableThemeTokens.current
    val pct = entry.progress.percentage
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .pressScale(interaction, 0.97f),
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent,
        shadowElevation = 6.dp,
        interactionSource = interaction,
    ) {
        Row(
            Modifier.genreAtmosphere(colors).padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(88.dp).coverGlow(colors.glow, 1.2f).padding(bottom = 4.dp)) {
                BookCover(
                    url = entry.book.thumbnailUrl,
                    title = entry.book.title,
                    author = entry.book.authors.firstOrNull(),
                    genre = match.primary,
                    sharedKey = if (sharedCover) bookCoverSharedKey(entry.bookId) else null,
                    elevation = 12.dp,
                    modifier = Modifier.fillMaxWidth().aspectRatio(FableProportions.COVER_ASPECT),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Eyebrow(match.primary?.label ?: "Currently reading", color = colors.primary)
                Text(
                    entry.book.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                    color = colors.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.book.authors.joinToString().ifBlank { "Author unavailable" },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (pct != null) {
                    ProgressBar(
                        pct / 100f,
                        color = colors.primary,
                        secondaryColor = colors.accent,
                        trackColor = colors.text.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                Text(
                    pageLabel(entry),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.mutedText,
                )
            }
        }
    }
}

/** Compact progress ring with the percentage inside — used on shelf rows. */
@Composable
fun ProgressBadge(entry: UserBook, color: Color, modifier: Modifier = Modifier) {
    val pct = entry.progress.percentage ?: return
    ProgressRing(pct / 100f, modifier, size = 46.dp, stroke = 3.5.dp, color = color) {
        Text("$pct%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

fun pageLabel(entry: UserBook): String {
    val pct = entry.progress.percentage
    return when {
        entry.status == ReadingStatus.READ -> entry.totalPages?.let { "Finished · $it pages" } ?: "Finished"
        entry.totalPages == null -> "Page ${entry.currentPage}"
        else -> "Page ${entry.currentPage} of ${entry.totalPages}" + (pct?.let { " · $it%" } ?: "")
    }
}

internal val ShortDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
internal fun formatDate(millis: Long): String = ShortDate.format(Date(millis))
