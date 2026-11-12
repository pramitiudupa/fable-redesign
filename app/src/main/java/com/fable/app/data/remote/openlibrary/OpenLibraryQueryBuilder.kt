package com.fable.app.data.remote.openlibrary

import com.fable.app.domain.repository.BookSearchType

object OpenLibraryQueryBuilder {
    fun build(query: String, type: BookSearchType): String {
        val normalized = query.trim()
        require(normalized.isNotEmpty()) { "Search query cannot be blank." }
        val field = when (type) {
            BookSearchType.GENERAL -> null
            BookSearchType.TITLE -> "title"
            BookSearchType.AUTHOR -> "author"
            BookSearchType.ISBN -> "isbn"
            BookSearchType.SUBJECT -> "subject"
        }
        return field?.let { "$it:$normalized" } ?: normalized
    }
}
