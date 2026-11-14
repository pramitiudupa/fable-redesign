package com.fable.app.presentation.books.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.FableDependencies
import com.fable.app.di.FableViewModelFactory
import com.fable.app.domain.model.Book
import com.fable.app.domain.repository.BookSearchType
import com.fable.app.presentation.books.BookSearchUiState
import com.fable.app.presentation.books.BookSearchViewModel
import com.fable.app.presentation.books.components.BookGrid
import com.fable.app.presentation.books.components.BookGridSkeleton
import com.fable.app.presentation.books.components.BookListRow
import com.fable.app.presentation.books.components.BookSearchBar
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.BookRowSkeleton
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableChip
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableErrorState
import com.fable.app.presentation.designsystem.FableIconButton
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableLoading
import com.fable.app.presentation.designsystem.FableTextButton
import com.fable.app.presentation.designsystem.GenreTile
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.Genre
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.genreColors

private enum class ResultLayout { Grid, List }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookSearchScreen(
    dependencies: FableDependencies,
    onBookClick: (Book) -> Unit,
) {
    val viewModel: BookSearchViewModel = viewModel(factory = FableViewModelFactory(dependencies))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var layout by rememberSaveable { mutableStateOf(ResultLayout.Grid) }
    var genreFilter by rememberSaveable(state.query, state.type) { mutableStateOf<String?>(null) }
    var authorFilter by rememberSaveable(state.query, state.type) { mutableStateOf<String?>(null) }

    // Facets are derived from the real results so filters only offer what is actually there.
    val genreFacets = remember(state.books) {
        state.books.mapNotNull { GenreThemes.resolve(it.categories).primary }
            .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key }.take(8)
    }
    val authorFacets = remember(state.books) {
        state.books.mapNotNull { it.authors.firstOrNull() }
            .groupingBy { it }.eachCount().entries.filter { it.value > 1 }.sortedByDescending { it.value }.map { it.key }.take(6)
    }
    val visibleBooks = remember(state.books, genreFilter, authorFilter) {
        state.books.filter { book ->
            (genreFilter == null || GenreThemes.resolve(book.categories).primary?.name == genreFilter) &&
                (authorFilter == null || book.authors.firstOrNull() == authorFilter)
        }
    }
    val runSubjectSearch: (Genre) -> Unit = { genre ->
        viewModel.onTypeChange(BookSearchType.SUBJECT)
        viewModel.onQueryChange(genre.subjectQuery)
        viewModel.submitSearch()
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.padding(horizontal = FableSpacing.gutter).padding(top = screenTopPadding()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("Open Library catalogue")
                    Text("Search", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                }
                if (state.books.isNotEmpty()) {
                    FableIconButton(
                        icon = if (layout == ResultLayout.Grid) FableIcons.Rows else FableIcons.Grid,
                        contentDescription = if (layout == ResultLayout.Grid) "Show as list" else "Show as grid",
                        onClick = { layout = if (layout == ResultLayout.Grid) ResultLayout.List else ResultLayout.Grid },
                    )
                }
            }
            BookSearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::submitSearch,
                onClear = viewModel::clearQuery,
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = FableSpacing.gutter, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(BookSearchType.entries) { type ->
                FableChip(label = type.label(), selected = state.type == type, onClick = { viewModel.onTypeChange(type) })
            }
        }

        val contentState = when {
            state.isLoading -> "loading"
            state.error != null && state.books.isEmpty() -> "error"
            state.hasSearched && state.books.isEmpty() -> "empty"
            state.books.isNotEmpty() -> "results"
            else -> "idle"
        }
        AnimatedContent(
            targetState = contentState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "searchContent",
        ) { mode ->
            val bottom = PaddingValues(start = FableSpacing.gutter, end = FableSpacing.gutter, top = 4.dp, bottom = screenBottomPadding())
            when (mode) {
                "loading" -> if (layout == ResultLayout.Grid) BookGridSkeleton(contentPadding = bottom)
                else Column(Modifier.padding(horizontal = FableSpacing.gutter), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    repeat(5) { BookRowSkeleton() }
                }
                "error" -> FableErrorState(state.error.orEmpty(), viewModel::submitSearch)
                "empty" -> FableEmptyState(
                    title = "No books found",
                    message = "Nothing matched “${state.query.trim()}”. Try a different title, author, ISBN, or subject.",
                    icon = FableIcons.Search,
                    actionLabel = "Clear search",
                    onAction = viewModel::clearQuery,
                )
                "results" -> {
                    val filters: @Composable () -> Unit = {
                        ResultFilters(
                            state = state,
                            shown = visibleBooks.size,
                            genreFacets = genreFacets,
                            authorFacets = authorFacets,
                            genreFilter = genreFilter,
                            authorFilter = authorFilter,
                            onGenre = { genreFilter = if (genreFilter == it) null else it },
                            onAuthor = { authorFilter = if (authorFilter == it) null else it },
                        )
                    }
                    if (layout == ResultLayout.Grid) {
                        BookGrid(
                            books = visibleBooks,
                            isLoadingMore = state.isLoadingMore,
                            hasMore = state.hasMore,
                            onLoadMore = viewModel::loadNextPage,
                            onBookClick = onBookClick,
                            contentPadding = bottom,
                            header = filters,
                        )
                    } else {
                        LazyColumn(contentPadding = bottom, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item(key = "filters") { filters() }
                            items(visibleBooks, key = Book::id) { book ->
                                BookListRow(book, onClick = { onBookClick(book) }, supporting = {
                                    book.publishedDate?.let {
                                        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                })
                            }
                            if (state.hasMore || state.isLoadingMore) item(key = "more") {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    if (state.isLoadingMore) FableLoading("Gathering more books…")
                                    else FableButton("Load more", viewModel::loadNextPage, style = FableButtonStyle.Tonal, icon = FableIcons.ChevronDown)
                                }
                            }
                        }
                    }
                }
                else -> IdleSearch(
                    state = state,
                    onHistory = viewModel::useHistory,
                    onClearHistory = viewModel::clearSearchHistory,
                    onGenre = runSubjectSearch,
                    contentPadding = bottom,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultFilters(
    state: BookSearchUiState,
    shown: Int,
    genreFacets: List<Genre>,
    authorFacets: List<String>,
    genreFilter: String?,
    authorFilter: String?,
    onGenre: (String) -> Unit,
    onAuthor: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            if (state.totalItems > 0) "Showing $shown of ${"%,d".format(state.totalItems)} results"
            else "$shown results",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.error?.let { StatusBanner(it, tone = BannerTone.Error) }
        if (genreFacets.size > 1) {
            Eyebrow("Filter by genre", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                genreFacets.forEach { genre ->
                    val colors = genreColors(genre)
                    FableChip(
                        label = genre.label,
                        selected = genreFilter == genre.name,
                        onClick = { onGenre(genre.name) },
                        accent = colors.primary,
                        onAccent = colors.onPrimary,
                        leading = colors.ornament,
                    )
                }
            }
        }
        if (authorFacets.isNotEmpty()) {
            Eyebrow("Filter by author", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                authorFacets.forEach { author ->
                    FableChip(label = author, selected = authorFilter == author, onClick = { onAuthor(author) }, icon = FableIcons.Feather)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdleSearch(
    state: BookSearchUiState,
    onHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onGenre: (Genre) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(contentPadding = contentPadding, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (state.searchHistory.isNotEmpty()) {
            item(key = "history-header") {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("Recent searches", modifier = Modifier.weight(1f))
                    FableTextButton("Clear", onClearHistory)
                }
            }
            items(state.searchHistory.take(6), key = { "${it.query}-${it.searchedAtMillis}" }) { entry ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(role = Role.Button) { onHistory(entry.query) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(FableIcons.Clock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Text(
                        entry.query,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(FableIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }
        item(key = "suggest-header") {
            Column(Modifier.padding(top = 18.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Eyebrow("Search by mood")
                Text("Not sure what you’re after?", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "Tap a genre to search Open Library subjects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item(key = "suggest") {
            val suggestions = remember { Genre.showcase.take(8).chunked(2) }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                suggestions.forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { genre ->
                            GenreTile(genre, onClick = { onGenre(genre) }, modifier = Modifier.weight(1f).height(112.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun BookSearchType.label(): String = when (this) {
    BookSearchType.GENERAL -> "All"
    BookSearchType.TITLE -> "Title"
    BookSearchType.AUTHOR -> "Author"
    BookSearchType.ISBN -> "ISBN"
    BookSearchType.SUBJECT -> "Subject"
}
