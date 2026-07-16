package dev.feature.auth.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.core.common.Resource
import dev.core.database.sql.StudentClubsDatabase
import dev.core.database.sql.UserEntity
import dev.core.domain.model.ExternalAuthUser
import dev.core.domain.model.User
import dev.core.domain.model.UserRole
import dev.core.domain.repository.AuthRepository
import dev.core.domain.repository.SettingsRepository
import dev.gitlive.firebase.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.functions.functions

/**
 * [AuthRepository] ning GitLive Firebase (backendsiz) implementatsiyasi.
 *
 * - Email/parol, ro'yxat, parolni tiklash → **Firebase Auth** (commonMain).
 * - Profil (rol, universitet, kurs...) bu yerda EMAS → `:dev:feature:profile` moduli
 *   (`ProfileRepository`) unga egalik qiladi.
 * - Google/Telefon → platformaga bog'liq `SocialAuthController` Firebase'ga kiritadi,
 *   bu yerdagi [syncExternalUser] esa o'sha sessiyani o'qib domen [User] qaytaradi.
 *
 * GitLive `Firebase.auth` platformadagi bir xil Firebase
 * singleton ustida ishlaydi — shu sabab native controller kiritgan sessiya bu yerda
 * ham ko'rinadi.
 */
class FirebaseAuthRepository(
    /** Local sessiya keshi (SQLDelight) — offline ishlash va avtomatik kirish uchun. */
    private val database: StudentClubsDatabase,
) : AuthRepository {

    private val auth get() = Firebase.auth
    private val fns get() = Firebase.functions
    private val userQueries get() = database.userQueries
    private val profileQueries get() = database.profileQueries

    override suspend fun login(email: String, password: String): Resource<User> = try {
        val fbUser = auth.signInWithEmailAndPassword(email, password).user
            ?: return Resource.Error("Foydalanuvchi topilmadi")
        val user = fbUser.toDomainUser()
        cacheUser(fbUser.uid, user)
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(mapError(e), e)
    }

    override suspend fun register(email: String, password: String): Resource<User> = try {
        val fbUser = auth.createUserWithEmailAndPassword(email, password).user
            ?: return Resource.Error("Hisob yaratilmadi")
        val user = fbUser.toDomainUser()
        cacheUser(fbUser.uid, user)
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(mapError(e), e)
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> = try {
        auth.sendPasswordResetEmail(email)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(mapError(e), e)
    }

    override suspend fun requestEmailSignup(email: String): Resource<Unit> = try {
        fns.httpsCallable("requestEmailSignup").invoke(mapOf("email" to email))
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Kod yuborilmadi", e)
    }

    override suspend fun confirmEmailSignup(email: String, code: String, password: String): Resource<Unit> = try {
        // Server kodni tekshirib akkaunt yaratadi (emailVerified=true)
        fns.httpsCallable("confirmEmailSignup")
            .invoke(mapOf("email" to email, "code" to code, "password" to password))
        // Endi email/parol bilan kiramiz
        auth.signInWithEmailAndPassword(email, password)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Tasdiqlash amalga oshmadi", e)
    }

    override suspend fun syncExternalUser(external: ExternalAuthUser): Resource<User> {
        val fbUser = auth.currentUser
            ?: return Resource.Error("Firebase sessiyasi topilmadi")
        val user = fbUser.toDomainUser()
        cacheUser(fbUser.uid, user)
        return Resource.Success(user)
    }

    override suspend fun logout() {
        auth.signOut()
        // Sessiya tugadi — local kesh (sessiya + profil) to'liq tozalanadi.
        userQueries.clear()
        profileQueries.clear()
        // Tanlangan rol ham tozalanadi — keyingi kirishда rol tanlash chiqadi.
        database.appSettingQueries.deleteByKey(SettingsRepository.KEY_SELECTED_ROLE)
    }

    override suspend fun currentUser(): User? {
        val fbUser = auth.currentUser
        if (fbUser == null) {
            // Sessiya yo'q — eskirgan keshni tozalaymiz.
            userQueries.clear()
            profileQueries.clear()
            return null
        }
        // Offline-first: kesh bo'lsa darrov qaytaramiz (tarmoqsiz ishlaydi)
        cachedUser()?.let { return it }
        // Kesh bo'sh — Firebase sessiyasidan tiklab keshlaymiz
        val user = fbUser.toDomainUser()
        cacheUser(fbUser.uid, user)
        return user
    }

    override fun observeCurrentUser(): Flow<User?> =
        userQueries.selectCurrent()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomainUser() }

    // ------------------------------------------------------------------
    // Local kesh (SQLDelight)
    // ------------------------------------------------------------------
    private fun cachedUser(): User? =
        userQueries.selectCurrent().executeAsOneOrNull()?.toDomainUser()

    /** Bitta joriy-foydalanuvchi qatorini yozadi (avval eskisini o'chirib). */
    private fun cacheUser(uid: String, user: User) {
        userQueries.transaction {
            userQueries.clear()
            userQueries.upsert(
                uid = uid,
                userId = user.id,
                fullName = user.fullName,
                email = user.email,
                role = user.role.name,
                phoneNumber = user.phoneNumber,
                photoUrl = user.photoUrl,
            )
        }
    }

    private fun UserEntity.toDomainUser(): User = User(
        id = userId,
        fullName = fullName,
        email = email,
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.STUDENT),
        phoneNumber = phoneNumber,
        photoUrl = photoUrl,
    )

    private fun FirebaseUser.toDomainUser(): User {
        return User(
            id = uid.hashCode().toLong() and 0xffffffffL,
            // Ism profildan EMAS — sessiyadan. Profil nomi ProfileRepository'dan keladi.
            fullName = displayName
                ?: email?.substringBefore('@')
                ?: phoneNumber
                ?: "Foydalanuvchi",
            email = email.orEmpty(),
            role = UserRole.STUDENT,
            phoneNumber = phoneNumber,
            photoUrl = photoURL,
        )
    }

    private fun mapError(e: Throwable): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "Parol juda oddiy (kamida 6 belgi)."
        is FirebaseAuthInvalidCredentialsException -> "Email yoki parol noto‘g‘ri."
        is FirebaseAuthInvalidUserException -> "Bunday foydalanuvchi topilmadi."
        is FirebaseAuthUserCollisionException -> "Bu email allaqachon ro‘yxatdan o‘tgan."
        else -> e.message ?: "Xatolik yuz berdi."
    }
}
