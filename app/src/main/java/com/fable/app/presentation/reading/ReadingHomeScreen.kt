package com.fable.app.presentation.reading

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.CategoryBooksViewModelFactory
import com.fable.app.di.FableDependencies
import com.fable.app.di.FableViewModelFactory
import com.fable.app.di.ReadingViewModelFactory
import com.fable.app.domain.model.Book
import com.fable.app.domain.model.ReadingStatus
import com.fable.app.domain.model.ReaderActivity
import com.fable.app.presentation.books.CategoryBooksViewModel
import com.fable.app.presentation.books.DiscoverViewModel
import com.fable.app.presentation.books.components.BookCarousel
import com.fable.app.presentation.books.components.BookCarouselSkeleton
import com.fable.app.presentation.books.components.FeaturedBookCard
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableCard
import com.fable.app.presentation.designsystem.FableIconButton
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.GenreTile
import com.fable.app.presentation.designsystem.OrnamentDivider
import com.fable.app.presentation.designsystem.ReaderAvatar
import com.fable.app.presentation.designsystem.SectionHeader
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.designsystem.shimmer
import com.fable.app.presentation.social.ActivityLine
import com.fable.app.presentation.social.SocialViewModel
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.Genre
import com.fable.app.presentation.theme.GenreThemes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Home — composed like the opening spread of a literary magazine: a dated masthead and
 * greeting, a featured book, the reader's own progress, genre doors, and shelves of real books.
 * Every number and list comes from the reader's library, Open Library or their friends.
 */
@Composable
fun ReadingHomeScreen(
    dependencies: FableDependencies,
    userId: String,
    displayName: String,
    onDiscover: () -> Unit,
    onSearch: () -> Unit,
    onLibrary: () -> Unit,
    onStatistics: () -> Unit,
    onMyReviews: () -> Unit,
    onDiscoverReaders: () -> Unit,
    onFriends: () -> Unit,
    onFriendRequests: () -> Unit,
    onActivityFeed: () -> Unit,
    onMyProfile: () -> Unit,
    onSelectTheme: () -> Unit,
    onBookClick: (String) -> Unit,
    profileImageUrl: String? = null,
    favouriteGenres: List<String> = emptyList(),
    socialViewModel: SocialViewModel? = null,
    pendingRequestCount: Int = 0,
    onGenreClick: (String) -> Unit = {},
    onOpenAccount: () -> Unit = onMyProfile,
) {
    val viewModel: ReadingViewModel = viewModel(key = "home-reading-$userId", factory = ReadingViewModelFactory(dependencies, userId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val discoverViewModel: DiscoverViewModel = viewModel(key = "home-discover", factory = FableViewModelFactory(dependencies))
    val discover by discoverViewModel.uiState.collectAsStateWithLifecycle()

    // Genres the reader told us they love, resolved to Fable's genre identities.
    val lovedGenres = remember(favouriteGenres) { favouriteGenres.mapNotNull(GenreThemes::fromName).distinct() }
    val curatedGenre = lovedGenres.firstOrNull()
    val curatedViewModel: CategoryBooksViewModel? = curatedGenre?.let {
        viewModel(key = "home-curated-${it.subjectQuery}", factory = CategoryBooksViewModelFactory(dependencies, it.subjectQuery))
    }
    val curated = curatedViewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val social = socialViewModel?.uiState?.collectAsStateWithLifecycle()?.value

    val currentReads = remember(state.books) {
        state.books.filter { it.status == ReadingStatus.CURRENTLY_READING }.sortedByDescending { it.updatedAt }.take(6)
    }
    val featured = discover.featuredBook
    // Keep each book in one place on the page so shelves never repeat each other.
    val shownIds = remember(featured, currentReads) { (listOfNotNull(featured?.id) + currentReads.map { it.bookId }).toSet() }
    val curatedBooks = remember(curated?.books, shownIds) { curated?.books.orEmpty().filterNot { it.id in shownIds }.take(12) }
    val popular = remember(discover.popularDiscoveries, shownIds, curatedBooks) {
        val excluded = shownIds + curatedBooks.map(Book::id)
        discover.popularDiscoveries.filterNot { it.id in excluded }.take(12)
    }
    val recentlyShelved = remember(state.books, shownIds, popular, curatedBooks) {
        val excluded = shownIds + popular.map(Book::id) + curatedBooks.map(Book::id)
        state.books.filter { it.status != ReadingStatus.CURRENTLY_READING && it.bookId !in excluded }
            .sortedByDescending { it.addedAt }.take(10)
    }
    val genreDoors = remember(lovedGenres) { (lovedGenres + Genre.showcase).distinct().take(10) }
    val stats = state.statistics

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = screenTopPadding(8.dp), bottom = screenBottomPadding()),
    ) {
        item(key = "masthead") {
            Masthead(
                displayName = displayName,
                profileImageUrl = profileImageUrl,
                pendingRequestCount = pendingRequestCount,
                onAccount = onOpenAccount,
                onRequests = onFriendRequests,
                onSearch = onSearch,
                modifier = Modifier.padding(horizontal = FableSpacing.gutter).entrance(0),
            )
        }

        item(key = "featured") {
            Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 28.dp).entrance(1)) {
                AnimatedContent(
                    targetState = when {
                        featured != null -> 2
                        discover.isLoading -> 1
                        else -> 0
                    },
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "featured",
                ) { mode ->
                    when (mode) {
                        2 -> featured?.let { FeaturedBookCard(it, onClick = { onBookClick(it.id) }, eyebrow = "Featured") }
                        1 -> Box(Modifier.fillMaxWidth().height(300.dp).shimmer(MaterialTheme.shapes.extraLarge))
                        else -> discover.error?.let {
                            StatusBanner("Featured books are unavailable right now. $it", tone = BannerTone.Warning)
                        } ?: Spacer(Modifier.height(0.dp))
                    }
                }
            }
        }

        item(key = "continue-header") {
            SectionHeader(
                title = "Continue reading",
                eyebrow = "Your bookmark",
                actionLabel = if (currentReads.isNotEmpty()) "Library" else null,
                onAction = onLibrary,
                modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 40.dp, bottom = 16.dp),
            )
        }
        item(key = "continue") {
            when {
                state.isLoading -> LazyRow(
                    contentPadding = PaddingValues(horizontal = FableSpacing.gutter),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    userScrollEnabled = false,
                ) { items(2) { Box(Modifier.width(312.dp).height(170.dp).shimmer(MaterialTheme.shapes.extraLarge)) } }
                currentReads.isEmpty() -> InvitationCard(
                    title = "Nothing on your nightstand",
                    message = "Start a book from Discover and your progress will be waiting for you here.",
                    actionLabel = "Find a book",
                    onAction = onDiscover,
                    modifier = Modifier.padding(horizontal = FableSpacing.gutter),
                )
                else -> LazyRow(
                    contentPadding = PaddingValues(horizontal = FableSpacing.gutter),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(currentReads, key = { it.bookId }) { entry ->
                        ContinueReadingCard(entry, onClick = { onBookClick(entry.bookId) })
                    }
                }
            }
        }

        item(key = "journey") {
            Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 40.dp)) {
                SectionHeader(
                    title = "Your reading journey",
                    eyebrow = "At a glance",
                    actionLabel = "Statistics",
                    onAction = onStatistics,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                JourneyPanel(
                    reading = stats.currentlyReadingCount,
                    completed = stats.totalBooksCompleted,
                    pages = stats.totalPagesRead,
                    wantToRead = stats.wantToReadCount,
                    onClick = onStatistics,
                )
            }
        }

        item(key = "genres-header") {
            SectionHeader(
                title = "Discover by genre",
                eyebrow = if (lovedGenres.isNotEmpty()) "Your favourites first" else "Every shelf has a mood",
                actionLabel = "All genres",
                onAction = onDiscover,
                modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 40.dp, bottom = 16.dp),
            )
        }
        item(key = "genres") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = FableSpacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(genreDoors, key = { it.name }) { genre ->
                    GenreTile(genre, onClick = { onGenreClick(genre.subjectQuery) }, modifier = Modifier.size(width = 148.dp, height = 186.dp))
                }
            }
        }

        if (curatedGenre != null) {
            item(key = "curated") {
                Column(Modifier.padding(top = 40.dp)) {
                    SectionHeader(
                        title = "Curated for you",
                        eyebrow = "Because you love ${curatedGenre.label}",
                        actionLabel = "More",
                        onAction = { onGenreClick(curatedGenre.subjectQuery) },
                        modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(bottom = 16.dp),
                    )
                    when {
                        curated?.isLoading == true -> BookCarouselSkeleton()
                        curatedBooks.isNotEmpty() -> BookCarousel(curatedBooks, { onBookClick(it.id) })
                        else -> Caption(
                            curated?.error ?: "No ${curatedGenre.label} books to suggest right now.",
                            modifier = Modifier.padding(horizontal = FableSpacing.gutter),
                        )
                    }
                }
            }
        }

        item(key = "popular") {
            Column(Modifier.padding(top = 40.dp)) {
                SectionHeader(
                    title = "Popular among readers",
                    eyebrow = "Most rated on Open Library",
                    actionLabel = "Discover",
                    onAction = onDiscover,
                    modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(bottom = 16.dp),
                )
                when {
                    discover.isLoading -> BookCarouselSkeleton()
                    popular.isNotEmpty() -> BookCarousel(popular, { onBookClick(it.id) })
                    else -> Caption(
                        "Popularity data is not available right now.",
                        modifier = Modifier.padding(horizontal = FableSpacing.gutter),
                    )
                }
            }
        }

        if (recentlyShelved.isNotEmpty()) {
            item(key = "recent") {
                Column(Modifier.padding(top = 40.dp)) {
                    SectionHeader(
                        title = "Recently explored",
                        eyebrow = "Lately on your shelves",
                        actionLabel = "Library",
                        onAction = onLibrary,
                        modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(bottom = 16.dp),
                    )
                    BookCarousel(recentlyShelved.map { it.book }, { onBookClick(it.id) }, itemWidth = 116.dp)
                }
            }
        }

        item(key = "circle") {
            Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 40.dp)) {
                SectionHeader(
                    title = "Your reading circle",
                    eyebrow = "Community",
                    actionLabel = if (!social?.activityFeed.isNullOrEmpty()) "Activity" else null,
                    onAction = onActivityFeed,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                CircleSection(
                    activity = social?.activityFeed.orEmpty().take(3),
                    friendCount = social?.friends?.size ?: 0,
                    onActivity = onActivityFeed,
                    onReaderBook = onBookClick,
                )
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CommunityDoor("Find readers", FableIcons.UserPlus, onDiscoverReaders, Modifier.weight(1f))
                    CommunityDoor("Friends", FableIcons.Users, onFriends, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CommunityDoor("My reviews", FableIcons.StarOutline, onMyReviews, Modifier.weight(1f))
                    CommunityDoor("Appearance", FableIcons.Palette, onSelectTheme, Modifier.weight(1f))
                }
            }
        }

        state.syncState.errorMessage?.let { message ->
            item(key = "sync-error") {
                StatusBanner(message, tone = BannerTone.Error, modifier = Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 20.dp))
            }
        }
        item(key = "colophon") {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 48.dp).padding(top = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrnamentDivider()
                Caption("Fable · book data from Open Library")
            }
        }
    }
}

@Composable
private fun Masthead(
    displayName: String,
    profileImageUrl: String?,
    pendingRequestCount: Int,
    onAccount: () -> Unit,
    onRequests: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = FableThemeTokens.current
    val now = remember { Date() }
    val dateLine = remember(now) { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now) }
    val greeting = remember(now) {
        when (Calendar.getInstance().apply { time = now }.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Reading late"
        }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Fable",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Eyebrow(dateLine, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FableIconButton(
                FableIcons.Bell,
                if (pendingRequestCount > 0) "Friend requests, $pendingRequestCount pending" else "Friend requests",
                onRequests,
                badge = pendingRequestCount > 0,
            )
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onAccount),
                contentAlignment = Alignment.Center,
            ) {
                ReaderAvatar(profileImageUrl, displayName, size = 44.dp)
            }
        }
        Text(
            "$greeting,\n${displayName.substringBefore(' ')}.",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(CircleShape)
                .background(tokens.raisedSurface)
                .border(1.dp, tokens.hairline, CircleShape)
                .clickable(role = Role.Button, onClick = onSearch)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(FableIcons.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(
                "Search titles, authors, ISBN…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun JourneyPanel(reading: Int, completed: Int, pages: Int, wantToRead: Int, onClick: () -> Unit) {
    val tokens = FableThemeTokens.current
    FableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            JourneyFigure(reading.toString(), "Reading")
            Box(Modifier.width(1.dp).height(52.dp).background(tokens.hairline))
            JourneyFigure(completed.toString(), "Finished")
            Box(Modifier.width(1.dp).height(52.dp).background(tokens.hairline))
            JourneyFigure(formatPages(pages), "Pages")
            Box(Modifier.width(1.dp).height(52.dp).background(tokens.hairline))
            JourneyFigure(wantToRead.toString(), "To read")
        }
    }
}

@Composable
private fun JourneyFigure(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun formatPages(pages: Int): String = if (pages >= 10_000) "${pages / 1000}k" else "%,d".format(pages)

@Composable
private fun CircleSection(
    activity: List<ReaderActivity>,
    friendCount: Int,
    onActivity: () -> Unit,
    onReaderBook: (String) -> Unit,
) {
    if (activity.isEmpty()) {
        InvitationCard(
            title = if (friendCount == 0) "Reading is better together" else "A quiet moment in your circle",
            message = if (friendCount == 0) "Add friends to see what they start, finish and review."
            else "When your friends add, finish or review books, it will appear here.",
            actionLabel = null,
            onAction = null,
        )
    } else {
        FableCard(onClick = onActivity, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                activity.forEach { item ->
                    ActivityLine(item, onBook = { onReaderBook(item.bookId) })
                }
            }
        }
    }
}

@Composable
private fun CommunityDoor(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FableCard(onClick = onClick, modifier = modifier) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun InvitationCard(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val tokens = FableThemeTokens.current
    FableCard(modifier.fillMaxWidth(), color = tokens.raisedSurface.copy(alpha = 0.7f), elevation = 0.dp) {
        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(tokens.ornament.takeUnless { it == "—" } ?: "✦", style = MaterialTheme.typography.headlineLarge, color = tokens.gilt)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (actionLabel != null && onAction != null) {
                    FableButton(actionLabel, onAction, style = FableButtonStyle.Tonal, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
