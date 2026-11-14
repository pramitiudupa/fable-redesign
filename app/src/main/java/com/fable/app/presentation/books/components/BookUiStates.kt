package com.fable.app.presentation.books.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fable.app.presentation.designsystem.FableEmptyState
import com.fable.app.presentation.designsystem.FableErrorState
import com.fable.app.presentation.designsystem.FableLoading

/*
 * Book-flow state components. They delegate to the shared design-system states so loading,
 * empty and error moments look identical everywhere in Fable.
 */

@Composable
fun LoadingIndicator(message: String, modifier: Modifier = Modifier) = FableLoading(message, modifier)

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) =
    FableEmptyState(title = title, message = message, modifier = modifier)

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) =
    FableErrorState(message = message, onRetry = onRetry, modifier = modifier)
