package com.fable.app.presentation.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.FableDependencies
import com.fable.app.di.ReviewViewModelFactory
import com.fable.app.domain.model.BookReview
import com.fable.app.presentation.books.components.BookCover
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.BookRowSkeleton
import com.fable.app.presentation.designsystem.FableCard
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.PageTitle
import com.fable.app.presentation.designsystem.RatingStars
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.bookCoverSharedKey
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyReviewsScreen(
    dependencies: FableDependencies,
    userId: String,
    username: String,
    onReviewClick: (String) -> Unit,
) {
    val viewModel: ReviewViewModel = viewModel(
        key = "my-reviews-$userId",
        factory = ReviewViewModelFactory(dependencies, userId, username),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val average = remember(state.myReviews) { state.myReviews.map { it.rating }.average().takeIf { state.myReviews.isNotEmpty() } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = FableSpacing.gutter,
            end = FableSpacing.gutter,
            top = screenTopPadding(),
            bottom = screenBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "title") {
            PageTitle(
                title = "My Reviews",
                eyebrow = "Your marginalia",
                subtitle = if (average != null) {
                    "${state.myReviews.size} ${if (state.myReviews.size == 1) "book" else "books"} reviewed · average ${"%.1f".format(average)} stars"
                } else "Ratings and reviews you’ve written",
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        state.error?.let { error -> item(key = "error") { StatusBanner(error, tone = BannerTone.Error) } }
        when {
            state.isLoading && state.myReviews.isEmpty() -> items(3) { BookRowSkeleton() }
            state.myReviews.isEmpty() -> item(key = "empty") {
                FableEmptyState(
                    title = "You haven’t reviewed any books yet",
                    message = "Rate a book from its details page and it will appear here.",
                    icon = FableIcons.Feather,
                )
            }
            else -> itemsIndexed(state.myReviews, key = { _, review -> review.bookId }) { index, review ->
                ReviewHistoryCard(review, Modifier.entrance(index, key = review.bookId)) { onReviewClick(review.bookId) }
            }
        }
    }
}

@Composable
private fun ReviewHistoryCard(review: BookReview, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val tokens = FableThemeTokens.current
    FableCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
            BookCover(
                review.bookThumbnailUrl,
                review.bookTitle,
                Modifier.width(74.dp).aspectRatio(FableProportions.COVER_ASPECT),
                sharedKey = bookCoverSharedKey(review.bookId),
                elevation = 5.dp,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    review.bookTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = tokens.bookTitleFont),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RatingStars(review.rating.toDouble(), starSize = 13.dp)
                    Text(DATE_FORMAT.format(Date(review.updatedAt)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                review.reviewText?.takeIf(String::isNotBlank)?.let {
                    Text(
                        "“$it”",
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (review.pendingSync) {
                    Text("Waiting to sync", color = tokens.gilt, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private val DATE_FORMAT = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
