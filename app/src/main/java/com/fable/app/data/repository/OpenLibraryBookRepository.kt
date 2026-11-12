package com.fable.app.data.repository

import com.fable.app.data.local.BookCacheEntity
import com.fable.app.data.local.BookDao
import com.fable.app.data.local.SearchHistoryEntity
import com.fable.app.data.local.toCacheEntity
import com.fable.app.data.local.toDomain
import com.fable.app.data.remote.openlibrary.OPEN_LIBRARY_SOURCE
import com.fable.app.data.remote.openlibrary.OpenLibraryApiService
import com.fable.app.data.remote.openlibrary.OpenLibraryQueryBuilder
import com.fable.app.data.remote.openlibrary.toDomainOrNull
import com.fable.app.data.remote.openlibrary.toDomainPage
import com.fable.app.domain.model.Book
import com.fable.app.domain.model.BookIds
import com.fable.app.domain.model.BookPage
import com.fable.app.domain.model.SearchHistoryEntry
import com.fable.app.domain.repository.BookRepository
import com.fable.app.domain.repository.BookSearchType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class OpenLibraryBookRepository(
    private val api: OpenLibraryApiService,
    private val bookDao: BookDao,
) : BookRepository {
    override suspend fun searchBooks(
        query: String,
        type: BookSearchType,
        startIndex: Int,
        pageSize: Int,
    ): BookPage {
        val safeStart = startIndex.coerceAtLeast(0)
        val safePageSize = pageSize.coerceIn(1, MAX_PAGE_SIZE)
        val apiQuery = OpenLibraryQueryBuilder.build(query, type)
        return try {
            api.searchBooks(apiQuery, limit = safePageSize, offset = safeStart)
                .toDomainPage(safeStart, safePageSize)
                .also { page -> bookDao.cacheBooks(page.books.map(Book::toCacheEntity)) }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val cached = runCatching { bookDao.searchCachedBooks(query.trim(), safePageSize) }
                .getOrDefault(emptyList())
                .map(BookCacheEntity::toDomain)
            if (cached.isNotEmpty()) {
                BookPage(cached.distinctBy(Book::id), cached.size, safeStart, false)
            } else {
                throw error.toBookRepositoryException()
            }
        }
    }

    override suspend fun discoverByCategory(category: String, startIndex: Int, pageSize: Int): BookPage =
        searchBooks(category, BookSearchType.SUBJECT, startIndex, pageSize)

    override suspend fun getBook(id: String): Book? {
        val (source, sourceId) = BookIds.parse(id) ?: return null
        val cached = runCatching { bookDao.getCachedBook(id)?.toDomain() }.getOrNull()

        // Existing shelves can contain Google Books IDs. Preserve their saved metadata locally.
        if (source != OPEN_LIBRARY_SOURCE) return cached

        return try {
            val remote = api.getWork(sourceId).toDomainOrNull(sourceId)
            if (remote == null) cached else {
                val merged = remote.mergeMissingFieldsFrom(cached)
                bookDao.cacheBooks(listOf(merged.toCacheEntity()))
                merged
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            cached ?: throw error.toBookRepositoryException()
        }
    }

    override fun observeSearchHistory(): Flow<List<SearchHistoryEntry>> =
        bookDao.observeSearchHistory(BookDao.SEARCH_HISTORY_LIMIT).map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordSearch(query: String) {
        val cleanQuery = query.trim().takeIf(String::isNotEmpty) ?: return
        bookDao.saveSearch(
            SearchHistoryEntity(
                normalizedQuery = cleanQuery.lowercase(),
                query = cleanQuery,
                searchedAtMillis = System.currentTimeMillis(),
            ),
        )
        bookDao.pruneSearchHistory(BookDao.SEARCH_HISTORY_LIMIT)
    }

    override suspend fun clearSearchHistory() = bookDao.clearSearchHistory()

    private fun Book.mergeMissingFieldsFrom(cached: Book?): Book {
        if (cached == null) return this
        return copy(
            subtitle = subtitle ?: cached.subtitle,
            authors = authors.ifEmpty { cached.authors },
            description = description ?: cached.description,
            thumbnailUrl = thumbnailUrl ?: cached.thumbnailUrl,
            publisher = publisher ?: cached.publisher,
            publishedDate = publishedDate ?: cached.publishedDate,
            pageCount = pageCount ?: cached.pageCount,
            categories = categories.ifEmpty { cached.categories },
            language = language ?: cached.language,
            isbn10 = isbn10 ?: cached.isbn10,
            isbn13 = isbn13 ?: cached.isbn13,
            averageRating = averageRating ?: cached.averageRating,
            ratingsCount = ratingsCount ?: cached.ratingsCount,
        )
    }

    private fun Throwable.toBookRepositoryException(): BookRepositoryException = when (this) {
        is BookRepositoryException -> this
        is IOException -> BookRepositoryException("Network connection failed. Check your connection and try again.", this)
        is HttpException -> when {
            code() == 403 || code() == 429 -> BookRepositoryException(
                "Open Library is limiting requests right now. Try again in a little while.", this,
            )
            code() == 404 -> BookRepositoryException("This book could not be found in Open Library.", this)
            code() >= 500 -> BookRepositoryException("Open Library is temporarily unavailable. Try again shortly.", this)
            else -> BookRepositoryException("Open Library request failed (HTTP ${code()}).", this)
        }
        else -> BookRepositoryException("We could not load books. Please try again.", this)
    }

    companion object {
        const val MAX_PAGE_SIZE = 40
    }
}
