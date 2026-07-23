package dev.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.core.common.AppDispatchers
import dev.core.common.Resource
import dev.core.data.remote.AdRemoteDataSource
import dev.core.data.remote.ChatRemoteDataSource
import dev.core.data.remote.DiscountRemoteDataSource
import dev.core.data.remote.JobRemoteDataSource
import dev.core.data.remote.StudentRemoteDataSource
import dev.core.data.remote.UniversityRemoteDataSource
import dev.core.data.mapper.joinDb
import dev.core.data.mapper.toBool
import dev.core.data.mapper.toDb
import dev.core.data.mapper.toDomain
import dev.core.database.sql.ElonUzDatabase
import dev.core.domain.model.Ad
import dev.core.domain.model.Conversation
import dev.core.domain.model.DiscountCategory
import dev.core.domain.model.DiscountOffer
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Job
import dev.core.domain.model.JobApplication
import dev.core.domain.model.Message
import dev.core.domain.model.Student
import dev.core.domain.model.University
import dev.core.domain.repository.AdRepository
import dev.core.domain.repository.ChatRealtimeSource
import dev.core.domain.repository.ChatRepository
import dev.core.domain.repository.DiscountRepository
import dev.core.domain.repository.JobRepository
import dev.core.domain.repository.StudentRepository
import dev.core.domain.repository.UniversityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// ===========================================================================
// Universitetlar
// ===========================================================================
class UniversityRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    private val remote: UniversityRemoteDataSource,
    private val syncEnabled: Boolean,
) : UniversityRepository {
    private val q get() = db.universityQueries

    override fun observeUniversities(): Flow<List<University>> =
        q.selectAll().asFlow().mapToList(dispatchers.io)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun refresh(): Resource<Unit> {
        if (!syncEnabled) return Resource.Success(Unit)
        return when (val res = remote.fetchUniversities()) {
            is Resource.Success -> {
                withContext(dispatchers.io) {
                    q.transaction {
                        q.clear()
                        res.data.forEach { u -> q.upsert(u.id, u.name, u.city, u.monogram, u.faculty, u.accent) }
                    }
                }
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Success(Unit)
        }
    }
}

// ===========================================================================
// Chegirmalar
// ===========================================================================
class DiscountRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    // --- B4 offline-first shabloni: tarmoq manbasi + sinxronlash bayrog'i ---
    private val remote: DiscountRemoteDataSource,
    /** `true` — refresh() backend'dan tortadi; `false` — no-op (backend yo'q, seed saqlanadi). */
    private val syncEnabled: Boolean,
) : DiscountRepository {
    private val q get() = db.discountQueries

    override fun observeCategories(): Flow<List<DiscountCategory>> =
        q.selectCategories().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeOffers(categoryId: String): Flow<List<DiscountOffer>> =
        q.selectOffersByCategory(categoryId).asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeFeatured(): Flow<List<DiscountOffer>> =
        q.selectFeaturedOffers().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeSaved(): Flow<List<DiscountOffer>> =
        q.selectSavedOffers().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override suspend fun setSaved(offerId: String, saved: Boolean) = withContext(dispatchers.io) {
        if (saved) q.saveOffer(offerId) else q.unsaveOffer(offerId)
    }

    /**
     * Offline-first sinxronlash: backend'dan oladi, muvaffaqiyatда local DB'ni almashtiradi.
     * Xato/tarmoqsiz bo'lsa — DB'ga tegilmaydi (cache/seed saqlanadi). UI DB'ni kuzatgani
     * uchun yangilanish avtomatik ko'rinadi.
     */
    override suspend fun refresh(): Resource<Unit> {
        if (!syncEnabled) return Resource.Success(Unit) // Backend hali yo'q — no-op.
        return when (val res = remote.fetchDiscounts()) {
            is Resource.Success -> {
                withContext(dispatchers.io) {
                    q.transaction {
                        q.clearCategories()
                        q.clearOffers()
                        res.data.categories.forEach { c ->
                            q.upsertCategory(c.id, c.name, c.emoji, c.offerCount.toLong(), c.accent)
                        }
                        res.data.offers.forEach { o ->
                            q.upsertOffer(
                                o.id, o.categoryId, o.merchant, o.title, o.discountPercent.toLong(),
                                o.tag, o.promoCode, o.location, o.expiry, o.emoji, o.bannerAccent,
                                if (o.featured) 1L else 0L,
                            )
                        }
                    }
                }
                Resource.Success(Unit)
            }
            is Resource.Error -> res           // cache saqlanadi
            Resource.Loading -> Resource.Success(Unit)
        }
    }
}

// ===========================================================================
// Ishlar
// ===========================================================================
class JobRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    private val remote: JobRemoteDataSource,
    private val syncEnabled: Boolean,
) : JobRepository {
    private val q get() = db.jobQueries

    override fun observeJobs(): Flow<List<Job>> =
        q.selectAllJobs().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeBookmarked(): Flow<List<Job>> =
        q.selectBookmarked().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeApplications(): Flow<List<JobApplication>> =
        q.selectApplications().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override suspend fun setBookmarked(jobId: String, bookmarked: Boolean) = withContext(dispatchers.io) {
        q.setBookmark(bookmarked.toDb(), jobId)
    }

    override suspend fun apply(job: Job) = withContext(dispatchers.io) {
        q.upsertApplication(
            id = "app-${job.id}",
            jobId = job.id,
            jobTitle = job.title,
            company = job.company,
            status = dev.core.domain.model.ApplicationStatus.SENT.name,
            appliedAgo = "hozir",
        )
    }

    override suspend fun refresh(): Resource<Unit> {
        if (!syncEnabled) return Resource.Success(Unit)
        return when (val res = remote.fetchJobs()) {
            is Resource.Success -> {
                withContext(dispatchers.io) {
                    q.transaction {
                        q.clearJobs()
                        res.data.forEach { j ->
                            q.upsertJob(
                                j.id, j.title, j.company, j.companyMonogram, j.location, j.category,
                                j.tags.joinDb(), j.salary, j.remote.toDb(), j.partTime.toDb(),
                                j.postedAgo, j.field, j.bookmarked.toDb(),
                            )
                        }
                    }
                }
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Success(Unit)
        }
    }
}

// ===========================================================================
// Studentlar
// ===========================================================================
class StudentRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    private val remote: StudentRemoteDataSource,
    private val syncEnabled: Boolean,
) : StudentRepository {
    private val q get() = db.studentQueries

    override fun observeStudents(): Flow<List<Student>> =
        q.selectAll().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeByUniversity(universityId: String): Flow<List<Student>> =
        q.selectByUniversity(universityId).asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override suspend fun setFriendStatus(studentId: String, status: FriendStatus) = withContext(dispatchers.io) {
        q.setFriendStatus(status.name, studentId)
    }

    override suspend fun refresh(): Resource<Unit> {
        if (!syncEnabled) return Resource.Success(Unit)
        return when (val res = remote.fetchStudents()) {
            is Resource.Success -> {
                withContext(dispatchers.io) {
                    q.transaction {
                        q.clear()
                        res.data.forEach { s ->
                            q.upsert(
                                s.id, s.firstName, s.lastName, s.initial, s.universityId, s.universityMonogram,
                                s.course.toLong(), s.faculty, s.friendStatus, s.interests.joinDb(),
                                s.friendsCount.toLong(), s.adsCount.toLong(), s.rating,
                            )
                        }
                    }
                }
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Success(Unit)
        }
    }
}

// ===========================================================================
// E'lonlar
// ===========================================================================
class AdRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    private val remote: AdRemoteDataSource,
    private val syncEnabled: Boolean,
) : AdRepository {
    private val q get() = db.adQueries

    override fun observeAds(): Flow<List<Ad>> =
        q.selectAll().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeByOwner(ownerId: String): Flow<List<Ad>> =
        q.selectByOwner(ownerId).asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override suspend fun post(ad: Ad) = withContext(dispatchers.io) {
        q.upsert(
            id = ad.id,
            type = ad.type.name,
            title = ad.title,
            category = ad.category,
            price = ad.price,
            description = ad.description,
            images = ad.images.joinDb(),
            ownerId = ad.ownerId,
            createdAgo = ad.createdAgo,
        )
    }

    override suspend fun delete(adId: String) = withContext(dispatchers.io) {
        q.deleteById(adId)
    }

    override suspend fun refresh(): Resource<Unit> {
        if (!syncEnabled) return Resource.Success(Unit)
        return when (val res = remote.fetchAds()) {
            is Resource.Success -> {
                withContext(dispatchers.io) {
                    q.transaction {
                        q.clear()
                        res.data.forEach { a ->
                            q.upsert(
                                a.id, a.type, a.title, a.category, a.price, a.description,
                                a.images.joinDb(), a.ownerId, a.createdAgo,
                            )
                        }
                    }
                }
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Success(Unit)
        }
    }
}

// ===========================================================================
// Chat
// ===========================================================================
class ChatRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    private val remote: ChatRemoteDataSource,
    private val syncEnabled: Boolean,
    // --- B7: real-time manba. enabled=false bo'lsa local DB'dan ishlaydi. ---
    private val realtime: ChatRealtimeSource,
) : ChatRepository {
    private val q get() = db.chatQueries

    override fun observeConversations(): Flow<List<Conversation>> =
        if (realtime.enabled) realtime.conversations()
        else q.selectConversations().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeArchivedConversations(): Flow<List<Conversation>> =
        if (realtime.enabled) realtime.archivedConversations()
        else q.selectArchivedConversations().asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        if (realtime.enabled) realtime.messages(conversationId)
        else q.selectMessages(conversationId).asFlow().mapToList(dispatchers.io).map { r -> r.map { it.toDomain() } }

    override suspend fun send(conversationId: String, text: String, time: String, createdAt: Long) {
        if (realtime.enabled) {
            realtime.send(conversationId, text, time, createdAt)
            return
        }
        withContext(dispatchers.io) {
            q.transaction {
                q.insertMessage(
                    id = "$conversationId-$createdAt",
                    conversationId = conversationId,
                    body = text,
                    outgoing = true.toDb(),
                    time = time,
                    createdAt = createdAt,
                )
                q.touchConversation(text, time, 0L, conversationId)
            }
        }
    }

    override suspend fun markRead(conversationId: String) {
        if (realtime.enabled) {
            realtime.markRead(conversationId)
            return
        }
        withContext(dispatchers.io) { q.markRead(conversationId) }
    }

    override suspend fun deleteMessage(conversationId: String, messageId: String) {
        if (realtime.enabled) {
            realtime.deleteMessage(conversationId, messageId)
            return
        }
        withContext(dispatchers.io) { q.deleteMessage(messageId) }
    }

    override suspend fun clearMessages(conversationId: String) {
        if (realtime.enabled) {
            realtime.clearMessages(conversationId)
            return
        }
        withContext(dispatchers.io) { q.clearConversationMessages(conversationId) }
    }

    override suspend fun deleteConversation(conversationId: String) {
        if (realtime.enabled) {
            realtime.deleteConversation(conversationId)
            return
        }
        withContext(dispatchers.io) {
            q.transaction {
                q.clearConversationMessages(conversationId)
                q.deleteConversation(conversationId)
            }
        }
    }

    override suspend fun setArchived(conversationId: String, archived: Boolean) {
        if (realtime.enabled) {
            realtime.setArchived(conversationId, archived)
            return
        }
        withContext(dispatchers.io) { q.setArchived(if (archived) 1L else 0L, conversationId) }
    }

    override suspend fun refresh(): Resource<Unit> {
        if (!syncEnabled) return Resource.Success(Unit)
        return when (val res = remote.fetchConversations()) {
            is Resource.Success -> {
                withContext(dispatchers.io) {
                    q.transaction {
                        q.clearConversations()
                        res.data.forEach { c ->
                            q.upsertConversation(
                                c.id, c.peerName, c.peerInitial, c.type, c.online.toDb(),
                                c.lastMessage, c.lastTime, c.unreadCount.toLong(),
                            )
                        }
                    }
                }
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Success(Unit)
        }
    }
}
