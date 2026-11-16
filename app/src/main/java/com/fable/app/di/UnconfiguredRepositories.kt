package com.fable.app.di

import com.fable.app.domain.model.AuthUser
import com.fable.app.domain.model.FriendRequest
import com.fable.app.domain.model.PublicReaderProfile
import com.fable.app.domain.model.ReaderActivity
import com.fable.app.domain.model.UserProfile
import com.fable.app.domain.repository.AuthenticationRepository
import com.fable.app.domain.repository.ReadingRepository
import com.fable.app.domain.repository.ReviewRepository
import com.fable.app.domain.repository.SocialRepository
import com.fable.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Stand-ins used only when app/google-services.json is missing. They return empty data and
// show a "Firebase setup required" error instead of crashing, so the app still opens.

internal class UnconfiguredAuthenticationRepository : AuthenticationRepository {
    override fun observeCurrentUser(): Flow<AuthUser?> = flowOf(null)
    override suspend fun register(email: String, password: String): AuthUser = throw FirebaseConfigurationRequiredException()
    override suspend fun login(email: String, password: String): AuthUser = throw FirebaseConfigurationRequiredException()
    override suspend fun sendPasswordReset(email: String) = throw FirebaseConfigurationRequiredException()
    override suspend fun logout() = Unit
    override suspend fun deleteCurrentUser() = Unit
}

internal class UnconfiguredUserRepository : UserRepository {
    override fun observeProfile(uid: String): Flow<UserProfile?> = flowOf(null)
    override suspend fun createProfile(profile: UserProfile) = throw FirebaseConfigurationRequiredException()
    override suspend fun updateProfile(profile: UserProfile, previousUsername: String) =
        throw FirebaseConfigurationRequiredException()
}

internal class UnconfiguredReadingRepository : ReadingRepository {
    override fun observeLibrary(userId: String): Flow<List<com.fable.app.domain.model.UserBook>> = flowOf(emptyList())
    override fun observeStatistics(userId: String): Flow<com.fable.app.domain.model.ReadingStatistics> =
        flowOf(com.fable.app.domain.model.calculateReadingStatistics(emptyList()))
    override fun observeSyncState(userId: String): Flow<com.fable.app.domain.model.ReadingSyncState> =
        flowOf(com.fable.app.domain.model.ReadingSyncState(errorMessage = "Firebase is not configured."))
    override suspend fun addOrMoveBook(userId: String, book: com.fable.app.domain.model.Book, status: com.fable.app.domain.model.ReadingStatus): com.fable.app.domain.model.UserBook =
        throw FirebaseConfigurationRequiredException()
    override suspend fun removeBook(userId: String, bookId: String) = throw FirebaseConfigurationRequiredException()
    override suspend fun updateProgress(userId: String, bookId: String, currentPage: Int, personalNotes: String?): com.fable.app.domain.model.UserBook =
        throw FirebaseConfigurationRequiredException()
    override suspend fun synchronizePending(userId: String) = throw FirebaseConfigurationRequiredException()
}

internal class UnconfiguredReviewRepository : ReviewRepository {
    override fun observeBookReviews(bookId: String): Flow<List<com.fable.app.domain.model.BookReview>> = flowOf(emptyList())
    override fun observeMyReviews(userId: String): Flow<List<com.fable.app.domain.model.BookReview>> = flowOf(emptyList())
    override fun observePendingChanges(userId: String): Flow<Int> = flowOf(0)
    override suspend fun saveReview(
        userId: String,
        username: String,
        book: com.fable.app.domain.model.Book,
        rating: Int,
        reviewText: String?,
    ): com.fable.app.domain.model.BookReview = throw FirebaseConfigurationRequiredException()
    override suspend fun deleteReview(userId: String, bookId: String) = throw FirebaseConfigurationRequiredException()
    override suspend fun synchronizePending(userId: String) = throw FirebaseConfigurationRequiredException()
}

internal class UnconfiguredSocialRepository : SocialRepository {
    override fun searchReaders(usernamePrefix: String): Flow<List<PublicReaderProfile>> = flowOf(emptyList())
    override fun observeReaderProfile(uid: String): Flow<PublicReaderProfile?> = flowOf(null)
    override fun observeIncomingRequests(userId: String): Flow<List<FriendRequest>> = flowOf(emptyList())
    override fun observeOutgoingRequests(userId: String): Flow<List<FriendRequest>> = flowOf(emptyList())
    override fun observeFriends(userId: String): Flow<List<PublicReaderProfile>> = flowOf(emptyList())
    override fun observeActivityFeed(userId: String): Flow<List<ReaderActivity>> = flowOf(emptyList())
    override suspend fun sendFriendRequest(sender: PublicReaderProfile, recipient: PublicReaderProfile) =
        throw FirebaseConfigurationRequiredException()
    override suspend fun acceptFriendRequest(userId: String, senderId: String) = throw FirebaseConfigurationRequiredException()
    override suspend fun rejectFriendRequest(userId: String, senderId: String) = throw FirebaseConfigurationRequiredException()
    override suspend fun cancelFriendRequest(userId: String, recipientId: String) = throw FirebaseConfigurationRequiredException()
    override suspend fun removeFriend(userId: String, friendId: String) = throw FirebaseConfigurationRequiredException()
}
