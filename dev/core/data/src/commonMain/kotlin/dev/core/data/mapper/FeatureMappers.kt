package dev.core.data.mapper

import dev.core.database.sql.AdEntity
import dev.core.database.sql.ConversationEntity
import dev.core.database.sql.DiscountCategoryEntity
import dev.core.database.sql.DiscountOfferEntity
import dev.core.database.sql.JobApplicationEntity
import dev.core.database.sql.JobEntity
import dev.core.database.sql.MessageEntity
import dev.core.database.sql.StudentEntity
import dev.core.database.sql.UniversityEntity
import dev.core.domain.model.Ad
import dev.core.domain.model.AdType
import dev.core.domain.model.ApplicationStatus
import dev.core.domain.model.Conversation
import dev.core.domain.model.ConversationType
import dev.core.domain.model.DiscountCategory
import dev.core.domain.model.DiscountOffer
import dev.core.domain.model.DiscountTag
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Job
import dev.core.domain.model.JobApplication
import dev.core.domain.model.Message
import dev.core.domain.model.Student
import dev.core.domain.model.University

// --- List <-> TEXT ("|" bilan) ---
internal fun List<String>.joinDb(): String = joinToString("|")
internal fun String.splitDb(): List<String> =
    if (isBlank()) emptyList() else split("|").filter { it.isNotBlank() }

// --- Boolean <-> INTEGER ---
internal fun Boolean.toDb(): Long = if (this) 1L else 0L
internal fun Long.toBool(): Boolean = this != 0L

// --- Enum xavfsiz o'qish ---
private inline fun <reified T : Enum<T>> parseEnum(value: String, default: T): T =
    runCatching { enumValueOf<T>(value) }.getOrDefault(default)

fun UniversityEntity.toDomain(): University = University(
    id = id, name = name, city = city, monogram = monogram, faculty = faculty, accent = accent,
)

fun DiscountCategoryEntity.toDomain(): DiscountCategory = DiscountCategory(
    id = id, name = name, emoji = emoji, offerCount = offerCount.toInt(), accent = accent,
)

fun DiscountOfferEntity.toDomain(): DiscountOffer = DiscountOffer(
    id = id,
    categoryId = categoryId,
    merchant = merchant,
    title = title,
    discountPercent = discountPercent.toInt(),
    tag = parseEnum(tag, DiscountTag.STUDENT_ID),
    promoCode = promoCode,
    location = location,
    expiry = expiry,
    emoji = emoji,
    bannerAccent = bannerAccent,
    featured = featured.toBool(),
)

fun JobEntity.toDomain(): Job = Job(
    id = id,
    title = title,
    company = company,
    companyMonogram = companyMonogram,
    location = location,
    category = category,
    tags = tags.splitDb(),
    salary = salary,
    remote = remote.toBool(),
    partTime = partTime.toBool(),
    postedAgo = postedAgo,
    field = field_,
    bookmarked = bookmarked.toBool(),
)

fun JobApplicationEntity.toDomain(): JobApplication = JobApplication(
    id = id,
    jobId = jobId,
    jobTitle = jobTitle,
    company = company,
    status = parseEnum(status, ApplicationStatus.SENT),
    appliedAgo = appliedAgo,
)

fun StudentEntity.toDomain(): Student = Student(
    id = id,
    firstName = firstName,
    lastName = lastName,
    initial = initial,
    universityId = universityId,
    universityMonogram = universityMonogram,
    course = course.toInt(),
    faculty = faculty,
    friendStatus = parseEnum(friendStatus, FriendStatus.NONE),
    interests = interests.splitDb(),
    friendsCount = friendsCount.toInt(),
    adsCount = adsCount.toInt(),
    rating = rating,
)

fun AdEntity.toDomain(): Ad = Ad(
    id = id,
    type = parseEnum(type, AdType.OTHER),
    title = title,
    category = category,
    price = price,
    description = description,
    images = images.splitDb(),
    ownerId = ownerId,
    createdAgo = createdAgo,
)

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    peerName = peerName,
    peerInitial = peerInitial,
    type = parseEnum(type, ConversationType.PEER),
    online = online.toBool(),
    lastMessage = lastMessage,
    lastTime = lastTime,
    unreadCount = unreadCount.toInt(),
    archived = archived != 0L,
)

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    text = body,
    outgoing = outgoing.toBool(),
    time = time,
    createdAt = createdAt,
)
