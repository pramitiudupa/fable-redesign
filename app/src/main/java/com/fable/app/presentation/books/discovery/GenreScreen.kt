package com.fable.app.presentation.books.discovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.CategoryBooksViewModelFactory
import com.fable.app.di.FableDependencies
import com.fable.app.domain.model.Book
import com.fable.app.presentation.books.CategoryBooksViewModel
import com.fable.app.presentation.books.components.BookGrid
import com.fable.app.presentation.books.components.BookGridSkeleton
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableErrorState
import com.fable.app.presentation.designsystem.drawCoverMotif
import com.fable.app.presentation.designsystem.drawGenreWash
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.genreColors

/**
 * A genre's own room: the header is painted in the genre's atmosphere and motif, and the
 * collection below carries the same identity. The header gently parallaxes as you scroll.
 */
@Composable
fun GenreScreen(dependencies: FableDependencies, category: String, onBookClick: (Book) -> Unit) {
    val factory = CategoryBooksViewModelFactory(dependencies, category)
    val viewModel: CategoryBooksViewModel = viewModel(key = "genre-$category", factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val genre = remember(category) { GenreThemes.fromName(category) }
    val colors = genreColors(genre)
    val gridState = rememberLazyGridState()
    val headerShift by remember {
        derivedStateOf { if (gridState.firstVisibleItemIndex == 0) gridState.firstVisibleItemScrollOffset.toFloat() else 0f }
    }
    val title = genre?.label ?: category.replaceFirstChar { it.uppercase() }
    val tokens = FableThemeTokens.current

    val header: @Composable () -> Unit = {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .graphicsLayer { translationY = headerShift * 0.35f; alpha = 1f - (headerShift / 900f).coerceIn(0f, 0.6f) },
        ) {
            Column(
                Modifier.fillMaxWidth().padding(top = screenTopPadding(8.dp), bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(genre?.let { "Genre collection" } ?: "Subject", color = colors.primary)
                }
                Text(
                    title,
                    style = MaterialTheme.typography.displayMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                    color = colors.text,
                    modifier = Modifier.semantics { heading() },
                )
                genre?.let {
                    Text(
                        it.tagline,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Italic),
                        color = colors.mutedText,
                    )
                }
                if (!state.isLoading && state.books.isNotEmpty()) {
                    Text(
                        "${state.books.size} books from Open Library",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.mutedText,
                    )
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                // The genre atmosphere washes the top of the page and fades into the theme.
                val washHeight = size.height * 0.55f
                clipRect(right = size.width, bottom = washHeight) {
                    drawGenreWash(colors)
                    drawCoverMotif(colors.motif, colors.text.copy(alpha = 0.05f), colors.accent.copy(alpha = 0.4f), title.hashCode())
                }
                drawRect(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(androidx.compose.ui.graphics.Color.Transparent, colors.background),
                        startY = washHeight * 0.55f,
                        endY = washHeight,
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width, washHeight),
                )
                drawRect(colors.background, topLeft = androidx.compose.ui.geometry.Offset(0f, washHeight), size = androidx.compose.ui.geometry.Size(size.width, size.height - washHeight))
            },
    ) {
        val padding = PaddingValues(start = FableSpacing.gutter, end = FableSpacing.gutter, bottom = screenBottomPadding())
        when {
            state.isLoading -> BookGridSkeleton(contentPadding = padding, header = header)
            state.error != null -> Column(Modifier.fillMaxSize().padding(horizontal = FableSpacing.gutter)) {
                header()
                FableErrorState(state.error.orEmpty(), viewModel::load)
            }
            state.books.isEmpty() -> Column(Modifier.fillMaxSize().padding(horizontal = FableSpacing.gutter)) {
                header()
                FableEmptyState(
                    title = "This shelf is quiet",
                    message = "Open Library has no books under “$title” right now. Try another genre.",
                    ornament = colors.ornament,
                    accent = colors.primary,
                )
            }
            else -> BookGrid(
                books = state.books,
                isLoadingMore = false,
                hasMore = false,
                onLoadMore = {},
                onBookClick = onBookClick,
                state = gridState,
                contentPadding = padding,
                header = header,
            )
        }
    }
}
