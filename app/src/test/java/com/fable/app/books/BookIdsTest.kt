package com.fable.app.books

import com.fable.app.domain.model.BookIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookIdsTest {
    @Test fun createsStandardIdWithNormalizedSource() {
        val id = BookIds.create("open-library", "OL12345W")
        assertEquals("open-library:OL12345W", id)
    }

    @Test fun createsIdWithCaseAndWhitespaceNormalization() {
        val id = BookIds.create("  Open-Library  ", "  OL9999M  ")
        assertEquals("open-library:OL9999M", id)
    }

    @Test fun rejectsBlankSourceOrSourceId() {
        assertTrue(runCatching { BookIds.create("", "OL123W") }.isFailure)
        assertTrue(runCatching { BookIds.create("   ", "OL123W") }.isFailure)
        assertTrue(runCatching { BookIds.create("open-library", "") }.isFailure)
        assertTrue(runCatching { BookIds.create("open-library", "   ") }.isFailure)
    }

    @Test fun parsesValidCompositeId() {
        val parsed = BookIds.parse("open-library:OL12345W")
        assertEquals("open-library" to "OL12345W", parsed)
    }

    @Test fun parsesIdWithColonsInSourceId() {
        val parsed = BookIds.parse("isbn:978-0-123456-47-2")
        assertEquals("isbn" to "978-0-123456-47-2", parsed)
    }

    @Test fun returnsNullForInvalidIdFormats() {
        assertNull(BookIds.parse("invalid-id-no-colon"))
        assertNull(BookIds.parse(":missing-source"))
        assertNull(BookIds.parse("missing-source-id:"))
        assertNull(BookIds.parse(""))
    }
}
