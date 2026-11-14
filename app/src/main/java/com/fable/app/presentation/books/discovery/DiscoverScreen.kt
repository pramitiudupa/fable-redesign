package com.fable.app.presentation.books.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.FableDependencies
import com.fable.app.di.FableViewModelFactory
import com.fable.app.domain.model.Book
import com.fable.app.presentation.books.DiscoverViewModel
import com.fable.app.presentation.books.components.BookCarousel
import com.fable.app.presentation.books.components.BookCarouselSkeleton
import com.fable.app.presentation.books.components.FeaturedBookCard
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableErrorState
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.GenreTile
import com.fable.app.presentation.designsystem.OrnamentDivider
import com.fable.app.presentation.designsystem.PageTitle
import com.fable.app.presentation.designsystem.SectionHeader
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.designsystem.shimmer
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.Genre

/** Labels offered as quick genre doors elsewhere in the app (kept for compatibility). */
val DISCOVERY_GENRES: List<String> = Genre.showcase.map(Genre::label)

/** One row of the genre mosaic: either a single wide tile or a pair. */
private sealed interface MosaicRow {
    data class Wide(val genre: Genre) : MosaicRow
    data class Pair(val first: Genre, val second: Genre?) : MosaicRow
}

private fun buildMosaic(genres: List<Genre>): List<MosaicRow> {
    val rows = mutableListOf<MosaicRow>()
    var index = 0
    var pattern = 0
    while (index < genres.size) {
        if (pattern % 3 == 0) {
            rows += MosaicRow.Wide(genres[index])
            index += 1
        } else {
            rows += MosaicRow.Pair(genres[index], genres.getOrNull(index + 1))
            index += 2
        }
        pattern++
    }
    return rows
}

@Composable
fun DiscoverScreen(
    dependencies: FableDependencies,
    onBookClick: (Book) -> Unit,
    onGenreClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val viewModel: DiscoverViewModel = viewModel(factory = FableViewModelFactory(dependencies))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mosaic = remember { buildMosaic(Genre.showcase) }
    val popular = remember(state.popularDiscoveries, state.featuredBook) {
        state.popularDiscoveries.filterNot { it.id == state.featuredBook?.id }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = screenTopPadding(), bottom = screenBottomPadding()),
    ) {
        item(key = "title") {
            Column(Modifier.padding(horizontal = FableSpacing.gutter).entrance(0), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                PageTitle(
                    title = "Discover",
                    eyebrow = "The open shelves",
                    subtitle = "Wander by mood and genre, or go straight to the book you have in mind.",
                )
                SearchEntry(onSearchClick)
            }
        }

        item(key = "featured") {
            Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 32.dp).entrance(1)) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxWidth().height(300.dp).shimmer(MaterialTheme.shapes.extraLarge))
                    state.error != null -> FableErrorState(state.error.orEmpty(), viewModel::load)
                    state.featuredBook == null -> FableEmptyState(
                        "No discoveries yet",
                        "Open Library didn’t return a featured book. Try again in a moment.",
                        icon = FableIcons.Compass,
                        actionLabel = "Refresh",
                        onAction = viewModel::load,
                    )
                    else -> FeaturedBookCard(
                        state.featuredBook!!,
                        onClick = { onBookClick(state.featuredBook!!) },
                        eyebrow = "Featured",
                    )
                }
            }
        }

        item(key = "popular") {
            Column(Modifier.padding(top = 40.dp)) {
                SectionHeader(
                    title = "Popular discoveries",
                    eyebrow = "Most rated",
                    subtitle = "Ordered by reader rating count on Open Library.",
                    modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(bottom = 16.dp),
                )
                when {
                    state.isLoading -> BookCarouselSkeleton()
                    popular.isNotEmpty() -> BookCarousel(popular, onBookClick)
                    state.error == null -> Caption(
                        "No rated books in this selection yet.",
                        modifier = Modifier.padding(horizontal = FableSpacing.gutter),
                    )
                }
            }
        }

        item(key = "genres-header") {
            SectionHeader(
                title = "Explore genres",
                eyebrow = "${Genre.showcase.size} worlds to wander",
                subtitle = "Each genre carries its own colours, motif and mood.",
                modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 44.dp, bottom = 16.dp),
            )
        }
        itemsIndexed(mosaic, key = { index, _ -> "mosaic-$index" }) { index, row ->
            val rowModifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(bottom = 12.dp).entrance(index % 4, key = "mosaic-$index")
            when (row) {
                is MosaicRow.Wide -> GenreTile(
                    row.genre,
                    onClick = { onGenreClick(row.genre.subjectQuery) },
                    large = true,
                    modifier = rowModifier.fillMaxWidth().height(168.dp),
                )
                is MosaicRow.Pair -> Row(rowModifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenreTile(row.first, onClick = { onGenreClick(row.first.subjectQuery) }, modifier = Modifier.weight(1f).height(176.dp))
                    if (row.second != null) {
                        GenreTile(row.second, onClick = { onGenreClick(row.second.subjectQuery) }, modifier = Modifier.weight(1f).height(176.dp))
                    } else {
                        Box(Modifier.weight(1f))
                    }
                }
            }
        }
        item(key = "end") {
            OrnamentDivider(Modifier.padding(horizontal = 56.dp, vertical = 24.dp))
        }
    }
}

@Composable
internal fun SearchEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = FableThemeTokens.current
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .background(tokens.raisedSurface)
            .border(1.dp, tokens.hairline, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 20.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(FableIcons.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(
            "Search books, authors, ISBN…",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) { Icon(FableIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp)) }
    }
}
