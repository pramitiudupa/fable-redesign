package com.fable.app.presentation.reading

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.FableDependencies
import com.fable.app.di.ReadingViewModelFactory
import com.fable.app.domain.model.MonthlyReadingActivity
import com.fable.app.domain.model.ReadingStatus
import com.fable.app.domain.model.UserBook
import com.fable.app.presentation.books.components.BookCover
import com.fable.app.presentation.designsystem.BookRowSkeleton
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableCard
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.PageTitle
import com.fable.app.presentation.designsystem.ProgressBar
import com.fable.app.presentation.designsystem.SectionHeader
import com.fable.app.presentation.designsystem.StatTile
import com.fable.app.presentation.designsystem.bookCoverSharedKey
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.genreColors
import com.fable.app.presentation.theme.rememberGenreMatch
import java.text.DateFormatSymbols
import java.util.Calendar

/** A milestone earned purely from the reader's own recorded statistics. */
private data class Milestone(val title: String, val detail: String, val icon: ImageVector, val target: Int, val current: Int) {
    val achieved get() = current >= target
}

private val BOOK_MILESTONES = listOf(1, 5, 10, 25, 50, 100)
private val PAGE_MILESTONES = listOf(100, 1_000, 5_000, 10_000, 25_000, 50_000)

@Composable
fun ReadingStatisticsScreen(
    dependencies: FableDependencies,
    userId: String,
    onBookClick: (String) -> Unit = {},
) {
    val viewModel: ReadingViewModel = viewModel(key = "statistics-$userId", factory = ReadingViewModelFactory(dependencies, userId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = state.statistics
    val current = remember(state.books) {
        state.books.filter { it.status == ReadingStatus.CURRENTLY_READING }.maxByOrNull { it.updatedAt }
    }
    val milestones = remember(stats.totalBooksCompleted, stats.totalPagesRead) {
        val books = BOOK_MILESTONES.map { target ->
            Milestone(
                title = if (target == 1) "First finish" else "$target books",
                detail = if (target == 1) "Complete your first book" else "Complete $target books",
                icon = FableIcons.Trophy,
                target = target,
                current = stats.totalBooksCompleted,
            )
        }
        val pages = PAGE_MILESTONES.map { target ->
            Milestone(
                title = "${formatPages(target)} pages",
                detail = "Log ${"%,d".format(target)} pages",
                icon = FableIcons.BookOpen,
                target = target,
                current = stats.totalPagesRead,
            )
        }
        // Earned milestones first, then the next one to reach in each track.
        val earned = (books + pages).filter { it.achieved }
        val next = listOfNotNull(books.firstOrNull { !it.achieved }, pages.firstOrNull { !it.achieved })
        earned.reversed() + next
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = screenTopPadding(), bottom = screenBottomPadding()),
    ) {
        item(key = "title") {
            PageTitle(
                title = "Reading Progress",
                eyebrow = "Your reading life",
                subtitle = "Statistics from the books in your personal library.",
                modifier = Modifier.padding(horizontal = FableSpacing.gutter).entrance(0),
            )
        }
        if (state.isLoading) {
            items(3) { BookRowSkeleton(Modifier.padding(horizontal = FableSpacing.gutter, vertical = 12.dp)) }
            return@LazyColumn
        }
        current?.let { entry ->
            item(key = "current") {
                Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 28.dp).entrance(1)) {
                    Eyebrow("Currently reading", modifier = Modifier.padding(bottom = 12.dp))
                    ContinueReadingCard(entry, onClick = { onBookClick(entry.bookId) }, width = null)
                }
            }
        }
        item(key = "tiles") {
            Column(
                Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 28.dp).entrance(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(stats.totalBooksCompleted.toString(), "Books completed", Modifier.weight(1f), icon = FableIcons.Check)
                    StatTile("%,d".format(stats.totalPagesRead), "Pages read", Modifier.weight(1f), icon = FableIcons.BookOpen)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(stats.currentlyReadingCount.toString(), "Currently reading", Modifier.weight(1f), icon = FableIcons.Bookmark)
                    StatTile(stats.wantToReadCount.toString(), "Want to read", Modifier.weight(1f), icon = FableIcons.Heart)
                }
                StatTile(
                    stats.averagePagesPerCompletedBook?.toString() ?: "—",
                    if (stats.averagePagesPerCompletedBook == null) "Average pages per completed book · not enough completed books"
                    else "Average pages per completed book",
                    Modifier.fillMaxWidth(),
                    icon = FableIcons.Chart,
                )
            }
        }

        item(key = "milestones") {
            Column(Modifier.padding(top = 40.dp)) {
                SectionHeader(
                    "Milestones",
                    eyebrow = "Earned from your library",
                    modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(bottom = 16.dp),
                )
                LazyRow(contentPadding = PaddingValues(horizontal = FableSpacing.gutter), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(milestones, key = { it.title }) { MilestoneCard(it) }
                }
            }
        }

        item(key = "monthly") {
            Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 40.dp)) {
                SectionHeader("Monthly activity", eyebrow = "Books completed · last 12 months", modifier = Modifier.padding(bottom = 16.dp))
                if (stats.activityByMonth.isEmpty()) {
                    Caption("Completed-book activity will appear here when you finish a book.")
                } else {
                    MonthlyChart(stats.activityByMonth)
                }
            }
        }

        item(key = "history-header") {
            SectionHeader(
                "Reading history",
                eyebrow = "Completion timeline",
                modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 40.dp, bottom = 12.dp),
            )
        }
        if (stats.readingHistory.isEmpty()) {
            item(key = "history-empty") {
                FableEmptyState("No completed books yet", "Mark a book as Read and it will begin your timeline.", icon = FableIcons.Flag)
            }
        } else {
            itemsIndexed(stats.readingHistory, key = { _, entry -> entry.bookId }) { index, entry ->
                TimelineEntry(entry, isLast = index == stats.readingHistory.lastIndex, onClick = { onBookClick(entry.bookId) })
            }
        }
    }
}

@Composable
private fun MilestoneCard(milestone: Milestone) {
    val tokens = FableThemeTokens.current
    val accent = if (milestone.achieved) tokens.gilt else MaterialTheme.colorScheme.onSurfaceVariant
    FableCard(Modifier.width(156.dp).alpha(if (milestone.achieved) 1f else 0.85f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (milestone.achieved) Brush.linearGradient(listOf(tokens.gilt, MaterialTheme.colorScheme.primary))
                        else Brush.linearGradient(listOf(accent.copy(alpha = 0.12f), accent.copy(alpha = 0.06f))),
                    )
                    .border(1.dp, accent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(milestone.icon, null, tint = if (milestone.achieved) MaterialTheme.colorScheme.onPrimary else accent, modifier = Modifier.size(20.dp))
            }
            Text(milestone.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                if (milestone.achieved) "Achieved" else milestone.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!milestone.achieved) {
                ProgressBar(milestone.current.toFloat() / milestone.target, height = 4.dp)
                Text(
                    "${"%,d".format(milestone.current)} / ${"%,d".format(milestone.target)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Animated bar chart of completed books per month for the trailing twelve months. */
@Composable
private fun MonthlyChart(activity: List<MonthlyReadingActivity>) {
    val months = remember(activity) {
        val calendar = Calendar.getInstance()
        val byKey = activity.associateBy { it.year * 12 + (it.month - 1) }
        val nowKey = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)
        (11 downTo 0).map { back ->
            val key = nowKey - back
            val entry = byKey[key]
            Triple(key % 12, entry?.booksCompleted ?: 0, entry?.pagesRead ?: 0)
        }
    }
    val maxBooks = months.maxOf { it.second }.coerceAtLeast(1)
    val totalBooks = months.sumOf { it.second }
    val totalPages = months.sumOf { it.third }
    val grow = remember { Animatable(0f) }
    LaunchedEffect(months) { grow.animateTo(1f, tween(900, easing = FableMotion.EmphasizedDecelerate)) }
    val tokens = FableThemeTokens.current
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val symbols = remember { DateFormatSymbols.getInstance().shortMonths }
    FableCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("$totalBooks", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    Caption("books in 12 months")
                }
                Column {
                    Text("%,d".format(totalPages), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    Caption("pages in 12 months")
                }
            }
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .semantics {
                        contentDescription = months.joinToString { "${symbols[it.first]}: ${it.second} books" }
                    },
            ) {
                val slot = size.width / months.size
                val barWidth = slot * 0.56f
                months.forEachIndexed { index, (_, books, _) ->
                    val x = index * slot + (slot - barWidth) / 2
                    drawRoundRect(track, Offset(x, 0f), Size(barWidth, size.height), CornerRadius(barWidth / 2))
                    if (books > 0) {
                        val h = size.height * (books.toFloat() / maxBooks) * grow.value
                        drawRoundRect(
                            Brush.verticalGradient(listOf(tokens.gilt, primary), startY = size.height - h, endY = size.height),
                            Offset(x, size.height - h),
                            Size(barWidth, h),
                            CornerRadius(barWidth / 2),
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth()) {
                months.forEach { (month, _, _) ->
                    Text(
                        symbols[month].take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineEntry(entry: UserBook, isLast: Boolean, onClick: () -> Unit) {
    val match = rememberGenreMatch(entry.book)
    val colors = genreColors(match.primary)
    val tokens = FableThemeTokens.current
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = FableSpacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.width(16.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.padding(top = 30.dp).size(12.dp).clip(CircleShape).background(Brush.linearGradient(colors.coverGradient)).border(2.dp, tokens.raisedSurface, CircleShape))
            if (!isLast) Box(Modifier.width(1.5.dp).weight(1f).background(tokens.hairline.copy(alpha = 0.2f)))
        }
        FableCard(Modifier.weight(1f).padding(bottom = 12.dp), onClick = onClick, elevation = 0.dp) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                BookCover(
                    entry.book.thumbnailUrl,
                    entry.book.title,
                    Modifier.width(52.dp).aspectRatio(FableProportions.COVER_ASPECT),
                    author = entry.book.authors.firstOrNull(),
                    genre = match.primary,
                    sharedKey = bookCoverSharedKey(entry.bookId),
                    elevation = 4.dp,
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                    )
                    val finished = entry.finishedAt ?: entry.completionHistory.maxOrNull()
                    Text(
                        buildString {
                            finished?.let { append("Finished ${formatDate(it)}") }
                            if (entry.completionHistory.size > 1) append(" · read ${entry.completionHistory.size} times")
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                    )
                }
            }
        }
    }
}
