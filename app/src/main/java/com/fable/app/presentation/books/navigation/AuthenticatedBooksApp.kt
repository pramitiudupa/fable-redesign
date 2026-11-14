package com.fable.app.presentation.books.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fable.app.di.FableDependencies
import com.fable.app.di.FableViewModelFactory
import com.fable.app.di.SocialViewModelFactory
import com.fable.app.domain.model.AuthUser
import com.fable.app.domain.model.Book
import com.fable.app.domain.model.FableThemeName
import com.fable.app.domain.model.FriendRequestStatus
import com.fable.app.domain.model.UserProfile
import com.fable.app.presentation.auth.ProfileSetupScreen
import com.fable.app.presentation.books.details.BookDetailsScreen
import com.fable.app.presentation.books.discovery.DiscoverScreen
import com.fable.app.presentation.books.discovery.GenreScreen
import com.fable.app.presentation.books.search.BookSearchScreen
import com.fable.app.presentation.designsystem.BottomDestination
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.FableBottomBar
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FloatingBackButton
import com.fable.app.presentation.designsystem.ListRow
import com.fable.app.presentation.designsystem.LocalHasBackButton
import com.fable.app.presentation.designsystem.LocalNavAnimatedVisibilityScope
import com.fable.app.presentation.designsystem.LocalSharedTransitionScope
import com.fable.app.presentation.designsystem.OrnamentDivider
import com.fable.app.presentation.designsystem.ReaderAvatar
import com.fable.app.presentation.designsystem.StatusBarScrim
import com.fable.app.presentation.designsystem.GenreArtworkViewModel
import com.fable.app.presentation.designsystem.LocalGenreArtwork
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fable.app.presentation.designsystem.screenTopPadding
import com.fable.app.presentation.reading.MyLibraryScreen
import com.fable.app.presentation.reading.ReadingHomeScreen
import com.fable.app.presentation.reading.ReadingStatisticsScreen
import com.fable.app.presentation.reviews.MyReviewsScreen
import com.fable.app.presentation.social.ActivityFeedScreen
import com.fable.app.presentation.social.DiscoverReadersScreen
import com.fable.app.presentation.social.FriendRequestsScreen
import com.fable.app.presentation.social.FriendsScreen
import com.fable.app.presentation.social.ReaderProfileScreen
import com.fable.app.presentation.social.SocialViewModel
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableThemeTokens
import com.fable.app.presentation.theme.ThemeSelectionScreen
import kotlinx.coroutines.launch

private val primaryDestinations = listOf("home", "discover", "search", "library")
private val immersiveRoutes = listOf("genre/", "book/", "reader/")

private val bottomDestinations = listOf(
    BottomDestination("home", "Home", FableIcons.Home),
    BottomDestination("discover", "Discover", FableIcons.Compass),
    BottomDestination("search", "Search", FableIcons.Search),
    BottomDestination("library", "Library", FableIcons.Library),
)

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedBooksApp(
    profile: UserProfile,
    userId: String,
    dependencies: FableDependencies,
    selectedTheme: FableThemeName,
    themeMessage: String?,
    themeError: String?,
    onSelectTheme: (FableThemeName) -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val socialViewModel: SocialViewModel = viewModel(
        key = "social-$userId-${profile.updatedAt}-${profile.username}",
        factory = SocialViewModelFactory(dependencies, userId, profile),
    )
    val socialState by socialViewModel.uiState.collectAsStateWithLifecycle()
    // One cover cache for every genre tile in the signed-in app.
    val genreArtwork: GenreArtworkViewModel = viewModel(
        key = "genre-artwork",
        factory = viewModelFactory { initializer { GenreArtworkViewModel(dependencies.bookRepository) } },
    )
    val pendingRequests = socialState.incomingRequests.count { it.status == FriendRequestStatus.PENDING }
    val navEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navEntry?.destination?.route
    val onPrimary = currentRoute == null || currentRoute in primaryDestinations
    var accountSheetOpen by rememberSaveable { mutableStateOf(false) }

    val openBookById: (String) -> Unit = { bookId -> navController.navigate("book/${Uri.encode(bookId)}") }

    Box(Modifier.fillMaxSize()) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this, LocalGenreArtwork provides genreArtwork) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { enterFor(this) },
                    exitTransition = { exitFor(this) },
                    popEnterTransition = { popEnterFor(this) },
                    popExitTransition = { popExitFor(this) },
                ) {
                    fableComposable("home") {
                        ReadingHomeScreen(
                            dependencies = dependencies,
                            userId = userId,
                            displayName = profile.displayName,
                            onDiscover = { navController.navigateTab("discover") },
                            onSearch = { navController.navigateTab("search") },
                            onLibrary = { navController.navigateTab("library") },
                            onStatistics = { navController.navigate("reading-statistics") },
                            onMyReviews = { navController.navigate("my-reviews") },
                            onDiscoverReaders = { navController.navigate("discover-readers") },
                            onFriends = { navController.navigate("friends") },
                            onFriendRequests = { navController.navigate("friend-requests") },
                            onActivityFeed = { navController.navigate("activity-feed") },
                            onMyProfile = { openReaderProfile(navController, socialViewModel, userId) },
                            onSelectTheme = { navController.navigate("themes") },
                            onBookClick = openBookById,
                            profileImageUrl = profile.profileImageUrl,
                            favouriteGenres = profile.favouriteGenres,
                            socialViewModel = socialViewModel,
                            pendingRequestCount = pendingRequests,
                            onGenreClick = { genre -> navController.navigate("genre/${Uri.encode(genre)}") },
                            onOpenAccount = { accountSheetOpen = true },
                        )
                    }
                    fableComposable("discover") {
                        DiscoverScreen(
                            dependencies = dependencies,
                            onBookClick = { book -> navController.openBook(book) },
                            onGenreClick = { genre -> navController.navigate("genre/${Uri.encode(genre)}") },
                            onSearchClick = { navController.navigateTab("search") },
                        )
                    }
                    fableComposable("search") {
                        BookSearchScreen(dependencies) { book -> navController.openBook(book) }
                    }
                    fableComposable(
                        route = "genre/{category}",
                        arguments = listOf(navArgument("category") { type = NavType.StringType }),
                    ) { entry ->
                        GenreScreen(
                            dependencies,
                            entry.arguments?.getString("category").orEmpty(),
                            onBookClick = { book -> navController.openBook(book) },
                        )
                    }
                    fableComposable("library") {
                        MyLibraryScreen(
                            dependencies = dependencies,
                            userId = userId,
                            onBookClick = openBookById,
                            onProgressClick = { navController.navigate("reading-statistics") },
                        )
                    }
                    fableComposable("reading-statistics") {
                        ReadingStatisticsScreen(dependencies, userId, onBookClick = openBookById)
                    }
                    fableComposable("my-reviews") {
                        MyReviewsScreen(dependencies, userId, profile.username) { bookId -> openBookById(bookId) }
                    }
                    fableComposable("discover-readers") {
                        DiscoverReadersScreen(socialViewModel) { readerId -> openReaderProfile(navController, socialViewModel, readerId) }
                    }
                    fableComposable("friend-requests") {
                        FriendRequestsScreen(socialViewModel, onReaderClick = { readerId -> openReaderProfile(navController, socialViewModel, readerId) })
                    }
                    fableComposable("friends") {
                        FriendsScreen(
                            socialViewModel,
                            onReaderClick = { readerId -> openReaderProfile(navController, socialViewModel, readerId) },
                            onDiscoverReaders = { navController.navigate("discover-readers") },
                        )
                    }
                    fableComposable("activity-feed") {
                        ActivityFeedScreen(
                            socialViewModel,
                            onReaderClick = { readerId -> openReaderProfile(navController, socialViewModel, readerId) },
                            onBookClick = openBookById,
                        )
                    }
                    fableComposable("themes") {
                        // The original theme screen is kept exactly as designed; only inset it below the bars.
                        Box(Modifier.fillMaxSize().padding(top = screenTopPadding(0.dp))) {
                        ThemeSelectionScreen(
                            selectedTheme = selectedTheme,
                            statusMessage = themeMessage,
                            errorMessage = themeError,
                            onSelect = onSelectTheme,
                            onDone = { navController.popBackStack() },
                        )
                        }
                    }
                    fableComposable(
                        route = "reader/{readerId}",
                        arguments = listOf(navArgument("readerId") { type = NavType.StringType }),
                    ) { entry ->
                        val readerId = entry.arguments?.getString("readerId").orEmpty()
                        LaunchedEffect(readerId) { socialViewModel.openProfile(readerId) }
                        ReaderProfileScreen(
                            viewModel = socialViewModel,
                            userId = userId,
                            onEditProfile = { navController.navigate("edit-profile") },
                            onSelectTheme = { navController.navigate("themes") },
                            onOpenRequests = { navController.navigate("friend-requests") },
                            onSignOut = onLogout,
                            onOpenFriends = { navController.navigate("friends") },
                            onOpenReviews = { navController.navigate("my-reviews") },
                            onOpenStatistics = { navController.navigate("reading-statistics") },
                        )
                    }
                    fableComposable("edit-profile") {
                        val profileViewModel: com.fable.app.presentation.auth.ProfileViewModel = viewModel(
                            key = "edit-profile-$userId",
                            factory = FableViewModelFactory(dependencies),
                        )
                        val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
                        ProfileSetupScreen(
                            authUser = AuthUser(profile.uid, profile.email),
                            profile = profile,
                            state = profileState,
                            onSave = { username, displayName, bio, genres, imageUrl ->
                                profileViewModel.saveSetup(AuthUser(profile.uid, profile.email), profile, username, displayName, bio, genres, imageUrl)
                            },
                            onLogout = {},
                            title = "Edit reader profile",
                            showLogout = false,
                        )
                    }
                    fableComposable(
                        route = "book/{bookId}",
                        arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                    ) { entry ->
                        BookDetailsScreen(
                            dependencies,
                            userId,
                            profile.username,
                            entry.arguments?.getString("bookId").orEmpty(),
                            onBookClick = { book -> navController.openBook(book) },
                            onGenreClick = { genre -> navController.navigate("genre/${Uri.encode(genre)}") },
                        )
                    }
                }
            }
        }

        // Full-bleed hero screens paint their own colour behind the status bar.
        val immersive = currentRoute?.let { route -> immersiveRoutes.any { route.startsWith(it) } } == true
        AnimatedVisibility(
            visible = !immersive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) { StatusBarScrim() }

        // Floating back button for every non-tab destination.
        AnimatedVisibility(
            visible = !onPrimary,
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.6f),
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 16.dp, top = 12.dp),
        ) {
            FloatingBackButton(onBack = { navController.popBackStack() })
        }

        // Floating bottom navigation for the four primary tabs.
        AnimatedVisibility(
            visible = onPrimary,
            enter = slideInVertically(FableMotion.offsetSpring) { it } + fadeIn(),
            exit = slideOutVertically(FableMotion.offsetSpring) { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FableBottomBar(
                destinations = bottomDestinations,
                currentRoute = currentRoute ?: "home",
                onNavigate = { route -> navController.navigateTab(route) },
            )
        }
    }

    if (accountSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        fun closeThen(action: () -> Unit) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                accountSheetOpen = false
                action()
            }
        }
        ModalBottomSheet(
            onDismissRequest = { accountSheetOpen = false },
            sheetState = sheetState,
            containerColor = FableThemeTokens.current.raisedSurface,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ReaderAvatar(profile.profileImageUrl, profile.displayName, size = 56.dp)
                    Column(Modifier.weight(1f)) {
                        Text(profile.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        Caption("@${profile.username} · ${profile.email}")
                    }
                }
                OrnamentDivider(Modifier.padding(vertical = 8.dp, horizontal = 24.dp))
                ListRow("My reader profile", { closeThen { openReaderProfile(navController, socialViewModel, userId) } }, subtitle = "How other readers see you", icon = FableIcons.User)
                ListRow("Reading statistics", { closeThen { navController.navigate("reading-statistics") } }, subtitle = "Pages, completions and history", icon = FableIcons.Chart)
                ListRow("My reviews", { closeThen { navController.navigate("my-reviews") } }, subtitle = "Ratings and reviews you’ve written", icon = FableIcons.StarOutline)
                ListRow(
                    "Friend requests",
                    { closeThen { navController.navigate("friend-requests") } },
                    subtitle = if (pendingRequests > 0) "$pendingRequests waiting for you" else "Incoming and outgoing",
                    icon = FableIcons.Bell,
                )
                ListRow("Appearance", { closeThen { navController.navigate("themes") } }, subtitle = selectedTheme.label, icon = FableIcons.Palette)
                ListRow("Sign out", { closeThen(onLogout) }, icon = FableIcons.SignOut, destructive = true)
            }
        }
    }
}

/** A NavHost destination that exposes its animation scope for shared elements and declares chrome. */
private fun NavGraphBuilder.fableComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(route = route, arguments = arguments) { entry ->
        CompositionLocalProvider(
            LocalNavAnimatedVisibilityScope provides this,
            LocalHasBackButton provides (route !in primaryDestinations),
        ) {
            content(entry)
        }
    }
}

private fun isTabSwitch(scope: AnimatedContentTransitionScope<NavBackStackEntry>): Boolean =
    scope.initialState.destination.route in primaryDestinations && scope.targetState.destination.route in primaryDestinations

private fun enterFor(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition =
    if (isTabSwitch(scope)) fadeIn(tween(FableMotion.MEDIUM_MS, delayMillis = 60)) + scaleIn(tween(FableMotion.MEDIUM_MS), initialScale = 0.985f)
    else slideInHorizontally(FableMotion.offsetSpring) { it / 4 } + fadeIn(tween(FableMotion.MEDIUM_MS))

private fun exitFor(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition =
    if (isTabSwitch(scope)) fadeOut(tween(FableMotion.SHORT_MS))
    else slideOutHorizontally(FableMotion.offsetSpring) { -it / 8 } + fadeOut(tween(FableMotion.MEDIUM_MS))

private fun popEnterFor(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition =
    if (isTabSwitch(scope)) fadeIn(tween(FableMotion.MEDIUM_MS))
    else slideInHorizontally(FableMotion.offsetSpring) { -it / 8 } + fadeIn(tween(FableMotion.MEDIUM_MS))

private fun popExitFor(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition =
    if (isTabSwitch(scope)) fadeOut(tween(FableMotion.SHORT_MS))
    else slideOutHorizontally(FableMotion.offsetSpring) { it / 4 } + fadeOut(tween(FableMotion.SHORT_MS))

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun openReaderProfile(
    navController: NavHostController,
    viewModel: SocialViewModel,
    readerId: String,
) {
    viewModel.openProfile(readerId)
    navController.navigate("reader/${Uri.encode(readerId)}")
}

private fun NavHostController.openBook(book: Book) {
    navigate("book/${Uri.encode(book.id)}")
}
