package dev.feature.auth.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.core.common.Resource
import dev.core.database.sql.ElonUzDatabase
import dev.core.database.sql.UserEntity
import dev.core.domain.model.ExternalAuthUser
import dev.core.domain.model.User
import dev.core.domain.model.UserRole
import dev.core.domain.repository.AuthRepository
import dev.core.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [AuthRepository] ning **local test** implementatsiyasi (`USE_LOCAL_DATA`) — Firebase/SMS/backend
 * YO'Q. Har qanday kirish (dev-login, email, OTP tasdiq, ijtimoiy) darrov muvaffaqiyatli bo'lib,
 * bitta **dev foydalanuvchi** local `UserEntity`ga yoziladi. Shu uid barcha local ma'lumot
 * (profil, biznes, e'lon) egaligini belgilaydi.
 */
class DevAuthRepository(
    private val database: ElonUzDatabase,
) : AuthRepository {

    private val userQueries get() = database.userQueries
    private val profileQueries get() = database.profileQueries

    private fun devUser() = User(
        id = DEV_USER_ID,
        fullName = "Dev Foydalanuvchi",
        email = "dev@local.test",
        role = UserRole.ADMIN,
        phoneNumber = "+998900000000",
    )

    private fun cacheDevUser() {
        val u = devUser()
        userQueries.transaction {
            userQueries.clear()
            userQueries.upsert(
                uid = DEV_UID,
                userId = u.id,
                fullName = u.fullName,
                email = u.email,
                role = u.role.name,
                phoneNumber = u.phoneNumber,
                photoUrl = u.photoUrl,
            )
        }
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        cacheDevUser(); return Resource.Success(devUser())
    }

    override suspend fun register(email: String, password: String): Resource<User> {
        cacheDevUser(); return Resource.Success(devUser())
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun requestEmailSignup(email: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun confirmEmailSignup(email: String, code: String, password: String): Resource<Unit> {
        cacheDevUser(); return Resource.Success(Unit)
    }

    override suspend fun syncExternalUser(external: ExternalAuthUser): Resource<User> {
        cacheDevUser(); return Resource.Success(devUser())
    }

    override suspend fun logout() {
        userQueries.clear()
        profileQueries.clear()
        database.appSettingQueries.deleteByKey(SettingsRepository.KEY_SELECTED_ROLE)
    }

    override suspend fun currentUser(): User? =
        userQueries.selectCurrent().executeAsOneOrNull()?.toDomainUser()

    override fun observeCurrentUser(): Flow<User?> =
        userQueries.selectCurrent()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomainUser() }

    private fun UserEntity.toDomainUser(): User = User(
        id = userId,
        fullName = fullName,
        email = email,
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.STUDENT),
        phoneNumber = phoneNumber,
        photoUrl = photoUrl,
    )

    companion object {
        const val DEV_UID = "dev-user-1"
        const val DEV_USER_ID = 1L
    }
}
