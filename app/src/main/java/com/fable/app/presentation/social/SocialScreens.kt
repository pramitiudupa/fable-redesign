package com.fable.app.presentation.social

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fable.app.domain.model.FriendRequest
import com.fable.app.domain.model.FriendRequestStatus
import com.fable.app.domain.model.PublicReaderProfile
import com.fable.app.domain.model.ReaderActivity
import com.fable.app.domain.model.ReaderActivityType
import com.fable.app.presentation.books.components.BookCover
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.BookRowSkeleton
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableCard
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableLoading
import com.fable.app.presentation.designsystem.FableTextButton
import com.fable.app.presentation.designsystem.FableTextField
import com.fable.app.presentation.designsystem.GenreBadge
import com.fable.app.presentation.designsystem.ListRow
import com.fable.app.presentation.designsystem.OrnamentDivider
import com.fable.app.presentation.designsystem.PageTitle
import com.fable.app.presentation.designsystem.ProgressBar
import com.fable.app.presentation.designsystem.PullQuote
import com.fable.app.presentation.designsystem.RatingStars
import com.fable.app.presentation.designsystem.ReaderAvatar
import com.fable.app.presentation.designsystem.SectionHeader
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.drawCoverMotif
import com.fable.app.presentation.designsystem.drawGenreWash
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.rememberFableHaptics
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.genreColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ───────────────────────────── Discover readers ─────────────────────────────

@Composable
fun DiscoverReadersScreen(
    viewModel: SocialViewModel,
    onReaderClick: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searching = state.query.trim().removePrefix("@").length >= 2
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = FableSpacing.gutter, end = FableSpacing.gutter, top = screenTopPadding(), bottom = screenBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") {
            PageTitle(
                "Discover Readers",
                eyebrow = "Find your people",
                subtitle = "Search public reader profiles by username.",
                modifier = Modifier.entrance(0),
            )
        }
        item(key = "search") {
            FableTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                label = "Username",
                placeholder = "Start typing a username",
                leadingIcon = FableIcons.Search,
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
            )
        }
        item(key = "banners") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBanner(state.message, tone = BannerTone.Success, onDismiss = viewModel::clearMessage)
                StatusBanner(state.error, tone = BannerTone.Error)
            }
        }
        when {
            !searching -> item(key = "hint") {
                FableEmptyState(
                    title = "Who’s reading what?",
                    message = "Enter at least two characters to search for readers.",
                    icon = FableIcons.Users,
                )
            }
            state.searchResults.isEmpty() && state.error == null -> item(key = "none") {
                FableEmptyState("No readers found", "No reader uses a username like “${state.query.trim()}”.", icon = FableIcons.Search)
            }
            else -> itemsIndexed(state.searchResults, key = { _, profile -> profile.uid }) { index, profile ->
                ReaderCard(profile, onClick = { onReaderClick(profile.uid) }, modifier = Modifier.entrance(index, key = profile.uid))
            }
        }
    }
}

// ───────────────────────────── Reader profile ─────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReaderProfileScreen(
    viewModel: SocialViewModel,
    userId: String,
    onEditProfile: () -> Unit,
    onSelectTheme: () -> Unit,
    onOpenRequests: () -> Unit,
    onSignOut: (() -> Unit)? = null,
    onOpenFriends: (() -> Unit)? = null,
    onOpenReviews: (() -> Unit)? = null,
    onOpenStatistics: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.selectedProfile
    val haptics = rememberFableHaptics()
    when {
        state.isLoading && profile == null -> Box(Modifier.fillMaxSize().padding(top = screenTopPadding()), contentAlignment = Alignment.TopCenter) {
            FableLoading("Opening reader profile…")
        }
        profile == null -> Box(Modifier.fillMaxSize().padding(top = screenTopPadding()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.padding(horizontal = FableSpacing.gutter)) {
                StatusBanner(state.error, tone = BannerTone.Error)
                if (state.error == null) {
                    FableEmptyState("Profile unavailable", "This reader profile is unavailable.", icon = FableIcons.User)
                }
            }
        }
        else -> {
            val reader = profile
            val signatureGenre = remember(reader.favouriteGenres) { reader.favouriteGenres.firstNotNullOfOrNull(GenreThemes::fromName) }
            val colors = genreColors(signatureGenre)
            val tokens = FableThemeTokens.current
            val isMe = reader.uid == userId
            val pageBackground = MaterialTheme.colorScheme.background
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = screenBottomPadding())) {
                item(key = "hero") {
                    Box(Modifier.fillMaxWidth()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(screenTopPadding(0.dp) + 150.dp)
                                .drawBehind {
                                    drawGenreWash(colors, 1.2f)
                                    drawCoverMotif(colors.motif, colors.text.copy(alpha = 0.07f), colors.accent.copy(alpha = 0.4f), reader.uid.hashCode())
                                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, pageBackground)))
                                },
                        )
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = screenTopPadding(0.dp) + 96.dp)
                                .padding(horizontal = FableSpacing.gutter),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            ReaderAvatar(reader.profileImageUrl, reader.displayName, size = 104.dp, accent = colors.primary)
                            Text(reader.displayName, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                            Text("@${reader.username}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                reader.bio?.takeIf(String::isNotBlank)?.let { bio ->
                    item(key = "bio") { PullQuote(bio, Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 16.dp), accent = colors.accent) }
                }
                if (reader.favouriteGenres.isNotEmpty()) {
                    item(key = "genres") {
                        Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Eyebrow("Favourite genres", color = colors.primary)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                reader.favouriteGenres.forEach { name ->
                                    GenreBadge(GenreThemes.fromName(name), label = name)
                                }
                            }
                        }
                    }
                }
                item(key = "stats") {
                    val stats = reader.readingStats
                    FableCard(Modifier.fillMaxWidth().padding(horizontal = FableSpacing.gutter).padding(top = 24.dp)) {
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 18.dp)) {
                            listOf(
                                stats.completedBooks.toString() to "Finished",
                                "%,d".format(stats.pagesRead) to "Pages",
                                stats.currentlyReading.toString() to "Reading",
                                stats.wantToRead.toString() to "To read",
                            ).forEachIndexed { index, (value, label) ->
                                if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(tokens.hairline))
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                item(key = "achievements") {
                    val earned = remember(reader.readingStats) { achievementsFor(reader.readingStats.completedBooks, reader.readingStats.pagesRead) }
                    if (earned.isNotEmpty()) {
                        Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Eyebrow("Reading achievements", color = colors.primary)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                earned.forEach { AchievementPill(it) }
                            }
                        }
                    }
                }
                item(key = "banners") {
                    Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBanner(state.message, tone = BannerTone.Success, onDismiss = viewModel::clearMessage)
                        StatusBanner(state.error, tone = BannerTone.Error)
                    }
                }
                if (isMe) {
                    item(key = "own") {
                        Column(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FableButton("Edit profile", onEditProfile, icon = FableIcons.Edit, modifier = Modifier.weight(1f))
                                FableButton("Appearance", onSelectTheme, style = FableButtonStyle.Secondary, icon = FableIcons.Palette, modifier = Modifier.weight(1f))
                            }
                            FableCard(Modifier.fillMaxWidth(), elevation = 0.dp) {
                                Column(Modifier.padding(6.dp)) {
                                    onOpenStatistics?.let { ListRow("Reading statistics", it, icon = FableIcons.Chart) }
                                    onOpenReviews?.let { ListRow("My reviews", it, icon = FableIcons.StarOutline) }
                                    onOpenFriends?.let { ListRow("Friends", it, subtitle = "${state.friends.size} ${if (state.friends.size == 1) "friend" else "friends"}", icon = FableIcons.Users) }
                                    ListRow("Friend requests", onOpenRequests, icon = FableIcons.Bell)
                                    onSignOut?.let { ListRow("Sign out", it, icon = FableIcons.SignOut, destructive = true) }
                                }
                            }
                        }
                    }
                } else {
                    item(key = "relationship") {
                        val friend = state.friends.firstOrNull { it.uid == reader.uid }
                        val sentRequest = state.outgoingRequests.firstOrNull { it.recipientId == reader.uid && it.status == FriendRequestStatus.PENDING }
                        val receivedRequest = state.incomingRequests.firstOrNull { it.senderId == reader.uid && it.status == FriendRequestStatus.PENDING }
                        Box(Modifier.padding(horizontal = FableSpacing.gutter).padding(top = 12.dp)) {
                            AnimatedContent(
                                targetState = when {
                                    friend != null -> 0
                                    sentRequest != null -> 1
                                    receivedRequest != null -> 2
                                    else -> 3
                                },
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "relationship",
                            ) { mode ->
                                when (mode) {
                                    0 -> FableButton("Friends · Remove", { friend?.let { haptics.reject(); viewModel.removeFriend(it) } }, style = FableButtonStyle.Secondary, icon = FableIcons.Check, modifier = Modifier.fillMaxWidth())
                                    1 -> FableButton("Request sent · Cancel", { sentRequest?.let(viewModel::cancel) }, style = FableButtonStyle.Secondary, icon = FableIcons.Clock, modifier = Modifier.fillMaxWidth())
                                    2 -> FableButton("Respond to friend request", onOpenRequests, style = FableButtonStyle.Tonal, icon = FableIcons.Bell, modifier = Modifier.fillMaxWidth())
                                    else -> FableButton(
                                        "Send friend request",
                                        { haptics.confirm(); viewModel.sendRequest(reader) },
                                        icon = FableIcons.UserPlus,
                                        accent = colors.primary,
                                        onAccent = colors.onPrimary,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun achievementsFor(completed: Int, pages: Int): List<String> = buildList {
    listOf(1 to "First finish", 5 to "5 books", 10 to "10 books", 25 to "25 books", 50 to "50 books", 100 to "Centurion")
        .filter { completed >= it.first }.forEach { add(it.second) }
    listOf(1_000 to "1k pages", 5_000 to "5k pages", 10_000 to "10k pages", 25_000 to "25k pages")
        .filter { pages >= it.first }.forEach { add(it.second) }
}

@Composable
private fun AchievementPill(label: String) {
    val tokens = FableThemeTokens.current
    Row(
        Modifier
            .clip(CircleShape)
            .background(tokens.gilt.copy(alpha = 0.14f))
            .border(1.dp, tokens.gilt.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(FableIcons.Trophy, null, tint = tokens.gilt, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ───────────────────────────── Friend requests ─────────────────────────────

@Composable
fun FriendRequestsScreen(viewModel: SocialViewModel, onReaderClick: (String) -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val incoming = state.incomingRequests.filter { it.status == FriendRequestStatus.PENDING }
    val outgoing = state.outgoingRequests.filter { it.status == FriendRequestStatus.PENDING }
    val haptics = rememberFableHaptics()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = FableSpacing.gutter, end = FableSpacing.gutter, top = screenTopPadding(), bottom = screenBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") { PageTitle("Friend Requests", eyebrow = "Invitations", modifier = Modifier.entrance(0)) }
        item(key = "banners") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBanner(state.message, tone = BannerTone.Success, onDismiss = viewModel::clearMessage)
                StatusBanner(state.error, tone = BannerTone.Error)
            }
        }
        item(key = "incoming-header") {
            SectionHeader("Incoming", eyebrow = "${incoming.size} waiting", modifier = Modifier.padding(top = 16.dp))
        }
        if (state.isLoading && incoming.isEmpty()) item(key = "incoming-loading") { BookRowSkeleton() }
        else if (incoming.isEmpty()) item(key = "incoming-empty") { Caption("No incoming requests.") }
        items(incoming, key = { "in-${it.senderId}" }) { request ->
            RequestCard(
                request,
                incoming = true,
                onOpen = { onReaderClick(request.senderId) },
                onAccept = { haptics.confirm(); viewModel.accept(request) },
                onReject = { viewModel.reject(request) },
            )
        }
        item(key = "outgoing-header") {
            SectionHeader("Outgoing", eyebrow = "${outgoing.size} sent", modifier = Modifier.padding(top = 24.dp))
        }
        if (outgoing.isEmpty()) item(key = "outgoing-empty") { Caption("No outgoing requests.") }
        items(outgoing, key = { "out-${it.recipientId}" }) { request ->
            RequestCard(request, incoming = false, onOpen = { onReaderClick(request.recipientId) }, onCancel = { viewModel.cancel(request) })
        }
    }
}

// ───────────────────────────── Friends ─────────────────────────────

@Composable
fun FriendsScreen(
    viewModel: SocialViewModel,
    onReaderClick: (String) -> Unit,
    onDiscoverReaders: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingRemoval by remember { mutableStateOf<PublicReaderProfile?>(null) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = FableSpacing.gutter, end = FableSpacing.gutter, top = screenTopPadding(), bottom = screenBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") {
            PageTitle(
                "Friends",
                eyebrow = "Your reading circle",
                subtitle = if (state.friends.isEmpty()) null else "${state.friends.size} ${if (state.friends.size == 1) "reader" else "readers"}",
                modifier = Modifier.entrance(0),
            )
        }
        item(key = "banners") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBanner(state.message, tone = BannerTone.Success, onDismiss = viewModel::clearMessage)
                StatusBanner(state.error, tone = BannerTone.Error)
            }
        }
        if (state.isLoading && state.friends.isEmpty()) {
            items(3) { BookRowSkeleton() }
        } else if (state.friends.isEmpty()) {
            item(key = "empty") {
                FableEmptyState(
                    title = "Your circle is waiting",
                    message = "Your friends will appear here after a request is accepted.",
                    icon = FableIcons.Users,
                    actionLabel = onDiscoverReaders?.let { "Find readers" },
                    onAction = onDiscoverReaders,
                )
            }
        }
        itemsIndexed(state.friends, key = { _, friend -> friend.uid }) { index, friend ->
            ReaderCard(
                friend,
                onClick = { onReaderClick(friend.uid) },
                modifier = Modifier.entrance(index, key = friend.uid),
                action = {
                    FableTextButton("Remove", { pendingRemoval = friend }, color = MaterialTheme.colorScheme.error)
                },
            )
        }
    }
    pendingRemoval?.let { friend ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            containerColor = FableThemeTokens.current.raisedSurface,
            shape = MaterialTheme.shapes.extraLarge,
            icon = { ReaderAvatar(friend.profileImageUrl, friend.displayName, size = 56.dp) },
            title = { Text("Remove ${friend.displayName}?", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center) },
            text = { Text("You’ll stop seeing @${friend.username}’s reading activity. You can send a new request later.", textAlign = TextAlign.Center) },
            confirmButton = {
                FableButton("Remove", {
                    viewModel.removeFriend(friend)
                    pendingRemoval = null
                }, accent = MaterialTheme.colorScheme.error, onAccent = MaterialTheme.colorScheme.onError)
            },
            dismissButton = { FableTextButton("Keep", { pendingRemoval = null }) },
        )
    }
}

// ───────────────────────────── Activity feed ─────────────────────────────

@Composable
fun ActivityFeedScreen(
    viewModel: SocialViewModel,
    onReaderClick: (String) -> Unit,
    onBookClick: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val grouped = remember(state.activityFeed) { state.activityFeed.groupBy { dayLabel(it.timestamp) }.toList() }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = FableSpacing.gutter, end = FableSpacing.gutter, top = screenTopPadding(), bottom = screenBottomPadding()),
    ) {
        item(key = "title") {
            PageTitle(
                "Activity",
                eyebrow = "From your reading circle",
                subtitle = "Recent reading activity from your accepted friends.",
                modifier = Modifier.padding(bottom = 16.dp).entrance(0),
            )
        }
        state.error?.let { item(key = "error") { StatusBanner(it, tone = BannerTone.Error) } }
        if (state.isLoading && state.activityFeed.isEmpty()) {
            items(3) { BookRowSkeleton(Modifier.padding(vertical = 10.dp)) }
        } else if (state.activityFeed.isEmpty() && state.error == null) {
            item(key = "empty") {
                FableEmptyState(
                    title = "A quiet reading room",
                    message = "Your friends’ activity will appear here.",
                    icon = FableIcons.Activity,
                )
            }
        }
        grouped.forEach { (day, items) ->
            item(key = "day-$day") {
                Row(Modifier.padding(top = 18.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(day, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            itemsIndexed(items, key = { _, activity -> activity.id }) { index, activity ->
                ActivityCard(
                    activity,
                    isLast = index == items.lastIndex,
                    onReader = { onReaderClick(activity.readerId) },
                    onBook = { onBookClick(activity.bookId) },
                )
            }
        }
        if (state.activityFeed.isNotEmpty()) item(key = "end") { OrnamentDivider(Modifier.padding(horizontal = 40.dp, vertical = 20.dp)) }
    }
}

/** A single feed entry on a vertical thread, written as a sentence rather than a log line. */
@Composable
private fun ActivityCard(activity: ReaderActivity, isLast: Boolean, onReader: () -> Unit, onBook: () -> Unit) {
    val tokens = FableThemeTokens.current
    val accent = activity.eventType.accent()
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.width(44.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.clickable(role = Role.Button, onClick = onReader)) {
                ReaderAvatar(null, activity.displayName, size = 44.dp, accent = accent)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .border(2.dp, tokens.raisedSurface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(activity.eventType.icon(), null, tint = Color.White, modifier = Modifier.size(11.dp)) }
            }
            if (!isLast) Box(Modifier.padding(top = 6.dp).width(1.5.dp).weight(1f).background(tokens.hairline.copy(alpha = 0.2f)))
        }
        FableCard(Modifier.weight(1f).padding(bottom = 16.dp), elevation = 0.dp) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivitySentence(activity, onReader)
                Row(
                    Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(role = Role.Button, onClick = onBook),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BookCover(
                        activity.bookThumbnailUrl,
                        activity.bookTitle,
                        Modifier.width(46.dp).aspectRatio(FableProportions.COVER_ASPECT),
                        elevation = 4.dp,
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            activity.bookTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = tokens.bookTitleFont),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        activity.progressPercent?.takeIf { activity.eventType == ReaderActivityType.PROGRESS_UPDATED }?.let {
                            ProgressBar(it / 100f, color = accent, height = 4.dp)
                        }
                        val detail = listOfNotNull(
                            activity.currentPage?.let { "Page $it" },
                            activity.progressPercent?.let { "$it%" },
                        ).joinToString(" · ")
                        if (detail.isNotEmpty()) Caption(detail)
                        activity.rating?.let { RatingStars(it.coerceIn(0, 5).toDouble(), starSize = 12.dp) }
                    }
                }
                activity.reviewText?.takeIf(String::isNotBlank)?.let { PullQuote(it, accent = accent, maxLines = 5) }
                Caption(TIME_FORMAT.format(Date(activity.timestamp)))
            }
        }
    }
}

@Composable
private fun ActivitySentence(activity: ReaderActivity, onReader: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = scheme.onSurface)) { append(activity.displayName) }
            withStyle(SpanStyle(color = scheme.onSurfaceVariant)) { append(" @${activity.username} · ") }
            withStyle(SpanStyle(color = scheme.onSurface)) { append(activity.eventType.displayLabel().replaceFirstChar { it.lowercase() }) }
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.clickable(role = Role.Button, onClick = onReader),
    )
}

/** Compact one-line activity preview used on Home. */
@Composable
fun ActivityLine(activity: ReaderActivity, onBook: () -> Unit) {
    val accent = activity.eventType.accent()
    val tokens = FableThemeTokens.current
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(role = Role.Button, onClick = onBook),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReaderAvatar(null, activity.displayName, size = 36.dp, accent = accent, ring = false)
        Column(Modifier.weight(1f)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(activity.displayName.substringBefore(' ')) }
                    append(" " + activity.eventType.displayLabel().replaceFirstChar { it.lowercase() })
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                activity.bookTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Italic),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BookCover(activity.bookThumbnailUrl, activity.bookTitle, Modifier.width(34.dp).aspectRatio(FableProportions.COVER_ASPECT), elevation = 3.dp)
    }
}

// ───────────────────────────── Shared pieces ─────────────────────────────

@Composable
private fun ReaderCard(
    profile: PublicReaderProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val genre = remember(profile.favouriteGenres) { profile.favouriteGenres.firstNotNullOfOrNull(GenreThemes::fromName) }
    val colors = genreColors(genre)
    FableCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ReaderAvatar(profile.profileImageUrl, profile.displayName, size = 56.dp, accent = colors.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(profile.displayName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("@${profile.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${profile.readingStats.completedBooks} finished · ${profile.readingStats.currentlyReading} reading",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                )
                if (genre != null) GenreBadge(genre, compact = true, modifier = Modifier.padding(top = 2.dp))
            }
            action?.invoke() ?: Icon(FableIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RequestCard(
    request: FriendRequest,
    incoming: Boolean,
    onOpen: () -> Unit,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val username = if (incoming) request.senderUsername else request.recipientUsername
    val name = if (incoming) request.senderDisplayName else request.recipientDisplayName
    val image = if (incoming) request.senderImageUrl else request.recipientImageUrl
    FableCard(Modifier.fillMaxWidth(), onClick = onOpen) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ReaderAvatar(image, name, size = 50.dp)
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("@$username", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Caption(if (incoming) "Wants to be reading friends · ${SHORT_DATE.format(Date(request.createdAt))}" else "Sent ${SHORT_DATE.format(Date(request.createdAt))}")
                }
            }
            if (incoming) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FableButton("Accept", onAccept, icon = FableIcons.Check, modifier = Modifier.weight(1f))
                    FableButton("Decline", onReject, style = FableButtonStyle.Secondary, modifier = Modifier.weight(1f))
                }
            } else {
                FableButton("Cancel request", onCancel, style = FableButtonStyle.Secondary, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ReaderActivityType.accent(): Color {
    val tokens = FableThemeTokens.current
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        ReaderActivityType.FINISHED_BOOK -> tokens.gilt
        ReaderActivityType.REVIEW_PUBLISHED, ReaderActivityType.REVIEW_UPDATED -> scheme.tertiary
        ReaderActivityType.STARTED_READING, ReaderActivityType.PROGRESS_UPDATED -> scheme.primary
        ReaderActivityType.ADDED_TO_LIBRARY -> scheme.secondary
    }
}

private fun ReaderActivityType.icon(): ImageVector = when (this) {
    ReaderActivityType.ADDED_TO_LIBRARY -> FableIcons.Plus
    ReaderActivityType.STARTED_READING -> FableIcons.BookOpen
    ReaderActivityType.PROGRESS_UPDATED -> FableIcons.Bookmark
    ReaderActivityType.FINISHED_BOOK -> FableIcons.Flag
    ReaderActivityType.REVIEW_PUBLISHED, ReaderActivityType.REVIEW_UPDATED -> FableIcons.Star
}

private fun ReaderActivityType.displayLabel(): String = when (this) {
    ReaderActivityType.ADDED_TO_LIBRARY -> "Added a book to their library"
    ReaderActivityType.STARTED_READING -> "Started reading"
    ReaderActivityType.PROGRESS_UPDATED -> "Updated reading progress"
    ReaderActivityType.FINISHED_BOOK -> "Finished a book"
    ReaderActivityType.REVIEW_PUBLISHED -> "Published a review"
    ReaderActivityType.REVIEW_UPDATED -> "Updated a review"
}

private fun dayLabel(timestamp: Long): String {
    val day = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    val sameDay = { a: Calendar, b: Calendar -> a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR) }
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        sameDay(day, today) -> "Today"
        sameDay(day, yesterday) -> "Yesterday"
        else -> DAY_FORMAT.format(Date(timestamp))
    }
}

private val TIME_FORMAT = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
private val SHORT_DATE = SimpleDateFormat("MMM d", Locale.getDefault())
private val DAY_FORMAT = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
