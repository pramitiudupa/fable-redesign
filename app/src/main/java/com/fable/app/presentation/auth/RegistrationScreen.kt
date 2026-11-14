package com.fable.app.presentation.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.fable.app.presentation.designsystem.Caption
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableIcons

@Composable
fun RegistrationScreen(
    firebaseConfigured: Boolean,
    state: AuthUiState,
    onRegister: (String, String, String, String, String) -> Unit,
    onLogin: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    AuthPage(
        title = "Create your account",
        subtitle = "Start building a home for the books you love.",
        firebaseConfigured = firebaseConfigured,
        state = state,
        heroLine = "Begin a new\nchapter.",
    ) {
        AuthTextField(
            username, { username = it }, "Username",
            icon = FableIcons.User,
            placeholder = "e.g. moonlit_reader",
            isError = state.errorMentions("username"),
        )
        AuthTextField(
            displayName, { displayName = it }, "Display name",
            icon = FableIcons.Feather,
            placeholder = "How other readers will see you",
            isError = state.errorMentions("display name"),
        )
        AuthTextField(
            email, { email = it }, "Email",
            keyboardType = KeyboardType.Email,
            icon = FableIcons.Mail,
            placeholder = "you@example.com",
            isError = state.errorMentions("email"),
        )
        PasswordField(password, { password = it }, "Password", isError = state.errorMentions("password"))
        PasswordField(
            confirmPassword, { confirmPassword = it }, "Confirm password",
            isError = state.errorMentions("do not match"),
            imeAction = ImeAction.Done,
        )
        Caption("Use at least 8 characters. Usernames use letters, numbers, and underscores.")
        AuthSubmitButton("Create account", state.isLoading) {
            onRegister(username, displayName, email, password, confirmPassword)
        }
        FableButton(
            text = "Already have an account? Sign in",
            onClick = onLogin,
            style = FableButtonStyle.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
