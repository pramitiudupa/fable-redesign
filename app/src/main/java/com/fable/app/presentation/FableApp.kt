package com.fable.app.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fable.app.di.FableDependencies
import com.fable.app.di.FableViewModelFactory
import com.fable.app.di.ThemeSettingsViewModelFactory
import com.fable.app.presentation.auth.AuthenticationViewModel
import com.fable.app.presentation.auth.ForgotPasswordScreen
import com.fable.app.presentation.auth.LoginScreen
import com.fable.app.presentation.auth.ProfileSetupScreen
import com.fable.app.presentation.auth.ProfileViewModel
import com.fable.app.presentation.auth.RegistrationScreen
import com.fable.app.presentation.auth.SessionState
import com.fable.app.presentation.books.navigation.AuthenticatedBooksApp
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.LocalHasBackButton
import com.fable.app.presentation.designsystem.StatusBarScrim
import com.fable.app.presentation.designsystem.FloatingBackButton
import com.fable.app.presentation.designsystem.fableAtmosphere
import com.fable.app.presentation.splash.FableSplash
import com.fable.app.presentation.theme.FableMotion
import com.fable.app.presentation.theme.FableTheme
import com.fable.app.presentation.theme.ThemeSelectionScreen
import com.fable.app.presentation.theme.ThemeSettingsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private enum class AuthDestination { Login, Register, ForgotPassword, ThemeSelection }

/** Which top-level stage of the app is on screen; drives the animated hand-offs between them. */
private enum class Stage { Splash, Auth, ProfileSetup, Ready, Failed }

private const val MIN_SPLASH_MILLIS = 1500L

@Composable
fun FableApp(dependencies: FableDependencies) {
    val factory = FableViewModelFactory(dependencies)
    val authenticationViewModel: AuthenticationViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val themeViewModel: ThemeSettingsViewModel = viewModel(factory = ThemeSettingsViewModelFactory(dependencies))
    val session by authenticationViewModel.sessionState.collectAsStateWithLifecycle()
    val authUi by authenticationViewModel.uiState.collectAsStateWithLifecycle()
    val profileUi by profileViewModel.uiState.collectAsStateWithLifecycle()
    val themeState by themeViewModel.state.collectAsStateWithLifecycle()
    var authDestination by rememberSaveable { mutableStateOf(AuthDestination.Login) }
    // Let the opening animation play once per launch, even when the session restores instantly.
    var splashFinished by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!splashFinished) {
            delay(MIN_SPLASH_MILLIS)
            splashFinished = true
        }
    }

    LaunchedEffect(session, dependencies.firebaseConfigured) {
        when (val current = session) {
            SessionState.Loading, is SessionState.Failed -> Unit
            SessionState.SignedOut -> themeViewModel.restore(ownerId = null, profile = null, cloudAvailable = false)
            is SessionState.ProfileSetup -> themeViewModel.restore(
                ownerId = current.authUser.uid,
                profile = current.profile,
                cloudAvailable = dependencies.firebaseConfigured && current.profile != null,
            )
            is SessionState.Ready -> themeViewModel.restore(
                ownerId = current.userId,
                profile = current.profile,
                cloudAvailable = dependencies.firebaseConfigured,
            )
        }
    }

    val stage = when {
        !splashFinished -> Stage.Splash
        else -> when (session) {
            SessionState.Loading -> Stage.Splash
            SessionState.SignedOut -> Stage.Auth
            is SessionState.ProfileSetup -> Stage.ProfileSetup
            is SessionState.Ready -> Stage.Ready
            is SessionState.Failed -> Stage.Failed
        }
    }

    FableTheme(themeState.selectedTheme) {
        // Default text colour follows the theme, so unstyled text stays readable on dark themes.
        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides androidx.compose.material3.MaterialTheme.colorScheme.onBackground) {
        Box(Modifier.fillMaxSize().fableAtmosphere()) {
            AnimatedContent(
                targetState = stage to session,
                contentKey = { it.first },
                transitionSpec = {
                    (fadeIn(tween(FableMotion.LONG_MS, delayMillis = 90)) + scaleIn(tween(FableMotion.LONG_MS, easing = FableMotion.EmphasizedDecelerate), initialScale = 0.96f)) togetherWith
                        (fadeOut(tween(FableMotion.MEDIUM_MS)) + scaleOut(tween(FableMotion.MEDIUM_MS), targetScale = 1.04f))
                },
                label = "stage",
            ) { (currentStage, currentSession) ->
                when (currentStage) {
                    Stage.Splash -> FableSplash(
                        statusText = if (currentSession == SessionState.Loading) "Checking your account…" else "Opening your library…",
                    )
                    Stage.Auth -> AuthFlow(
                        destination = authDestination,
                        onDestinationChange = { authDestination = it },
                        dependencies = dependencies,
                        authenticationViewModel = authenticationViewModel,
                        authUi = authUi,
                        themeViewModel = themeViewModel,
                        themeState = themeState,
                    )
                    Stage.ProfileSetup -> (currentSession as? SessionState.ProfileSetup)?.let { state ->
                        ProfileSetupScreen(
                            authUser = state.authUser,
                            profile = state.profile,
                            state = profileUi,
                            onSave = { username, displayName, bio, genres, imageUrl ->
                                profileViewModel.saveSetup(state.authUser, state.profile, username, displayName, bio, genres, imageUrl)
                            },
                            onLogout = {
                                authDestination = AuthDestination.Login
                                authenticationViewModel.logout()
                            },
                        )
                    }
                    Stage.Ready -> (currentSession as? SessionState.Ready)?.let { state ->
                        AuthenticatedBooksApp(
                            profile = state.profile,
                            userId = state.userId,
                            dependencies = dependencies,
                            selectedTheme = themeState.selectedTheme,
                            themeMessage = themeState.message,
                            themeError = themeState.error,
                            onSelectTheme = { theme ->
                                themeViewModel.select(state.userId, state.profile, theme, dependencies.firebaseConfigured)
                            },
                            onLogout = {
                                authDestination = AuthDestination.Login
                                authenticationViewModel.logout()
                            },
                        )
                    }
                    Stage.Failed -> SessionErrorScreen(
                        message = (currentSession as? SessionState.Failed)?.message.orEmpty(),
                        onSignOut = authenticationViewModel::logout,
                    )
                }
            }
            // Soft scrim so scrolling content never collides with the status-bar icons. The
            // signed-in app draws its own, because some of its screens are full-bleed heroes.
            if (stage != Stage.Splash && stage != Stage.Ready) {
                val pastel = !com.fable.app.presentation.theme.FableThemeTokens.current.dark && stage != Stage.Failed
                StatusBarScrim(
                    Modifier.align(Alignment.TopCenter),
                    color = if (pastel) com.fable.app.presentation.designsystem.PastelColors.first() else androidx.compose.material3.MaterialTheme.colorScheme.background,
                )
            }
        }
        }
    }
}

@Composable
private fun AuthFlow(
    destination: AuthDestination,
    onDestinationChange: (AuthDestination) -> Unit,
    dependencies: FableDependencies,
    authenticationViewModel: AuthenticationViewModel,
    authUi: com.fable.app.presentation.auth.AuthUiState,
    themeViewModel: ThemeSettingsViewModel,
    themeState: com.fable.app.presentation.theme.ThemeSettingsState,
) {
    AnimatedContent(
        targetState = destination,
        transitionSpec = {
            // Moving "deeper" (register, reset, themes) slides forward; returning to sign in slides back.
            val forward = targetState != AuthDestination.Login
            val enter = slideInHorizontally(FableMotion.offsetSpring) { full -> if (forward) full / 4 else -full / 4 } + fadeIn(tween(FableMotion.MEDIUM_MS))
            val exit = slideOutHorizontally(FableMotion.offsetSpring) { full -> if (forward) -full / 4 else full / 4 } + fadeOut(tween(FableMotion.SHORT_MS))
            enter togetherWith exit
        },
        label = "authFlow",
    ) { current ->
        when (current) {
            AuthDestination.Login -> LoginScreen(
                firebaseConfigured = dependencies.firebaseConfigured,
                state = authUi,
                onLogin = authenticationViewModel::login,
                onForgotPassword = {
                    authenticationViewModel.clearMessage()
                    onDestinationChange(AuthDestination.ForgotPassword)
                },
                onRegister = {
                    authenticationViewModel.clearMessage()
                    onDestinationChange(AuthDestination.Register)
                },
                currentTheme = themeState.selectedTheme,
                onThemes = { onDestinationChange(AuthDestination.ThemeSelection) },
            )
            AuthDestination.Register -> RegistrationScreen(
                firebaseConfigured = dependencies.firebaseConfigured,
                state = authUi,
                onRegister = authenticationViewModel::register,
                onLogin = {
                    authenticationViewModel.clearMessage()
                    onDestinationChange(AuthDestination.Login)
                },
            )
            AuthDestination.ForgotPassword -> ForgotPasswordScreen(
                firebaseConfigured = dependencies.firebaseConfigured,
                state = authUi,
                onReset = authenticationViewModel::requestPasswordReset,
                onLogin = {
                    authenticationViewModel.clearMessage()
                    onDestinationChange(AuthDestination.Login)
                },
            )
            AuthDestination.ThemeSelection -> Box(Modifier.fillMaxSize()) {
                CompositionLocalProvider(LocalHasBackButton provides true) {
                    Box(Modifier.fillMaxSize().padding(top = com.fable.app.presentation.designsystem.screenTopPadding(0.dp))) {
                    ThemeSelectionScreen(
                        selectedTheme = themeState.selectedTheme,
                        statusMessage = themeState.message,
                        errorMessage = themeState.error,
                        onSelect = { theme -> themeViewModel.select(null, null, theme, cloudAvailable = false) },
                        onDone = { onDestinationChange(AuthDestination.Login) },
                    )
                    }
                }
                FloatingBackButton(
                    onBack = { onDestinationChange(AuthDestination.Login) },
                    modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 16.dp, top = 12.dp),
                )
            }
        }
    }
    if (destination != AuthDestination.Login) {
        androidx.activity.compose.BackHandler { onDestinationChange(AuthDestination.Login) }
    }
}

@Composable
private fun SessionErrorScreen(message: String, onSignOut: () -> Unit) {
    Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
        FableEmptyState(
            title = "We couldn’t load your profile",
            message = message,
            icon = FableIcons.Alert,
            actionLabel = "Return to sign in",
            onAction = onSignOut,
        )
    }
}
