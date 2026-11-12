package com.fable.app.data.remote.openlibrary

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApiService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("fields") fields: String = OPEN_LIBRARY_SEARCH_FIELDS,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): OpenLibrarySearchResponseDto

    @GET("works/{workId}.json")
    suspend fun getWork(@Path("workId") workId: String): OpenLibraryWorkDto
}

const val OPEN_LIBRARY_SEARCH_FIELDS =
    "key,title,author_name,first_publish_year,cover_i,subject,isbn,publisher,language," +
        "number_of_pages_median,ratings_average,ratings_count,first_sentence"
