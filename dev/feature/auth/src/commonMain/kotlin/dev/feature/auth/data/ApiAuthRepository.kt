package dev.feature.auth.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.core.common.Resource
import dev.core.common.auth.AuthTokens
import dev.core.common.auth.TokenStore
import dev.core.common.deviceName
import dev.core.common.error.AppException
import dev.core.common.errorOf
import dev.core.common.network.NetworkConnectivity
import dev.core.common.platformName
import dev.core.database.sql.ElonUzDatabase
import dev.core.database.sql.UserEntity
import dev.core.domain.model.AuthIdentifier
import dev.core.domain.model.DeviceSession
import dev.core.domain.model.OtpChallenge
import dev.core.domain.model.User
import dev.core.domain.model.UserRole
import dev.core.domain.repository.AuthRepository
import dev.core.domain.repository.SettingsRepository
import dev.core.network.generated.api.AuthBusinessApi
import dev.core.network.generated.model.AuthTokensDto
import dev.core.network.generated.model.ForgotPasswordDto
import dev.core.network.generated.model.LoginDto
import dev.core.network.generated.model.LogoutDto
import dev.core.network.generated.model.OtpRequestDto
import dev.core.network.generated.model.OtpVerifyDto
import dev.core.network.generated.model.RegisterDto
import dev.core.network.generated.model.ResetPasswordDto
import dev.core.network.generated.model.SessionDto
import dev.core.network.generated.model.SetPasswordDto
import dev.core.network.resetAuthTokenCache
import dev.core.network.response.safeCall
import dev.feature.profile.domain.repository.ProfileRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [AuthRepository] ning **backend** implementatsiyasi — `/v1/auth/business/…`.
 *
 * Sessiya oqimi:
 * 1. `login`/`register` access + refresh juftligini qaytaradi;
 * 2. juftlik [TokenStore] ga yoziladi va Ktor'ning token keshi tozalanadi
 *    ([resetAuthTokenCache]) — aks holda klient eski (yoki yo'q) token bilan ishlayveradi;
 * 3. access-token JWT'sining `sub` maydoni — foydalanuvchi id'si (biznes egaligi shu bo'yicha);
 * 4. profil (`GET /profile/me`) tortiladi — ism/telefon/rasm shundan keladi;
 * 5. hammasi local `UserEntity` ga yoziladi, UI esa uni **reaktiv** kuzatadi (avtomatik kirish).
 *
 * Token muddati tugasa `refresh` avtomatik, tarmoq qatlamida bo'ladi — bu klass unga aralashmaydi.
 */
class ApiAuthRepository(
    private val api: AuthBusinessApi,
    private val tokenStore: TokenStore,
    private val database: ElonUzDatabase,
    private val httpClient: HttpClient,
    private val connectivity: NetworkConnectivity,
    private val profileRepository: ProfileRepository,
) : AuthRepository {

    private val userQueries get() = database.userQueries

    // ------------------------------------------------------------------
    // Kirish / ro'yxatdan o'tish
    // ------------------------------------------------------------------

    override suspend fun login(identifier: AuthIdentifier, password: String): Resource<User> =
        authenticate(identifier) {
            api.businessAuthLogin(
                LoginDto(
                    password = password,
                    email = (identifier as? AuthIdentifier.Email)?.value,
                    phoneNumber = (identifier as? AuthIdentifier.Phone)?.value,
                    deviceName = deviceName,
                    platform = platformName,
                ),
            ).body()
        }

    override suspend fun register(identifier: AuthIdentifier, password: String): Resource<User> =
        authenticate(identifier) {
            api.businessAuthRegister(
                RegisterDto(
                    password = password,
                    email = (identifier as? AuthIdentifier.Email)?.value,
                    phoneNumber = (identifier as? AuthIdentifier.Phone)?.value,
                    deviceName = deviceName,
                    platform = platformName,
                ),
            ).body()
        }

    /** Umumiy qism: tokenlarni saqlash → profil → local sessiya. */
    private suspend fun authenticate(
        identifier: AuthIdentifier,
        call: suspend () -> AuthTokensDto,
    ): Resource<User> = when (val tokens = safeCall(connectivity) { call() }) {
        is Resource.Error -> tokens
        Resource.Loading -> errorOf(AppException.Unknown())
        is Resource.Success -> {
            val uid = JwtClaims.subject(tokens.data.accessToken)
            if (uid == null) {
                errorOf(AppException.Server(cause = IllegalStateException("Tokenda `sub` yo'q")))
            } else {
                tokenStore.save(
                    AuthTokens(tokens.data.accessToken, tokens.data.refreshToken),
                    userId = uid,
                )
                httpClient.resetAuthTokenCache()
                Resource.Success(cacheSession(uid, identifier))
            }
        }
    }

    /**
     * Profilni backenddan tortib local sessiya qatorini yozadi. Profil kelmasa (yangi hisob
     * yoki tarmoq muammosi) sessiya baribir ochiladi — foydalanuvchi ilovaga kiradi va
     * profilni keyin to'ldiradi.
     */
    private suspend fun cacheSession(uid: String, identifier: AuthIdentifier): User {
        runCatching { profileRepository.refresh() }
        val profile = runCatching { profileRepository.observeProfile().first() }.getOrNull()

        val user = User(
            id = uid,
            fullName = profile?.displayName.orEmpty(),
            email = profile?.email
                ?: (identifier as? AuthIdentifier.Email)?.value.orEmpty(),
            role = profile?.role?.let { role -> UserRole.entries.firstOrNull { it.name == role } }
                ?: UserRole.BUSINESS,
            phoneNumber = profile?.phoneNumber ?: (identifier as? AuthIdentifier.Phone)?.value,
            photoUrl = profile?.avatarUrl,
        )
        // Jins — biznes turlari/kategoriyalar ro'yxatini moslaydi (`GET /business/types?gender=`).
        // Uni profil beradi, shuning uchun kirish paytida local sozlamaga ko'chirib qo'yamiz.
        profile?.gender?.let { database.appSettingQueries.upsert(SettingsRepository.KEY_GENDER, it) }
        userQueries.transaction {
            userQueries.clear()
            userQueries.upsert(
                uid = user.id,
                fullName = user.fullName,
                email = user.email,
                role = user.role.name,
                phoneNumber = user.phoneNumber,
                photoUrl = user.photoUrl,
            )
        }
        return user
    }

    // ------------------------------------------------------------------
    // Chiqish va sessiya holati
    // ------------------------------------------------------------------

    override suspend fun logout() {
        // Refresh tokenni serverда bekor qilamiz. Tarmoq bo'lmasa ham local sessiya tozalanadi:
        // foydalanuvchi "chiqdim" degan bo'lsa, ilova uni ushlab turmasligi kerak.
        tokenStore.tokens()?.refreshToken?.let { refresh ->
            runCatching { api.businessAuthLogout(LogoutDto(refreshToken = refresh)) }
        }
        clearLocalSession()
    }

    private fun clearLocalSession() {
        tokenStore.clear()
        httpClient.resetAuthTokenCache()
        userQueries.clear()
        database.profileQueries.clear()
        database.appSettingQueries.deleteByKey(SettingsRepository.KEY_SELECTED_ROLE)
    }

    override suspend fun currentUser(): User? =
        userQueries.selectCurrent().executeAsOneOrNull()?.toDomainUser()

    override fun observeCurrentUser(): Flow<User?> =
        userQueries.selectCurrent()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomainUser() }

    // ------------------------------------------------------------------
    // Telefonni tasdiqlash
    // ------------------------------------------------------------------

    override suspend fun requestPhoneOtp(phone: String): Resource<OtpChallenge> =
        safeCall(connectivity) {
            val result = api.businessOtpRequest(OtpRequestDto(phoneNumber = phone)).body()
            OtpChallenge(
                expiresInSeconds = result.expiresInSeconds,
                resendCooldownSeconds = result.resendCooldownSeconds,
            )
        }

    override suspend fun verifyPhoneOtp(phone: String, code: String): Resource<Unit> =
        when (
            val result = safeCall(connectivity) {
                api.businessOtpVerify(OtpVerifyDto(phoneNumber = phone, code = code)).body()
            }
        ) {
            is Resource.Error -> result
            Resource.Loading -> errorOf(AppException.Unknown())
            is Resource.Success ->
                if (result.data.verified) Resource.Success(Unit)
                else errorOf(AppException.Validation("Kod noto'g'ri yoki muddati o'tgan."))
        }

    // ------------------------------------------------------------------
    // Parol
    // ------------------------------------------------------------------

    override suspend fun forgotPassword(phone: String): Resource<Unit> = safeCall(connectivity) {
        api.businessPasswordForgot(ForgotPasswordDto(phoneNumber = phone))
        Unit
    }

    override suspend fun resetPassword(
        phone: String,
        code: String,
        newPassword: String,
    ): Resource<Unit> = when (
        val result = safeCall(connectivity) {
            api.businessPasswordReset(
                ResetPasswordDto(phoneNumber = phone, code = code, newPassword = newPassword),
            ).body()
        }
    ) {
        is Resource.Error -> result
        Resource.Loading -> errorOf(AppException.Unknown())
        is Resource.Success ->
            if (result.data.reset) Resource.Success(Unit)
            else errorOf(AppException.Validation("Parolni tiklab bo'lmadi. Kodni tekshiring."))
    }

    override suspend fun setPassword(currentPassword: String?, newPassword: String): Resource<Unit> =
        safeCall(connectivity) {
            api.businessPasswordSet(
                SetPasswordDto(newPassword = newPassword, currentPassword = currentPassword),
            )
            Unit
        }

    // ------------------------------------------------------------------
    // Faol qurilmalar
    // ------------------------------------------------------------------

    override suspend fun sessions(): Resource<List<DeviceSession>> = safeCall(connectivity) {
        api.businessSessionsList().body().map { it.toDomain() }
    }

    override suspend fun revokeSession(id: String): Resource<Unit> = safeCall(connectivity) {
        api.businessSessionsRevoke(id)
        Unit
    }

    override suspend fun logoutAllDevices(): Resource<Unit> {
        val result = safeCall(connectivity) {
            api.businessSessionsLogoutAll()
            Unit
        }
        // Joriy qurilma ham chiqarildi — local sessiyani ushlab turishning ma'nosi yo'q.
        if (result is Resource.Success) clearLocalSession()
        return result
    }
}

// ---------------------------------------------------------------------------
// Mapper'lar
// ---------------------------------------------------------------------------

private fun UserEntity.toDomainUser(): User = User(
    id = uid,
    fullName = fullName,
    email = email,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.BUSINESS),
    phoneNumber = phoneNumber,
    photoUrl = photoUrl,
)

private fun SessionDto.toDomain() = DeviceSession(
    id = id,
    deviceName = deviceName,
    platform = platform,
    ipAddress = ipAddress,
    lastUsedAtMillis = lastUsedAt?.toEpochMilliseconds(),
    createdAtMillis = createdAt.toEpochMilliseconds(),
)
