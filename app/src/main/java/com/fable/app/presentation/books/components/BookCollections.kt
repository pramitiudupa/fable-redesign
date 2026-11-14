package com.fable.app.presentation.books.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fable.app.domain.model.Book
import com.fable.app.presentation.designsystem.BookCardSkeleton
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableLoading
import com.fable.app.presentation.designsystem.OrnamentDivider
import com.fable.app.presentation.theme.FableSpacing

/** Horizontally scrolling shelf of [BookCard]s with editorial spacing. */
@Composable
fun BookCarousel(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 136.dp,
    sharedCovers: Boolean = true,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = FableSpacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items(books, key = Book::id) { book ->
            BookCard(
                book = book,
                onClick = { onBookClick(book) },
                modifier = Modifier.width(itemWidth),
                sharedKey = if (sharedCovers) com.fable.app.presentation.designsystem.bookCoverSharedKey(book.id) else null,
            )
        }
    }
}

/** Skeleton version of [BookCarousel] shown while a shelf loads. */
@Composable
fun BookCarouselSkeleton(modifier: Modifier = Modifier, itemWidth: Dp = 136.dp) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = FableSpacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        userScrollEnabled = false,
    ) {
        items(4) { BookCardSkeleton(Modifier.width(itemWidth)) }
    }
}

/**
 * Responsive book grid. An optional [header] scrolls with the grid so pages feel like one
 * continuous editorial spread; pagination ends with a "load more" control or an ornament.
 */
@Composable
fun BookGrid(
    books: List<Book>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = FableSpacing.gutter, vertical = 12.dp),
    header: (@Composable () -> Unit)? = null,
    showEndOrnament: Boolean = true,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 148.dp),
        state = state,
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        if (header != null) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "grid-header", contentType = "header") { header() }
        }
        items(books, key = Book::id, contentType = { "book" }) { book ->
            BookCard(book, { onBookClick(book) }, modifier = Modifier.fillMaxWidth())
        }
        if (hasMore || isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "grid-more", contentType = "more") {
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    if (isLoadingMore) FableLoading("Gathering more books…")
                    else FableButton(
                        text = "Load more",
                        onClick = onLoadMore,
                        style = FableButtonStyle.Tonal,
                        icon = FableIcons.ChevronDown,
                    )
                }
            }
        } else if (showEndOrnament && books.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "grid-end", contentType = "end") {
                OrnamentDivider(Modifier.padding(horizontal = 40.dp, vertical = 8.dp))
            }
        }
    }
}

/** Grid of skeleton cards matching [BookGrid]'s rhythm. */
@Composable
fun BookGridSkeleton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = FableSpacing.gutter, vertical = 12.dp),
    header: (@Composable () -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 148.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
        userScrollEnabled = header != null,
    ) {
        if (header != null) item(span = { GridItemSpan(maxLineSpan) }) { header() }
        items(6) { BookCardSkeleton(Modifier.fillMaxWidth()) }
    }
}
