package dev.core.domain.model

/** Do'stlik holati (student connect). */
enum class FriendStatus { NONE, PENDING, FRIENDS }

/** Studentlar ro'yxati saralash rejimi. */
enum class StudentSort { MY_UNIVERSITY, ALL }

/** Boshqa student (do'st topish / profil). */
data class Student(
    val id: String,
    val firstName: String,
    val lastName: String,
    val initial: String,         // "D"
    val universityId: String,
    val universityMonogram: String, // "TATU"
    val course: Int,             // 1..4 (Mag = 5)
    val faculty: String,         // "IT", "Telekom", "Dasturiy inj."
    val friendStatus: FriendStatus = FriendStatus.NONE,
    val interests: List<String> = emptyList(), // ["Dizayn", "Frontend"]
    val friendsCount: Int = 0,
    val adsCount: Int = 0,
    val rating: Double = 0.0,
) {
    val fullName: String get() = "$firstName $lastName".trim()
}
