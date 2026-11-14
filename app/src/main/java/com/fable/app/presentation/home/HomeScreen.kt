package com.fable.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fable.app.domain.model.UserProfile
import com.fable.app.presentation.auth.AuthUiState
import com.fable.app.presentation.designsystem.BannerTone
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.GenreBadge
import com.fable.app.presentation.designsystem.PageTitle
import com.fable.app.presentation.designsystem.PullQuote
import com.fable.app.presentation.designsystem.ReaderAvatar
import com.fable.app.presentation.designsystem.StatusBanner
import com.fable.app.presentation.theme.GenreThemes

/** Simple signed-in summary screen (kept for compatibility with earlier phases). */
@Composable
fun AuthenticatedHomeScreen(
    profile: UserProfile,
    state: AuthUiState,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ReaderAvatar(profile.profileImageUrl, profile.displayName, size = 88.dp)
        PageTitle("Welcome, ${profile.displayName}", eyebrow = "Fable", subtitle = "@${profile.username}")
        profile.bio?.takeIf { it.isNotBlank() }?.let { PullQuote(it) }
        if (profile.favouriteGenres.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                profile.favouriteGenres.forEach { GenreBadge(GenreThemes.fromName(it), label = it) }
            }
        }
        Caption("Signed in as ${profile.email}")
        StatusBanner(state.error, tone = BannerTone.Error)
        FableButton(
            text = if (state.isLoading) "Signing out…" else "Sign out",
            onClick = onLogout,
            enabled = !state.isLoading,
            style = FableButtonStyle.Secondary,
            icon = FableIcons.SignOut,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
