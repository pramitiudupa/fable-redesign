package com.fable.app.theme

import com.fable.app.presentation.theme.Genre
import com.fable.app.presentation.theme.GenreMatch
import com.fable.app.presentation.theme.GenreThemes
import com.fable.app.presentation.theme.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreThemeTest {
    @Test fun everyRequestedGenreHasAnIdentityAndIsShowcased() {
        assertEquals(24, Genre.entries.size)
        assertEquals(Genre.entries.toSet(), Genre.showcase.toSet())
        assertEquals(Genre.entries.size, Genre.entries.map { it.subjectQuery }.distinct().size)
    }

    @Test fun subjectsResolveToTheExpectedPrimaryGenre() {
        assertEquals(Genre.FANTASY, GenreThemes.resolve(listOf("Fiction", "Fantasy fiction", "Wizards")).primary)
        assertEquals(Genre.SCIENCE_FICTION, GenreThemes.resolve(listOf("Science fiction", "Space warfare")).primary)
        assertEquals(Genre.MYSTERY, GenreThemes.resolve(listOf("Detective and mystery stories")).primary)
        assertEquals(Genre.DARK_ROMANCE, GenreThemes.resolve(listOf("Dark romance")).primary)
        assertEquals(Genre.BIOGRAPHY, GenreThemes.resolve(listOf("Biography & Autobiography")).primary)
        // Fictional diaries for young readers are children's fiction, not memoir or literary fiction.
        assertEquals(
            Genre.CHILDRENS,
            GenreThemes.resolve(listOf("Diaries", "Juvenile fiction", "Families", "Middle schools", "Humorous stories")).primary,
        )
    }

    @Test fun booksWithSeveralGenresKeepSecondaryIndicators() {
        val match = GenreThemes.resolve(listOf("Fantasy", "Magic", "Romance", "Love stories"))
        assertEquals(Genre.FANTASY, match.primary)
        assertTrue(Genre.ROMANCE in match.secondary)
        assertTrue(match.secondary.size <= 2)
    }

    @Test fun unknownOrEmptySubjectsFallBackGracefully() {
        assertEquals(GenreMatch.Unknown, GenreThemes.resolve(emptyList()))
        assertNull(GenreThemes.resolve(listOf("Accessible book", "Protected DAISY")).primary)
        assertNull(GenreThemes.fromName("   "))
    }

    @Test fun discoverRouteArgumentsMapBackToTheirGenre() {
        Genre.entries.forEach { genre ->
            assertEquals(genre, GenreThemes.fromName(genre.subjectQuery))
            assertEquals(genre, GenreThemes.fromName(genre.label))
        }
    }

    @Test fun genreTextMeetsAccessibleContrastInLightAndDarkThemes() {
        listOf(false, true).forEach { dark ->
            Genre.entries.forEach { genre ->
                val colors = GenreThemes.colors(genre, dark)
                assertTrue("$genre text (dark=$dark)", contrastRatio(colors.text, colors.background) >= 7f)
                assertTrue("$genre muted text (dark=$dark)", contrastRatio(colors.mutedText, colors.surface) >= 4.5f)
                assertTrue("$genre badge (dark=$dark)", contrastRatio(colors.badgeContent, colors.badgeContainer) >= 4.5f)
                assertTrue("$genre on-primary (dark=$dark)", contrastRatio(colors.onPrimary, colors.primary) >= 4.5f)
            }
        }
    }
}
