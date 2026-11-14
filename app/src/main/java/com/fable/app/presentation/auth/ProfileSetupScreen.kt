package com.fable.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fable.app.domain.model.AuthUser
import com.fable.app.domain.model.UserProfile
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.Eyebrow
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableChip
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableTextField
import com.fable.app.presentation.designsystem.ReaderAvatar
import com.fable.app.presentation.theme.Genre
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.genreColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    authUser: AuthUser,
    profile: UserProfile?,
    state: AuthUiState,
    onSave: (String, String, String, List<String>, String?) -> Unit,
    onLogout: () -> Unit,
    title: String = "Set up your profile",
    showLogout: Boolean = true,
) {
    var username by rememberSaveable(profile?.username) { mutableStateOf(profile?.username.orEmpty()) }
    var displayName by rememberSaveable(profile?.displayName) { mutableStateOf(profile?.displayName.orEmpty()) }
    var bio by rememberSaveable(profile?.bio) { mutableStateOf(profile?.bio.orEmpty()) }
    var profileImageUrl by rememberSaveable(profile?.profileImageUrl) { mutableStateOf(profile?.profileImageUrl.orEmpty()) }
    var selectedGenres by rememberSaveable(profile?.favouriteGenres) {
        mutableStateOf(profile?.favouriteGenres.orEmpty())
    }
    // Offer every genre identity, and keep any genre a reader saved earlier that is not in the list.
    val genreOptions = remember(profile?.favouriteGenres) {
        val labels = Genre.showcase.map(Genre::label)
        labels + profile?.favouriteGenres.orEmpty().filter { it !in labels }
    }

    AuthPage(
        title = title,
        subtitle = "A few details help other readers get to know you.",
        firebaseConfigured = true,
        state = state,
        heroLine = if (profile == null) "Introduce\nyourself." else "Your reader\nportrait.",
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReaderAvatar(
                imageUrl = profileImageUrl.takeIf { it.startsWith("https://", ignoreCase = true) },
                name = displayName.ifBlank { username.ifBlank { authUser.email } },
                size = 72.dp,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    displayName.ifBlank { "Your name" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    if (username.isBlank()) "@username" else "@${username.trim().removePrefix("@").lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AuthTextField(username, { username = it }, "Username", icon = FableIcons.User, isError = state.errorMentions("username"))
        AuthTextField(displayName, { displayName = it }, "Display name", icon = FableIcons.Feather, isError = state.errorMentions("display name"))
        AuthTextField(
            profileImageUrl, { profileImageUrl = it }, "Profile image URL (optional)",
            keyboardType = KeyboardType.Uri,
            icon = FableIcons.Globe,
            placeholder = "https://…",
            isError = state.errorMentions("profile image"),
        )
        FableTextField(
            value = bio,
            onValueChange = { bio = it },
            label = "Short biography",
            placeholder = "What do you love to read, and why?",
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            supportingText = "${bio.length}/280",
            isError = bio.length > 280 || state.errorMentions("biography"),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow("Favourite genres")
            Caption("Choose the shelves you return to most. They colour your reader profile.")
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                genreOptions.forEach { genre ->
                    val colors = genreColors(GenreThemes.fromName(genre))
                    FableChip(
                        label = genre,
                        selected = genre in selectedGenres,
                        onClick = {
                            selectedGenres = if (genre in selectedGenres) selectedGenres - genre else selectedGenres + genre
                        },
                        accent = colors.primary,
                        onAccent = colors.onPrimary,
                        leading = colors.ornament,
                    )
                }
            }
        }
        AuthSubmitButton("Save profile", state.isLoading) {
            onSave(username, displayName, bio, selectedGenres, profileImageUrl)
        }
        if (showLogout) {
            FableButton(
                text = "Sign out",
                onClick = onLogout,
                style = FableButtonStyle.Ghost,
                icon = FableIcons.SignOut,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Caption("Signed in as ${authUser.email}")
    }
}
