package com.fable.app.presentation.designsystem

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fable.app.domain.repository.BookRepository
import com.fable.app.presentation.theme.Genre
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Real book-cover artwork for genre tiles. For each genre it asks the existing
 * [BookRepository] for a few books in that genre's subject and keeps their cover URLs.
 * Genres are only fetched when a tile for them is actually shown, once per session, and
 * at most a few at a time. If a request fails the tile keeps its colour-only design.
 */
class GenreArtworkViewModel(private val repository: BookRepository) : ViewModel() {
    private val _covers = MutableStateFlow<Map<Genre, List<String>>>(emptyMap())
    val covers: StateFlow<Map<Genre, List<String>>> = _covers.asStateFlow()
    private val requested = mutableSetOf<Genre>()
    private val gate = Semaphore(MAX_PARALLEL)

    fun request(genre: Genre) {
        if (!requested.add(genre)) return
        viewModelScope.launch {
            gate.withPermit {
                try {
                    val candidates = repository.discoverByCategory(genre.subjectQuery, startIndex = 0, pageSize = PAGE_SIZE)
                        .books
                        .mapNotNull { it.thumbnailUrl?.takeIf(String::isNotBlank) }
                        .distinct()
                    _covers.update { current ->
                        // Books often sit in several genres; prefer covers no other tile is showing
                        // so neighbouring tiles (e.g. Mystery and Thriller) don't look identical.
                        val shown = current.values.flatten().toHashSet()
                        val (fresh, repeated) = candidates.partition { it !in shown }
                        current + (genre to (fresh + repeated).take(COVERS_PER_GENRE))
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // Allow a later retry; the tile stays colour-only meanwhile.
                    requested.remove(genre)
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 12
        const val COVERS_PER_GENRE = 3
        const val MAX_PARALLEL = 3
    }
}

/** Provided by the signed-in navigation host so every genre tile shares one cache. */
val LocalGenreArtwork = compositionLocalOf<GenreArtworkViewModel?> { null }
