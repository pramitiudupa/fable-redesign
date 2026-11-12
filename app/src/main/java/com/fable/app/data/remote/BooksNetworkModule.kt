package com.fable.app.data.remote

import com.fable.app.data.remote.openlibrary.OpenLibraryApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BooksNetworkModule {
    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"

    fun createOpenLibraryService(): OpenLibraryApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "FABLE Android (https://github.com/shashankarya0908/Android)")
                        .build(),
                )
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(OPEN_LIBRARY_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenLibraryApiService::class.java)
    }
}
