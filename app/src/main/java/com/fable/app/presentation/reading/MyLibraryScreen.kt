package com.fable.app.presentation.reading

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.FableDependencies
import com.fable.app.di.ReadingViewModelFactory
import com.fable.app.domain.model.ReadingStatus
import com.fable.app.domain.model.UserBook
import com.fable.app.presentation.books.components.BookCover
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.BookRowSkeleton
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.FableCard
import com.fable.app.presentation.designsystem.FableChip
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableIconButton
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableTextButton
import com.fable.app.presentation.designsystem.FableTextField
import com.fable.app.presentation.designsystem.GenreBadgeRow
import com.fable.app.presentation.designsystem.PageTitle
import com.fable.app.presentation.designsystem.ProgressBar
import com.fable.app.presentation.designsystem.SegmentedTabs
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.bookCoverSharedKey
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.pressScale
import com.fable.app.presentation.designsystem.rememberFableHaptics
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.genreColors
import com.fable.app.presentation.theme.rememberGenreMatch

private enum class LibraryLayout { Shelf, List }

@Composable
fun MyLibraryScreen(
    dependencies: FableDependencies,
    userId: String,
    onBookClick: (String) -> Unit,
    onProgressClick: () -> Unit,
) {
    val viewModel: ReadingViewModel = viewModel(key = "reading-$userId", factory = ReadingViewModelFactory(dependencies, userId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var layout by rememberSaveable { mutableStateOf(LibraryLayout.Shelf) }
    val visible = state.visibleBooks
    val counts = remember(state.books) { ReadingStatus.entries.associateWith { status -> state.books.count { it.status == status } } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val columns = (maxWidth.value / 118f).toInt().coerceIn(3, 6)
        val rows = remember(visible, columns) { visible.chunked(columns) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = screenTopPadding(), bottom = screenBottomPadding()),
        ) {
            item(key = "title") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = FableSpacing.gutter).entrance(0),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    PageTitle(
                        title = "My Library",
                        eyebrow = "Your shelves",
                        subtitle = "${state.books.size} ${if (state.books.size == 1) "book" else "books"} · ${state.statistics.totalPagesRead} pages logged",
                        modifier = Modifier.weight(1f),
                    )
                    FableIconButton(FableIcons.Chart, "Reading statistics", onProgressClick)
                }
            }
            item(key = "shelves") {
                SegmentedTabs(
                    options = ReadingStatus.entries,
                    selected = state.selectedShelf,
                    onSelect = viewModel::selectShelf,
                    label = { it.shortLabel() },
                    count = { counts[it] ?: 0 },
                    modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 24.dp),
                )
            }
            item(key = "search") {
                FableTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    label = "Search your library",
                    placeholder = "Title or author",
                    leadingIcon = FableIcons.Search,
                    keyboardActions = KeyboardActions.Default,
                    trailing = if (state.query.isNotEmpty()) {
                        { FableIconButton(FableIcons.Close, "Clear", { viewModel.setQuery("") }, filled = false, size = 40.dp) }
                    } else null,
                    modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 18.dp),
                )
            }
            if (state.categories.isNotEmpty()) {
                item(key = "categories") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = FableSpacing.gutter),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        item { FableChip("All categories", state.category == null, { viewModel.setCategory(null) }) }
                        items(state.categories, key = { it }) { category ->
                            val colors = genreColors(GenreThemes.fromName(category))
                            FableChip(
                                category,
                                state.category == category,
                                { viewModel.setCategory(category) },
                                accent = colors.primary,
                                onAccent = colors.onPrimary,
                                leading = colors.ornament,
                            )
                        }
                    }
                }
            }
            item(key = "sort") {
                Row(
                    Modifier.fillMaxWidth().padding(start = FableSpacing.gutter, end = 12.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(FableIcons.Sort, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(18.dp))
                    LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LibrarySort.entries) { sort ->
                            FableChip(sort.label(), state.sort == sort, { viewModel.setSort(sort) })
                        }
                    }
                    FableIconButton(
                        icon = if (layout == LibraryLayout.Shelf) FableIcons.Rows else FableIcons.Grid,
                        contentDescription = if (layout == LibraryLayout.Shelf) "Show as list" else "Show as shelf",
                        onClick = { layout = if (layout == LibraryLayout.Shelf) LibraryLayout.List else LibraryLayout.Shelf },
                        filled = false,
                    )
                }
            }
            item(key = "banners") {
                Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBanner(state.message, tone = BannerTone.Success, onDismiss = viewModel::clearMessage)
                    StatusBanner(state.error, tone = BannerTone.Error)
                    StatusBanner(
                        if (state.syncState.pendingChanges > 0) "${state.syncState.pendingChanges} saved change(s) waiting to sync" else null,
                        tone = BannerTone.Warning,
                    )
                    StatusBanner(state.syncState.errorMessage, tone = BannerTone.Error)
                }
            }
            when {
                state.isLoading -> items(4) {
                    BookRowSkeleton(Modifier.padding(horizontal = FableSpacing.gutter, vertical = 10.dp))
                }
                visible.isEmpty() -> item(key = "empty") { LibraryEmptyState(state.selectedShelf, filtered = state.query.isNotBlank() || state.category != null) }
                layout == LibraryLayout.Shelf -> itemsIndexed(rows, key = { _, row -> row.joinToString { it.bookId } }) { index, row ->
                    ShelfRow(
                        books = row,
                        columns = columns,
                        onOpen = onBookClick,
                        onMove = { entry, status -> viewModel.addOrMove(entry.book, status) },
                        onRemove = { entry -> viewModel.removeBook(entry.bookId) },
                        modifier = Modifier.padding(top = if (index == 0) 22.dp else 6.dp).entrance(index, key = "shelf-${row.first().bookId}"),
                    )
                }
                else -> itemsIndexed(visible, key = { _, entry -> entry.bookId }) { index, entry ->
                    LibraryBookCard(
                        entry = entry,
                        onOpen = { onBookClick(entry.bookId) },
                        onMove = { status -> viewModel.addOrMove(entry.book, status) },
                        onRemove = { viewModel.removeBook(entry.bookId) },
                        modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 12.dp).entrance(index, key = entry.bookId),
                    )
                }
            }
            if (!state.isLoading && visible.isNotEmpty() && layout == LibraryLayout.Shelf) {
                item(key = "hint") {
                    Caption(
                        "Tip: press and hold a book to move it between shelves.",
                        modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 18.dp),
                    )
                }
            }
        }
    }
}

/**
 * One plank of the bookshelf: covers stand on a wooden ledge with a soft shadow. Long-press
 * a book for shelf actions.
 */
@Composable
private fun ShelfRow(
    books: List<UserBook>,
    columns: Int,
    onOpen: (String) -> Unit,
    onMove: (UserBook, ReadingStatus) -> Unit,
    onRemove: (UserBook) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = FableThemeTokens.current
    val plank = if (tokens.dark) lerp(tokens.raisedSurface, tokens.gilt, 0.18f) else lerp(MaterialTheme.colorScheme.surfaceVariant, tokens.gilt, 0.22f)
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = FableSpacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            books.forEach { entry ->
                ShelfBook(entry, onOpen, onMove, onRemove, Modifier.weight(1f))
            }
            repeat(columns - books.size) { Spacer(Modifier.weight(1f)) }
        }
        // The plank
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(12.dp)
                .drawBehind {
                    drawRect(Brush.verticalGradient(listOf(lerp(plank, Color.White, 0.15f), plank, lerp(plank, Color.Black, 0.25f))), size = Size(size.width, 8.dp.toPx()))
                    drawRect(
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.18f), Color.Transparent)),
                        topLeft = Offset(0f, 8.dp.toPx()),
                        size = Size(size.width, 4.dp.toPx()),
                    )
                },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShelfBook(
    entry: UserBook,
    onOpen: (String) -> Unit,
    onMove: (UserBook, ReadingStatus) -> Unit,
    onRemove: (UserBook) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember(entry.bookId) { mutableStateOf(false) }
    val match = rememberGenreMatch(entry.book)
    val colors = genreColors(match.primary)
    val haptics = rememberFableHaptics()
    val interaction = remember { MutableInteractionSource() }
    Box(modifier) {
        Column(
            Modifier
                .pressScale(interaction, 0.94f)
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onLongClick = {
                        haptics.longPress()
                        menuOpen = true
                    },
                    onClick = { onOpen(entry.bookId) },
                )
                .semantics { onLongClick(label = "Shelf actions") { menuOpen = true; true } },
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BookCover(
                url = entry.book.thumbnailUrl,
                title = entry.book.title,
                author = entry.book.authors.firstOrNull(),
                genre = match.primary,
                sharedKey = bookCoverSharedKey(entry.bookId),
                elevation = 6.dp,
                modifier = Modifier.fillMaxWidth().aspectRatio(FableProportions.COVER_ASPECT),
            )
            entry.progress.percentage?.takeIf { entry.status == ReadingStatus.CURRENTLY_READING }?.let { pct ->
                ProgressBar(pct / 100f, color = colors.primary, secondaryColor = colors.accent, height = 3.dp)
            }
            Spacer(Modifier.height(2.dp))
        }
        ShelfMenu(menuOpen, entry, onDismiss = { menuOpen = false }, onMove = { onMove(entry, it) }, onRemove = { onRemove(entry) })
    }
}

@Composable
private fun ShelfMenu(
    expanded: Boolean,
    entry: UserBook,
    onDismiss: () -> Unit,
    onMove: (ReadingStatus) -> Unit,
    onRemove: () -> Unit,
) {
    val haptics = rememberFableHaptics()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        containerColor = FableThemeTokens.current.raisedSurface,
    ) {
        ReadingStatus.entries.filterNot { it == entry.status }.forEach { status ->
            DropdownMenuItem(
                text = { Text("Move to ${status.label()}") },
                leadingIcon = { Icon(status.icon(), null, modifier = Modifier.width(18.dp)) },
                onClick = {
                    onDismiss()
                    haptics.confirm()
                    onMove(status)
                },
            )
        }
        DropdownMenuItem(
            text = { Text("Remove from library", color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(FableIcons.Trash, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.width(18.dp)) },
            onClick = {
                onDismiss()
                haptics.reject()
                onRemove()
            },
        )
    }
}

@Composable
private fun LibraryBookCard(
    entry: UserBook,
    onOpen: () -> Unit,
    onMove: (ReadingStatus) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember(entry.bookId) { mutableStateOf(false) }
    val match = rememberGenreMatch(entry.book)
    val colors = genreColors(match.primary)
    val tokens = FableThemeTokens.current
    FableCard(modifier.fillMaxWidth(), onClick = onOpen) {
        Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCover(
                entry.book.thumbnailUrl,
                entry.book.title,
                Modifier.width(76.dp).aspectRatio(FableProportions.COVER_ASPECT),
                author = entry.book.authors.firstOrNull(),
                genre = match.primary,
                sharedKey = bookCoverSharedKey(entry.bookId),
                elevation = 5.dp,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                GenreBadgeRow(match, compact = true)
                Text(
                    entry.book.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.book.authors.joinToString().ifBlank { "Author unavailable" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (entry.status == ReadingStatus.CURRENTLY_READING) {
                    entry.progress.percentage?.let { pct ->
                        ProgressBar(pct / 100f, color = colors.primary, secondaryColor = colors.accent, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Text(pageLabel(entry), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.syncPending) Text("Waiting to sync", style = MaterialTheme.typography.labelMedium, color = tokens.gilt)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box {
                        FableTextButton("Move", { menuOpen = true }, icon = FableIcons.Swap, color = colors.primary)
                        ShelfMenu(menuOpen, entry, onDismiss = { menuOpen = false }, onMove = onMove, onRemove = onRemove)
                    }
                    FableTextButton("Remove", onRemove, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(status: ReadingStatus, filtered: Boolean) {
    if (filtered) {
        FableEmptyState(
            title = "No matches on this shelf",
            message = "Try a different title, author or category filter.",
            icon = FableIcons.Search,
        )
        return
    }
    FableEmptyState(
        title = when (status) {
            ReadingStatus.WANT_TO_READ -> "Your wishlist awaits"
            ReadingStatus.CURRENTLY_READING -> "Nothing on your nightstand"
            ReadingStatus.READ -> "Your finished shelf is waiting"
        },
        message = "Add a book from Discover or Search and it will appear in ${status.label()}.",
        icon = status.icon(),
    )
}

private fun ReadingStatus.shortLabel() = when (this) {
    ReadingStatus.WANT_TO_READ -> "To read"
    ReadingStatus.CURRENTLY_READING -> "Reading"
    ReadingStatus.READ -> "Read"
}

private fun ReadingStatus.icon() = when (this) {
    ReadingStatus.WANT_TO_READ -> FableIcons.Bookmark
    ReadingStatus.CURRENTLY_READING -> FableIcons.BookOpen
    ReadingStatus.READ -> FableIcons.Check
}

private fun LibrarySort.label() = when (this) {
    LibrarySort.ADDED_DATE -> "Date added"
    LibrarySort.TITLE -> "Title"
    LibrarySort.AUTHOR -> "Author"
}
