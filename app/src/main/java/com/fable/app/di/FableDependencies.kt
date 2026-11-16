package com.fable.app.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fable.app.data.repository.FirebaseAuthenticationRepository
import com.fable.app.data.repository.FirestoreUserRepository
import com.fable.app.data.repository.OpenLibraryBookRepository
import com.fable.app.data.repository.FirestoreReadingRepository
import com.fable.app.data.repository.FirestoreReviewRepository
import com.fable.app.data.repository.FirestoreSocialRepository
import com.fable.app.data.local.LocalThemePreferenceRepository
import com.fable.app.data.local.FableDatabase
import com.fable.app.data.remote.BooksNetworkModule
import com.fable.app.domain.model.UserProfile
import com.fable.app.domain.model.PublicReaderProfile
import com.fable.app.domain.repository.AuthenticationRepository
import com.fable.app.domain.repository.UserRepository
import com.fable.app.domain.repository.BookRepository
import com.fable.app.domain.repository.ReadingRepository
import com.fable.app.domain.repository.ReviewRepository
import com.fable.app.domain.repository.SocialRepository
import com.fable.app.domain.repository.ThemePreferenceRepository
import com.fable.app.presentation.auth.AuthenticationViewModel
import com.fable.app.presentation.auth.ProfileViewModel
import com.fable.app.presentation.social.SocialViewModel
import com.fable.app.presentation.theme.ThemeSettingsViewModel
import com.fable.app.presentation.books.BookSearchViewModel
import com.fable.app.presentation.books.DiscoverViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.room.Room

class FirebaseConfigurationRequiredException :
    IllegalStateException("Firebase configuration is missing. Add app/google-services.json and rebuild the app.")

class FableDependencies(context: Context) {
    val firebaseConfigured: Boolean = runCatching {
        if (FirebaseApp.getApps(context.applicationContext).isEmpty()) {
            FirebaseApp.initializeApp(context.applicationContext) != null
        } else {
            true
        }
    }.getOrDefault(false)

    val authenticationRepository: AuthenticationRepository
    val userRepository: UserRepository
    val bookRepository: BookRepository
    val readingRepository: ReadingRepository
    val reviewRepository: ReviewRepository
    val socialRepository: SocialRepository
    val themePreferenceRepository: ThemePreferenceRepository = LocalThemePreferenceRepository(context.applicationContext)

    init {
        val database = Room.databaseBuilder(context.applicationContext, FableDatabase::class.java, "fable.db")
            .addMigrations(FableDatabase.MIGRATION_1_2, FableDatabase.MIGRATION_2_3)
            .build()
        bookRepository = OpenLibraryBookRepository(
            BooksNetworkModule.createOpenLibraryService(),
            database.bookDao(),
        )
        if (firebaseConfigured) {
            authenticationRepository = FirebaseAuthenticationRepository(FirebaseAuth.getInstance())
            userRepository = FirestoreUserRepository(FirebaseFirestore.getInstance())
            readingRepository = FirestoreReadingRepository(FirebaseFirestore.getInstance(), database.bookDao())
            reviewRepository = FirestoreReviewRepository(FirebaseFirestore.getInstance(), database.bookDao())
            socialRepository = FirestoreSocialRepository(FirebaseFirestore.getInstance())
        } else {
            authenticationRepository = UnconfiguredAuthenticationRepository()
            userRepository = UnconfiguredUserRepository()
            readingRepository = UnconfiguredReadingRepository()
            reviewRepository = UnconfiguredReviewRepository()
            socialRepository = UnconfiguredSocialRepository()
        }
    }
}

class SocialViewModelFactory(
    private val dependencies: FableDependencies,
    private val userId: String,
    private val profile: UserProfile,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SocialViewModel(
        userId,
        PublicReaderProfile(
            uid = profile.uid,
            username = profile.username,
            displayName = profile.displayName,
            bio = profile.bio,
            profileImageUrl = profile.profileImageUrl,
            favouriteGenres = profile.favouriteGenres,
        ),
        dependencies.socialRepository,
    ) as T
}

class ThemeSettingsViewModelFactory(private val dependencies: FableDependencies) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ThemeSettingsViewModel(
        dependencies.userRepository,
        dependencies.themePreferenceRepository,
    ) as T
}

class FableViewModelFactory(private val dependencies: FableDependencies) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthenticationViewModel::class.java) ->
            AuthenticationViewModel(dependencies.authenticationRepository, dependencies.userRepository) as T
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(dependencies.userRepository) as T
        modelClass.isAssignableFrom(BookSearchViewModel::class.java) ->
            BookSearchViewModel(dependencies.bookRepository) as T
        modelClass.isAssignableFrom(DiscoverViewModel::class.java) ->
            DiscoverViewModel(dependencies.bookRepository) as T
        else -> error("Unknown ViewModel class: ${modelClass.name}")
    }
}

class CategoryBooksViewModelFactory(
    private val dependencies: FableDependencies,
    private val category: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        com.fable.app.presentation.books.CategoryBooksViewModel(dependencies.bookRepository, category) as T
}

class BookDetailsViewModelFactory(
    private val dependencies: FableDependencies,
    private val bookId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        com.fable.app.presentation.books.BookDetailsViewModel(dependencies.bookRepository, bookId) as T
}

class ReadingViewModelFactory(
    private val dependencies: FableDependencies,
    private val userId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        com.fable.app.presentation.reading.ReadingViewModel(userId, dependencies.readingRepository) as T
}

class ReviewViewModelFactory(
    private val dependencies: FableDependencies,
    private val userId: String,
    private val username: String,
    private val bookId: String? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        com.fable.app.presentation.reviews.ReviewViewModel(userId, username, dependencies.reviewRepository, bookId) as T
}
