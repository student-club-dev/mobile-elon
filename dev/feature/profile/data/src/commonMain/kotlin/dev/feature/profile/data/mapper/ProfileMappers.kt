package dev.feature.profile.data.mapper

import dev.core.database.sql.ProfileEntity
import dev.core.network.generated.model.CourseYearDto
import dev.core.network.generated.model.GenderDto
import dev.core.network.generated.model.ProfileRoleDto
import dev.core.network.generated.model.UpdateProfileDto
import dev.core.network.generated.model.UserProfileDto
import dev.feature.profile.domain.model.UserProfile

// ---------------------------------------------------------------------------
// Local kesh (SQLDelight)
// ---------------------------------------------------------------------------

fun ProfileEntity.toDomain(): UserProfile = UserProfile(
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    gender = gender,
    role = role,
    universityId = universityId,
    universityEmail = universityEmail,
    birthYear = birthYear?.toInt(),
    courseYear = courseYear,
    avatarUrl = avatarUrl,
    businessName = businessName,
    businessType = businessType,
    email = email,
)

// ---------------------------------------------------------------------------
// REST — OpenAPI'dan generatsiya qilingan modellar
// ---------------------------------------------------------------------------

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    gender = gender?.value,
    role = role?.value,
    universityId = universityId,
    universityEmail = universityEmail,
    birthYear = birthYear,
    courseYear = courseYear?.value,
    avatarUrl = avatarUrl,
)

/**
 * Domen profil → `PUT /profile/me` tanasi.
 *
 * `businessName`/`businessType`/`email` bu yerda YO'Q: backend profil sxemasida bunday maydonlar
 * yo'q (biznes ma'lumoti alohida `/business` resursida). Ular faqat local keshda saqlanadi.
 */
fun UserProfile.toUpdateRequest(): UpdateProfileDto = UpdateProfileDto(
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    gender = gender?.toGenderDto(),
    role = role?.toRoleDto(),
    universityId = universityId,
    universityEmail = universityEmail,
    birthYear = birthYear,
    courseYear = courseYear?.toCourseYearDto(),
    avatarUrl = avatarUrl,
)

/** Domen `String` -> generatsiya qilingan enum. Noma'lum qiymat bo'lsa `null` (server default'i qoladi). */
private fun String.toRoleDto(): ProfileRoleDto? =
    ProfileRoleDto.entries.firstOrNull { it.value == this }

private fun String.toCourseYearDto(): CourseYearDto? =
    CourseYearDto.entries.firstOrNull { it.value == this }

private fun String.toGenderDto(): GenderDto? =
    GenderDto.entries.firstOrNull { it.value == this }
