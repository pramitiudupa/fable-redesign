package com.fable.app.presentation.books.details

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.BookDetailsViewModelFactory
import com.fable.app.di.CategoryBooksViewModelFactory
import com.fable.app.di.FableDependencies
import com.fable.app.di.ReadingViewModelFactory
import com.fable.app.domain.model.Book
import com.fable.app.domain.model.ReadingStatus
import com.fable.app.domain.model.UserBook
import com.fable.app.presentation.books.BookDetailsViewModel
import com.fable.app.presentation.books.CategoryBooksViewModel
import com.fable.app.presentation.books.components.BookCarousel
import com.fable.app.presentation.books.components.BookCarouselSkeleton
import com.fable.app.presentation.books.components.BookCover
import com.fable.app.presentation.books.components.compactCount
import com.fable.app.presentation.books.components.coverGlow
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableCard
import com.fable.app.presentation.designsystem.FableChip
import com.fable.app.presentation.designsystem.FableErrorState
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableTextButton
import com.fable.app.presentation.designsystem.FableTextField
import com.fable.app.presentation.designsystem.GenreBadge
import com.fable.app.presentation.designsystem.OrnamentDivider
import com.fable.app.presentation.designsystem.ProgressRing
import com.fable.app.presentation.designsystem.RatingStars
import com.fable.app.presentation.designsystem.SectionHeader
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.designsystem.bookCoverSharedKey
import com.fable.app.presentation.designsystem.drawCoverMotif
import com.fable.app.presentation.designsystem.drawGenreWash
import com.fable.app.presentation.designsystem.entrance
import com.fable.app.presentation.designsystem.pressScale
import com.fable.app.presentation.designsystem.rememberFableHaptics
import com.fable.app.presentation.designsystem.screenBottomPadding
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.designsystem.shimmer
import com.fable.app.presentation.designsystem.sharedBookCover
import androidx.compose.ui.layout.layout
import com.fable.app.presentation.reading.ReadingViewModel
import com.fable.app.presentation.reading.label
import com.fable.app.presentation.reviews.BookReviewsSection
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableProportions
import com.fable.app.presentation.theme.FableSpacing
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.GenreColors
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.genreColors
import com.fable.app.presentation.theme.rememberGenreMatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookDetailsScreen(
    dependencies: FableDependencies,
    userId: String,
    username: String,
    bookId: String,
    onBookClick: (Book) -> Unit = {},
    onGenreClick: (String) -> Unit = {},
) {
    val factory = BookDetailsViewModelFactory(dependencies, bookId)
    val viewModel: BookDetailsViewModel = viewModel(key = "book-$bookId", factory = factory)
    val readingViewModel: ReadingViewModel = viewModel(
        key = "reading-details-$userId",
        factory = ReadingViewModelFactory(dependencies, userId),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val readingState by readingViewModel.uiState.collectAsStateWithLifecycle()
    when {
        state.isLoading -> DetailsSkeleton(bookId)
        state.book == null -> Box(Modifier.fillMaxSize().padding(top = screenTopPadding()), contentAlignment = Alignment.Center) {
            FableErrorState(state.error ?: "This book could not be found.", viewModel::load)
        }
        else -> {
            val book = state.book!!
            val libraryEntry = readingState.books.firstOrNull { it.bookId == book.id }
            BookDetailsContent(
                dependencies = dependencies,
                userId = userId,
                username = username,
                book = book,
                libraryEntry = libraryEntry,
                readingViewModel = readingViewModel,
                readingMessage = readingState.message,
                readingError = readingState.error,
                pendingChanges = readingState.syncState.pendingChanges,
                syncError = readingState.syncState.errorMessage,
                onBookClick = onBookClick,
                onGenreClick = onGenreClick,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailsContent(
    dependencies: FableDependencies,
    userId: String,
    username: String,
    book: Book,
    libraryEntry: UserBook?,
    readingViewModel: ReadingViewModel,
    readingMessage: String?,
    readingError: String?,
    pendingChanges: Int,
    syncError: String?,
    onBookClick: (Book) -> Unit,
    onGenreClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val match = rememberGenreMatch(book)
    val colors = genreColors(match.primary)
    val tokens = FableThemeTokens.current
    val scroll = rememberScrollState()

    Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
        // ── Hero: the cover as centrepiece inside the genre's atmosphere ──────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawGenreWash(colors, 1.2f)
                    drawCoverMotif(colors.motif, colors.text.copy(alpha = 0.05f), colors.accent.copy(alpha = 0.35f), book.title.hashCode())
                    drawRect(
                        Brush.verticalGradient(listOf(Color.Transparent, colors.background), startY = size.height * 0.7f, endY = size.height),
                    )
                },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = screenTopPadding(4.dp), bottom = 28.dp)
                    .padding(horizontal = FableSpacing.gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val coverWidth = (maxWidth * 0.52f).coerceAtMost(240.dp)
                    Box(
                        Modifier
                            .width(coverWidth)
                            .graphicsLayer { translationY = scroll.value * 0.25f }
                            .coverGlow(colors.glow, 1.8f)
                            .padding(bottom = 14.dp),
                    ) {
                        BookCover(
                            url = book.thumbnailUrl,
                            title = book.title,
                            author = book.authors.firstOrNull(),
                            genre = match.primary,
                            sharedKey = bookCoverSharedKey(book.id),
                            elevation = 26.dp,
                            modifier = Modifier.fillMaxWidth().aspectRatio(FableProportions.HERO_ASPECT),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (match.primary != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.entrance(0)) {
                        GenreBadge(match.primary)
                        match.secondary.forEach { GenreBadge(it, compact = true, modifier = Modifier.align(Alignment.CenterVertically)) }
                    }
                }
                Text(
                    book.title,
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Normal),
                    color = colors.text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.entrance(1).semantics { heading() },
                )
                book.subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = tokens.bookTitleFont, fontStyle = FontStyle.Italic),
                        color = colors.mutedText,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    "by " + book.authors.joinToString().ifBlank { "Author unavailable" },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.mutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.entrance(2),
                )
                if (book.averageRating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.entrance(3)) {
                        RatingStars(book.averageRating, starSize = 16.dp, color = colors.accent, emptyColor = colors.text.copy(alpha = 0.18f))
                        Text(
                            "%.1f".format(book.averageRating) + (book.ratingsCount?.let { " · ${compactCount(it)} ratings" } ?: ""),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.mutedText,
                        )
                    }
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(horizontal = FableSpacing.gutter)
                .padding(bottom = screenBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            FactStrip(book, colors)

            ShelfModule(
                book = book,
                entry = libraryEntry,
                colors = colors,
                onMove = { status -> readingViewModel.addOrMove(book, status) },
                onSaveProgress = { page, notes -> readingViewModel.saveProgress(book.id, page, notes) },
                onRemove = { readingViewModel.removeBook(book.id) },
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusBanner(readingError, tone = BannerTone.Error)
                StatusBanner(syncError, tone = BannerTone.Error)
                StatusBanner(if (pendingChanges > 0) "Saved locally · waiting to sync" else null, tone = BannerTone.Warning)
                StatusBanner(readingMessage, tone = BannerTone.Success, onDismiss = readingViewModel::clearMessage)
            }

            book.description?.takeIf(String::isNotBlank)?.let { description ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("About this book", eyebrow = "Synopsis", accent = colors.primary)
                    ExpandableText(description, colors)
                }
            }

            BookFacts(book, colors)

            if (book.categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Subjects", eyebrow = "Shelved under", accent = colors.primary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        book.categories.forEach { category ->
                            val categoryColors = genreColors(GenreThemes.fromName(category))
                            FableChip(
                                label = category,
                                selected = false,
                                onClick = { onGenreClick(category) },
                                accent = categoryColors.primary,
                                leading = categoryColors.ornament,
                            )
                        }
                    }
                }
            }

            book.previewLink?.let { url ->
                FableButton(
                    text = "View on Open Library",
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                    style = FableButtonStyle.Secondary,
                    icon = FableIcons.External,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OrnamentDivider(ornament = colors.ornament, color = colors.accent, modifier = Modifier.padding(horizontal = 32.dp))

            BookReviewsSection(dependencies, userId, username, book, accent = colors.primary)

            match.primary?.let { genre -> RelatedBooks(dependencies, genre.subjectQuery, genre.label, book.id, onBookClick, colors) }
        }
    }
}

/** Pages · Published · Language · Publisher — a magazine-style facts strip with hairline dividers. */
@Composable
private fun FactStrip(book: Book, colors: GenreColors) {
    val facts = listOfNotNull(
        book.pageCount?.let { it.toString() to "Pages" },
        book.publishedDate?.let { it.take(4) to "Published" },
        book.language?.let { it.uppercase().take(3) to "Language" },
    )
    if (facts.isEmpty()) return
    val tokens = FableThemeTokens.current
    FableCard(Modifier.fillMaxWidth(), color = lerp(tokens.raisedSurface, colors.surface, 0.5f), border = colors.border) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 16.dp)) {
            facts.forEachIndexed { index, (value, label) ->
                if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(colors.border))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * The reader's relationship with this book: three shelves as large tactile choices, the
 * reading-progress editor when it is on the Currently Reading shelf, and removal.
 */
@Composable
private fun ShelfModule(
    book: Book,
    entry: UserBook?,
    colors: GenreColors,
    onMove: (ReadingStatus) -> Unit,
    onSaveProgress: (Int, String?) -> Unit,
    onRemove: () -> Unit,
) {
    val haptics = rememberFableHaptics()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(
            title = if (entry == null) "Add to your library" else "On your shelf",
            eyebrow = "Your library",
            subtitle = entry?.let { "${it.status.label()} · added ${DATE.format(Date(it.addedAt))}" },
            accent = colors.primary,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReadingStatus.entries.forEach { status ->
                ShelfOption(
                    status = status,
                    selected = entry?.status == status,
                    colors = colors,
                    onClick = {
                        if (entry?.status != status) {
                            haptics.confirm()
                            onMove(status)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        AnimatedVisibility(
            visible = entry?.status == ReadingStatus.CURRENTLY_READING,
            enter = fadeIn() + expandVertically(FableMotion.gentleSpring()),
            exit = fadeOut() + shrinkVertically(FableMotion.gentleSpring()),
        ) {
            if (entry != null) ProgressEditor(entry, colors, onSaveProgress)
        }
        AnimatedVisibility(visible = entry != null, enter = fadeIn(), exit = fadeOut()) {
            FableTextButton(
                "Remove from library",
                onClick = {
                    haptics.reject()
                    onRemove()
                },
                color = MaterialTheme.colorScheme.error,
                icon = FableIcons.Trash,
            )
        }
    }
}

@Composable
private fun ShelfOption(
    status: ReadingStatus,
    selected: Boolean,
    colors: GenreColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = FableThemeTokens.current
    val container by animateColorAsState(if (selected) colors.primary else tokens.raisedSurface, FableMotion.snappySpring(), label = "shelfContainer")
    val content by animateColorAsState(if (selected) colors.onPrimary else MaterialTheme.colorScheme.onSurface, label = "shelfContent")
    val border by animateColorAsState(if (selected) colors.primary else tokens.cardBorder.copy(alpha = 0.6f), label = "shelfBorder")
    val interaction = remember { MutableInteractionSource() }
    val icon: ImageVector = when (status) {
        ReadingStatus.WANT_TO_READ -> FableIcons.Bookmark
        ReadingStatus.CURRENTLY_READING -> FableIcons.BookOpen
        ReadingStatus.READ -> FableIcons.Check
    }
    Column(
        modifier
            .heightIn(min = 88.dp)
            .pressScale(interaction, 0.95f)
            .clip(MaterialTheme.shapes.large)
            .background(container)
            .border(1.dp, border, MaterialTheme.shapes.large)
            .clickable(interactionSource = interaction, indication = null, role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        com.fable.app.presentation.designsystem.AnimatedIconSwap(
            checked = selected,
            checkedIcon = FableIcons.Check,
            uncheckedIcon = icon,
            tint = content,
            modifier = Modifier.size(22.dp),
        )
        Text(
            when (status) {
                ReadingStatus.WANT_TO_READ -> "Want to read"
                ReadingStatus.CURRENTLY_READING -> "Reading"
                ReadingStatus.READ -> "Read"
            },
            style = MaterialTheme.typography.labelLarge,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProgressEditor(entry: UserBook, colors: GenreColors, onSave: (Int, String?) -> Unit) {
    var pageText by rememberSaveable(entry.bookId, entry.currentPage) { mutableStateOf(entry.currentPage.toString()) }
    var notes by rememberSaveable(entry.bookId, entry.personalNotes) { mutableStateOf(entry.personalNotes.orEmpty()) }
    val typedPage = pageText.toIntOrNull()
    val total = entry.totalPages
    val preview = when {
        total == null || total <= 0 -> entry.progress.percentage?.div(100f)
        typedPage != null -> (typedPage.toFloat() / total).coerceIn(0f, 1f)
        else -> entry.progress.percentage?.div(100f)
    }
    val tokens = FableThemeTokens.current
    FableCard(Modifier.fillMaxWidth(), color = lerp(tokens.raisedSurface, colors.surface, 0.6f), border = colors.border) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                if (preview != null) {
                    ProgressRing(preview, size = 84.dp, stroke = 6.dp, color = colors.primary, trackColor = colors.primary.copy(alpha = 0.14f)) {
                        Text("${(preview * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow("Reading progress", color = colors.primary)
                    Text(
                        if (total == null) "Page ${entry.currentPage}" else "Page ${entry.currentPage} of $total",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    entry.startedAt?.let {
                        Text("Started ${DATE.format(Date(it))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            FableTextField(
                value = pageText,
                onValueChange = { pageText = it.filter(Char::isDigit) },
                label = total?.let { "Current page (of $it)" } ?: "Current page",
                keyboardType = KeyboardType.Number,
                leadingIcon = FableIcons.Bookmark,
            )
            FableTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Personal notes (optional)",
                placeholder = "A line you loved, a thought to keep…",
                singleLine = false,
                minLines = 2,
                maxLines = 6,
                leadingIcon = FableIcons.Feather,
            )
            FableButton(
                text = "Save progress",
                onClick = { pageText.toIntOrNull()?.let { onSave(it, notes) } },
                enabled = pageText.toIntOrNull() != null,
                icon = FableIcons.Check,
                accent = colors.primary,
                onAccent = colors.onPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ExpandableText(text: String, colors: GenreColors) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val long = text.length > 420
    Column(Modifier.animateContentSize(FableMotion.gentleSpring())) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded || !long) Int.MAX_VALUE else 7,
            overflow = TextOverflow.Ellipsis,
        )
        if (long) {
            FableTextButton(
                if (expanded) "Show less" else "Read more",
                onClick = { expanded = !expanded },
                color = colors.primary,
                icon = FableIcons.ChevronDown,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Bibliographic details as a quiet two-column table. */
@Composable
private fun BookFacts(book: Book, colors: GenreColors) {
    val rows = listOf(
        "Publisher" to book.publisher,
        "Published" to book.publishedDate,
        "Pages" to book.pageCount?.toString(),
        "Language" to book.language,
        "ISBN-10" to book.isbn10,
        "ISBN-13" to book.isbn13,
    ).filter { !it.second.isNullOrBlank() }
    if (rows.isEmpty()) return
    val tokens = FableThemeTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Edition details", eyebrow = "Colophon", accent = colors.primary)
        Column {
            rows.forEachIndexed { index, (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(96.dp).padding(top = 2.dp))
                    Text(value.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                }
                if (index < rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(tokens.hairline))
            }
        }
    }
}

@Composable
private fun RelatedBooks(
    dependencies: FableDependencies,
    subject: String,
    label: String,
    currentBookId: String,
    onBookClick: (Book) -> Unit,
    colors: GenreColors,
) {
    val viewModel: CategoryBooksViewModel = viewModel(key = "related-$subject", factory = CategoryBooksViewModelFactory(dependencies, subject))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val related = remember(state.books, currentBookId) { state.books.filterNot { it.id == currentBookId }.take(12) }
    if (!state.isLoading && related.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
        SectionHeader("More in $label", eyebrow = "Related books", accent = colors.primary)
        // The carousel bleeds to the screen edges like a magazine shelf.
        if (state.isLoading) BookCarouselSkeleton(Modifier.bleed(FableSpacing.gutter))
        else BookCarousel(related, onBookClick, modifier = Modifier.bleed(FableSpacing.gutter))
    }
}

/** Cancels the parent's horizontal gutter so a carousel spans the full screen width. */
private fun Modifier.bleed(gutter: androidx.compose.ui.unit.Dp): Modifier = this.layout { measurable, constraints ->
    val extra = gutter.roundToPx() * 2
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.minWidth + extra,
            maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth + extra else constraints.maxWidth,
        ),
    )
    val width = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
    layout(width, placeable.height) { placeable.place(-gutter.roundToPx(), 0) }
}

@Composable
private fun DetailsSkeleton(bookId: String) {
    Column(
        Modifier.fillMaxSize().padding(top = screenTopPadding()).padding(horizontal = FableSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .width(200.dp)
                .aspectRatio(FableProportions.HERO_ASPECT)
                .sharedBookCover(bookCoverSharedKey(bookId))
                .shimmer(com.fable.app.presentation.books.components.BookShape),
        )
        Box(Modifier.width(220.dp).height(28.dp).shimmer())
        Box(Modifier.width(140.dp).height(16.dp).shimmer())
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { Box(Modifier.weight(1f).height(88.dp).shimmer(MaterialTheme.shapes.large)) }
        }
        Box(Modifier.fillMaxWidth().height(120.dp).shimmer(MaterialTheme.shapes.large))
    }
}

private val DATE = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
