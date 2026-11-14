package com.fable.app.books

import com.fable.app.presentation.books.details.publicationYear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PublicationYearTest {
    @Test fun extractsTheYearFromCommonOpenLibraryFormats() {
        assertEquals("1937", publicationYear("1937"))
        assertEquals("1937", publicationYear("Sep 21, 1937"))
        assertEquals("1937", publicationYear("January 1937"))
        assertEquals("2008", publicationYear("2008-10-14"))
    }

    @Test fun returnsNullWhenThereIsNoYear() {
        assertNull(publicationYear("January"))
        assertNull(publicationYear("n.d."))
    }
}
