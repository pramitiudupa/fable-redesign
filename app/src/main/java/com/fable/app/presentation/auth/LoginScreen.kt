package com.fable.app.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.fable.app.domain.model.FableThemeName
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableChip
import com.fable.app.presentation.designsystem.FableIcons
import com.fable.app.presentation.designsystem.FableTextButton

@Composable
fun LoginScreen(
    firebaseConfigured: Boolean,
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    currentTheme: FableThemeName,
    onThemes: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    AuthPage(
        title = "Welcome back",
        subtitle = "Sign in to return to your shelves, notes and reading friends.",
        firebaseConfigured = firebaseConfigured,
        state = state,
    ) {
        AuthTextField(
            email, { email = it }, "Email",
            keyboardType = KeyboardType.Email,
            icon = FableIcons.Mail,
            placeholder = "you@example.com",
            isError = state.errorMentions("email"),
        )
        PasswordField(password, { password = it }, "Password", isError = state.errorMentions("password"), imeAction = ImeAction.Done)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FableTextButton("Forgot password?", onForgotPassword)
        }
        AuthSubmitButton("Sign in", state.isLoading) { onLogin(email, password) }
        AuthDivider("New to Fable?")
        FableButton(
            text = "Create an account",
            onClick = onRegister,
            style = FableButtonStyle.Secondary,
            icon = FableIcons.UserPlus,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            FableChip(
                label = "Appearance · ${currentTheme.label}",
                selected = false,
                onClick = onThemes,
                icon = FableIcons.Palette,
            )
        }
        // Shows which build is installed, so an outdated APK is easy to spot.
        Caption(
            "Fable ${com.fable.app.BuildConfig.VERSION_NAME}" + if (firebaseConfigured) " · connected" else " · offline build",
            modifier = Modifier.fillMaxWidth(),
            align = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
