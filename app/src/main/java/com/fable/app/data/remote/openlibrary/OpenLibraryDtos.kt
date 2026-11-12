package com.fable.app.data.remote.openlibrary

import com.fable.app.domain.model.Book
import com.fable.app.domain.model.BookIds
import com.fable.app.domain.model.BookPage
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class OpenLibrarySearchResponseDto(
    @SerializedName("numFound") val numFound: Int? = null,
    @SerializedName("num_found") val numFoundSnakeCase: Int? = null,
    val start: Int? = null,
    val docs: List<OpenLibrarySearchBookDto>? = emptyList(),
)

data class OpenLibrarySearchBookDto(
    val key: String? = null,
    val title: String? = null,
    @SerializedName("author_name") val authorNames: List<String>? = null,
    @SerializedName("first_publish_year") val firstPublishYear: Int? = null,
    @SerializedName("cover_i") val coverId: Long? = null,
    val subject: List<String>? = null,
    val isbn: List<String>? = null,
    val publisher: List<String>? = null,
    val language: List<String>? = null,
    @SerializedName("number_of_pages_median") val pageCount: Int? = null,
    @SerializedName("ratings_average") val ratingsAverage: Double? = null,
    @SerializedName("ratings_count") val ratingsCount: Int? = null,
    @SerializedName("first_sentence") val firstSentence: JsonElement? = null,
)

data class OpenLibraryWorkDto(
    val key: String? = null,
    val title: String? = null,
    val description: JsonElement? = null,
    val covers: List<Long>? = null,
    val subjects: List<String>? = null,
    @SerializedName("first_publish_date") val firstPublishDate: String? = null,
)

fun OpenLibrarySearchBookDto.toDomainOrNull(): Book? {
    val sourceId = key?.toOpenLibrarySourceId() ?: return null
    val cleanTitle = title.cleanOrNull() ?: return null
    val identifiers = isbn.orEmpty().mapNotNull(String::cleanIdentifier)
    return Book(
        id = BookIds.create(OPEN_LIBRARY_SOURCE, sourceId),
        source = OPEN_LIBRARY_SOURCE,
        sourceId = sourceId,
        title = cleanTitle,
        authors = authorNames.orEmpty().mapNotNull(String::cleanOrNull),
        description = firstSentence.toCleanText(),
        thumbnailUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg?default=false" },
        publisher = publisher.orEmpty().firstNotNullOfOrNull(String::cleanOrNull),
        publishedDate = firstPublishYear?.toString(),
        pageCount = pageCount?.takeIf { it > 0 },
        categories = subject.orEmpty().mapNotNull(String::cleanOrNull).distinct().take(8),
        language = language.orEmpty().firstNotNullOfOrNull(String::cleanOrNull),
        isbn10 = identifiers.firstOrNull { it.length == 10 },
        isbn13 = identifiers.firstOrNull { it.length == 13 },
        averageRating = ratingsAverage?.takeIf { it in 0.0..5.0 },
        ratingsCount = ratingsCount?.takeIf { it >= 0 },
        previewLink = "https://openlibrary.org/works/$sourceId",
    )
}

fun OpenLibrarySearchResponseDto.toDomainPage(startIndex: Int, requestedPageSize: Int): BookPage {
    val rawItems = docs.orEmpty()
    val books = rawItems.mapNotNull(OpenLibrarySearchBookDto::toDomainOrNull).distinctBy(Book::id)
    val total = (numFound ?: numFoundSnakeCase ?: 0).coerceAtLeast(0)
    val next = startIndex + rawItems.size
    return BookPage(
        books = books,
        totalItems = total,
        nextStartIndex = next,
        hasMore = rawItems.isNotEmpty() && next < total && requestedPageSize > 0,
    )
}

fun OpenLibraryWorkDto.toDomainOrNull(sourceIdFallback: String): Book? {
    val sourceId = (key?.toOpenLibrarySourceId() ?: sourceIdFallback).cleanOrNull() ?: return null
    val cleanTitle = title.cleanOrNull() ?: return null
    return Book(
        id = BookIds.create(OPEN_LIBRARY_SOURCE, sourceId),
        source = OPEN_LIBRARY_SOURCE,
        sourceId = sourceId,
        title = cleanTitle,
        description = description.toCleanText(),
        thumbnailUrl = covers.orEmpty().firstOrNull()?.let {
            "https://covers.openlibrary.org/b/id/$it-M.jpg?default=false"
        },
        publishedDate = firstPublishDate.cleanOrNull(),
        categories = subjects.orEmpty().mapNotNull(String::cleanOrNull).distinct().take(8),
        previewLink = "https://openlibrary.org/works/$sourceId",
    )
}

private fun String.toOpenLibrarySourceId(): String? = trim()
    .removePrefix("/works/")
    .takeIf { it.isNotEmpty() && it != trim() && it.startsWith("OL") }
    ?: trim().takeIf { it.startsWith("OL") }

private fun String?.cleanOrNull(): String? = this?.trim()?.takeIf(String::isNotEmpty)

private fun String.cleanIdentifier(): String? = filterNot(Char::isWhitespace)
    .filterNot { it == '-' }
    .uppercase()
    .takeIf(String::isNotEmpty)

private fun JsonElement?.toCleanText(): String? = when {
    this == null || isJsonNull -> null
    isJsonPrimitive && asJsonPrimitive.isString -> asString.cleanOrNull()
    isJsonObject -> asJsonObject.get("value")?.toCleanText()
    isJsonArray -> asJsonArray.joinToString(" ") { it.toCleanText().orEmpty() }.cleanOrNull()
    else -> null
}

const val OPEN_LIBRARY_SOURCE = "openlibrary"
