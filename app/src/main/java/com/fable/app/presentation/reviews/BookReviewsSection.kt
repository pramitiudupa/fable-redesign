package com.fable.app.presentation.reviews

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.FableDependencies
import com.fable.app.di.ReviewViewModelFactory
import com.fable.app.domain.model.Book
import com.fable.app.domain.model.BookReview
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableCard
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableLoading
import com.fable.app.presentation.designsystem.FableTextButton
import com.fable.app.presentation.designsystem.FableTextField
import com.fable.app.presentation.designsystem.ProgressBar
import com.fable.app.presentation.designsystem.RatingInput
import com.fable.app.presentation.designsystem.RatingStars
import com.fable.app.presentation.designsystem.ReaderAvatar
import com.fable.app.presentation.designsystem.SectionHeader
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.rememberFableHaptics
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookReviewsSection(
    dependencies: FableDependencies,
    userId: String,
    username: String,
    book: Book,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val factory = ReviewViewModelFactory(dependencies, userId, username, book.id)
    val viewModel: ReviewViewModel = viewModel(key = "reviews-${book.id}-$userId", factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ownReview = state.reviews.firstOrNull { it.reviewerId == userId }
        ?: state.myReviews.firstOrNull { it.bookId == book.id }
    var ratingDraft by rememberSaveable(book.id, ownReview?.updatedAt) { mutableIntStateOf(ownReview?.rating ?: 0) }
    var reviewText by rememberSaveable(book.id, ownReview?.updatedAt) { mutableStateOf(ownReview?.reviewText.orEmpty()) }
    val haptics = rememberFableHaptics()
    val tokens = FableThemeTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Reader reviews", eyebrow = "From the Fable community", accent = accent)

        RatingSummary(state.summary.averageRating, state.summary.reviewCount, state.summary.reviews, accent)

        FableCard(Modifier.fillMaxWidth(), color = tokens.raisedSurface) {
            Column(Modifier.padding(18.dp).animateContentSize(FableMotion.gentleSpring()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Eyebrow(if (ownReview == null) "Your rating" else "Your rating and review", color = accent)
                RatingInput(ratingDraft, { ratingDraft = it }, color = tokens.gilt)
                FableTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it.take(MAX_REVIEW_LENGTH) },
                    label = "Write a review (optional)",
                    placeholder = "What stayed with you?",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 10,
                    supportingText = "${reviewText.length}/$MAX_REVIEW_LENGTH",
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FableButton(
                        text = if (ownReview == null) "Submit rating" else "Update review",
                        onClick = {
                            haptics.confirm()
                            viewModel.save(book, ratingDraft, reviewText)
                        },
                        enabled = ratingDraft in 1..5,
                        icon = FableIcons.Send,
                        accent = accent,
                    )
                    if (ownReview != null) {
                        FableTextButton(
                            "Delete",
                            onClick = {
                                haptics.reject()
                                viewModel.delete(book.id)
                            },
                            color = MaterialTheme.colorScheme.error,
                            icon = FableIcons.Trash,
                        )
                    }
                }
            }
        }

        StatusBanner(state.message, tone = BannerTone.Success, onDismiss = viewModel::clearMessage)
        StatusBanner(state.error, tone = BannerTone.Error)
        StatusBanner(
            if (state.pendingChanges > 0) "${state.pendingChanges} review change(s) waiting to sync" else null,
            tone = BannerTone.Warning,
        )

        when {
            state.isLoading && state.reviews.isEmpty() -> FableLoading("Gathering reviews…")
            state.summary.reviews.isEmpty() -> Caption("No reviews yet. Your rating will be the first one.")
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.summary.reviews.forEach { review -> ReviewCard(review, isOwn = review.reviewerId == userId, accent = accent) }
            }
        }
    }
}

/** Big average, stars, and a real distribution of ratings across the visible reviews. */
@Composable
private fun RatingSummary(average: Double?, count: Int, reviews: List<BookReview>, accent: Color) {
    val tokens = FableThemeTokens.current
    if (average == null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RatingStars(0.0, starSize = 18.dp)
            Text("No ratings yet · Be the first to rate this book", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val distribution = remember(reviews) { (1..5).associateWith { star -> reviews.count { it.rating == star } } }
    Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "%.1f".format(Locale.getDefault(), average),
                style = MaterialTheme.typography.displayMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                color = MaterialTheme.colorScheme.onSurface,
            )
            RatingStars(average, starSize = 14.dp)
            Caption("$count ${if (count == 1) "review" else "reviews"}")
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            (5 downTo 1).forEach { star ->
                val n = distribution[star] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$star", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(10.dp))
                    ProgressBar(
                        if (count == 0) 0f else n.toFloat() / count,
                        color = tokens.gilt,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f),
                    )
                    Text("$n", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: BookReview, isOwn: Boolean, accent: Color) {
    val tokens = FableThemeTokens.current
    var expanded by remember(review.updatedAt) { mutableStateOf(false) }
    val text = review.reviewText?.takeIf(String::isNotBlank)
    FableCard(Modifier.fillMaxWidth(), elevation = 0.dp) {
        Column(Modifier.padding(16.dp).animateContentSize(FableMotion.gentleSpring()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReaderAvatar(null, review.reviewerUsername, size = 38.dp, accent = accent, ring = false)
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("@${review.reviewerUsername}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        if (isOwn) {
                            Box(Modifier.clip(CircleShape).background(accent.copy(alpha = 0.14f)).padding(horizontal = 7.dp, vertical = 1.dp)) {
                                Text("You", style = MaterialTheme.typography.labelMedium, color = accent)
                            }
                        }
                    }
                    Text(
                        if (review.updatedAt > review.createdAt) {
                            "Reviewed ${DATE_FORMAT.format(Date(review.createdAt))} · Updated ${DATE_FORMAT.format(Date(review.updatedAt))}"
                        } else {
                            "Reviewed ${DATE_FORMAT.format(Date(review.createdAt))}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                RatingStars(review.rating.toDouble(), starSize = 13.dp)
            }
            text?.let {
                Text(
                    "“$it”",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(role = Role.Button) { expanded = !expanded },
                )
            }
        }
    }
}

private val DATE_FORMAT = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
private const val MAX_REVIEW_LENGTH = 4000
