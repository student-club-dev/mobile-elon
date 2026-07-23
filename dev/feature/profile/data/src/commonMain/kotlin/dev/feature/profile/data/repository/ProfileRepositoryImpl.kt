package dev.feature.profile.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.core.common.AppDispatchers
import dev.core.common.Resource
import dev.core.database.sql.ElonUzDatabase
import dev.feature.profile.data.mapper.toDomain
import dev.feature.profile.data.remote.ProfileRemoteDataSource
import dev.core.domain.repository.SessionProvider
import dev.core.domain.repository.SettingsRepository
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
import dev.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Offline-first profil repository'si.
 *
 * UI **faqat** local DB'ni (`ProfileEntity`) kuzatadi — shu sabab tarmoq bo'lmasa ham
 * profil ko'rinadi. [refresh]/[saveProfile] masofaviy manba bilan gaplashib keshni yangilaydi.
 * Masofaviy manba qaysi ekanini bu klass bilmaydi ([ProfileRemoteDataSource]).
 */
class ProfileRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    private val remote: ProfileRemoteDataSource,
    private val session: SessionProvider,
) : ProfileRepository {

    private val q get() = db.profileQueries

    /** Kesh kaliti — joriy sessiya uid (access-token JWT `sub`). */
    private val currentUid: String? get() = session.currentUid()

    override fun observeProfile(): Flow<UserProfile?> =
        q.selectCurrent()
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { it?.toDomain() }

    override suspend fun refresh(): Resource<Unit> {
        val uid = currentUid ?: return Resource.Error("Sessiya topilmadi")
        return when (val res = remote.fetch()) {
            is Resource.Success -> {
                // Masofada profil yo'q bo'lsa keshni o'zgartirmaymiz (mavjud local
                // ma'lumotni o'chirib yubormaslik uchun).
                res.data?.let { cache(uid, it) }
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Success(Unit)
        }
    }

    override suspend fun saveProfile(profile: UserProfile): Resource<Unit> {
        val uid = currentUid ?: return Resource.Error("Sessiya topilmadi — avval kiring")
        return when (val res = remote.save(profile)) {
            is Resource.Success -> {
                cache(uid, res.data)
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Success(Unit)
        }
    }

    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> {
        val uid = currentUid ?: return Resource.Error("Sessiya topilmadi — avval kiring")

        val url = when (val uploaded = remote.uploadAvatar(bytes, fileName)) {
            is Resource.Success -> uploaded.data
            is Resource.Error -> return uploaded
            Resource.Loading -> return Resource.Error("Rasmni yuklab bo'lmadi")
        }

        // Rasm yuklandi — endi uning manzilini profilga yozamiz, aks holda URL yo'qoladi.
        val updated = (cachedProfile() ?: UserProfile()).copy(avatarUrl = url)
        return when (val saved = remote.save(updated)) {
            is Resource.Success -> {
                cache(uid, saved.data)
                Resource.Success(url)
            }
            is Resource.Error -> saved
            Resource.Loading -> Resource.Success(url)
        }
    }

    override suspend fun profileExistence(): ProfileExistence {
        // Offline-first: local kesh bo'lsa tarmoqni kutmaymiz — profil aniq bor.
        if (cachedProfile() != null) return ProfileExistence.EXISTS
        // Kesh yo'q — backend hal qiladi: MISSING (404) → SignUp, ERROR → OTP'da qayta urinish.
        return remote.checkExistence()
    }

    private suspend fun cachedProfile(): UserProfile? = withContext(dispatchers.io) {
        q.selectCurrent().executeAsOneOrNull()?.toDomain()
    }

    /**
     * Bitta joriy-profil qatorini yozadi (avval eskisini o'chirib).
     *
     * [p] masofaviy manbadan kelgan profil, unda `businessName`/`businessType`/`email` **doim
     * `null`**: backend sxemasida bunday maydonlar yo'q (ProfileMappers.kt izohiga qarang).
     * Shuning uchun ular keshdagi eski qatordan olib qolinadi — aks holda har `refresh()` yoki
     * `saveProfile()` chaqirig'i faqat local yashaydigan bu ma'lumotni o'chirib yuborardi.
     */
    private suspend fun cache(uid: String, p: UserProfile) = withContext(dispatchers.io) {
        val cached = q.selectCurrent().executeAsOneOrNull()
        q.transaction {
            q.clear()
            q.upsert(
                uid = uid,
                firstName = p.firstName,
                lastName = p.lastName,
                phoneNumber = p.phoneNumber,
                gender = p.gender,
                role = p.role,
                universityId = p.universityId,
                universityEmail = p.universityEmail,
                birthYear = p.birthYear?.toLong(),
                courseYear = p.courseYear,
                avatarUrl = p.avatarUrl,
                businessName = p.businessName ?: cached?.businessName,
                businessType = p.businessType ?: cached?.businessType,
                email = p.email ?: cached?.email,
            )
            // Jins — biznes turlari/kategoriyalarni moslaydi (`GET /business/types?gender=`).
            // Uni kirish paytida auth ham ko'chiradi, ammo profil tahrirlanganda qiymat shu
            // yerda yangilanmasa ro'yxat eski jinsga qarab filtrlanib qolardi.
            p.gender?.let { db.appSettingQueries.upsert(SettingsRepository.KEY_GENDER, it) }
        }
    }
}
