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
import com.fable.app.presentation.designsystem.FableButton
import com.fable.app.presentation.designsystem.FableButtonStyle
import com.fable.app.presentation.designsystem.FableIcons

@Composable
fun ForgotPasswordScreen(
    firebaseConfigured: Boolean,
    state: AuthUiState,
    onReset: (String) -> Unit,
    onLogin: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    AuthPage(
        title = "Reset your password",
        subtitle = "Enter your account email and we’ll send a reset link.",
        firebaseConfigured = firebaseConfigured,
        state = state,
        heroLine = "Lost your\nplace?",
    ) {
        AuthTextField(
            email, { email = it }, "Email",
            keyboardType = KeyboardType.Email,
            icon = FableIcons.Mail,
            placeholder = "you@example.com",
            isError = state.errorMentions("email"),
            imeAction = ImeAction.Done,
        )
        AuthSubmitButton("Send reset link", state.isLoading) { onReset(email) }
        FableButton(
            text = "Back to sign in",
            onClick = onLogin,
            style = FableButtonStyle.Ghost,
            icon = FableIcons.Back,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
