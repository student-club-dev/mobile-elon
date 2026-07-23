package dev.core.di

import dev.core.common.AppDispatchers
import dev.core.common.DefaultAppDispatchers
import dev.core.common.auth.TokenStore
import dev.core.data.auth.SecureTokenStore
import dev.core.data.auth.SqlDelightTokenStore
import dev.core.data.remote.AdRemoteDataSource
import dev.core.data.remote.ChatRemoteDataSource
import dev.core.data.remote.DiscountRemoteDataSource
import dev.core.data.remote.JobRemoteDataSource
import dev.core.data.remote.KtorAdRemoteDataSource
import dev.core.data.remote.KtorChatRemoteDataSource
import dev.core.data.remote.KtorDiscountRemoteDataSource
import dev.core.data.remote.KtorJobRemoteDataSource
import dev.core.data.remote.KtorStudentRemoteDataSource
import dev.core.data.remote.KtorUniversityRemoteDataSource
import dev.core.data.remote.StudentRemoteDataSource
import dev.core.data.remote.UniversityRemoteDataSource
import dev.core.data.repository.AdRepositoryImpl
import dev.core.data.repository.ChatRepositoryImpl
import dev.core.data.repository.DiscountRepositoryImpl
import dev.core.data.repository.JobRepositoryImpl
import dev.core.data.repository.NotificationRepositoryImpl
import dev.core.data.repository.SettingsRepositoryImpl
import dev.core.data.repository.StudentRepositoryImpl
import dev.core.data.repository.UniversityRepositoryImpl
import dev.core.data.seed.LocalDataSeeder
import dev.core.database.DatabaseFactory
import dev.core.database.DriverFactory
import dev.core.database.sql.ElonUzDatabase
import dev.core.domain.repository.AdRepository
import dev.core.domain.repository.ChatRepository
import dev.core.domain.repository.DiscountRepository
import dev.core.domain.repository.JobRepository
import dev.core.domain.repository.NotificationRepository
import dev.core.domain.repository.SettingsRepository
import dev.core.domain.repository.StudentRepository
import dev.core.domain.repository.UniversityRepository
import dev.core.domain.usecase.ForgotPasswordUseCase
import dev.core.domain.usecase.GetDeviceSessionsUseCase
import dev.core.domain.usecase.LoginUseCase
import dev.core.domain.usecase.LogoutAllDevicesUseCase
import dev.core.domain.usecase.LogoutUseCase
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.core.domain.usecase.RegisterUseCase
import dev.core.domain.usecase.RequestPhoneOtpUseCase
import dev.core.domain.usecase.ResetPasswordUseCase
import dev.core.domain.usecase.RevokeDeviceSessionUseCase
import dev.core.domain.usecase.SetPasswordUseCase
import dev.core.domain.usecase.VerifyPhoneOtpUseCase
import dev.core.network.NetworkConfig
import dev.core.network.createHttpClient
import dev.core.network.media.MediaUploader
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Backend manzillari — API o'zgarganda faqat shu yerni almashtiring.
 * `USE_PROD_API = true` qilib prod'ga o'tasiz (yoki build-flag'ga ulaysiz).
 *
 * ⚠️ Oxiridagi `/v1/` (slash bilan) MUHIM:
 * - Ktor `defaultRequest` nisbiy yo'llarni shunga nisbatan hal qiladi (`get("jobs")` → `/v1/jobs`),
 * - OpenAPI'dan generatsiya qilingan klient ham shu bazaga yo'lni qo'shadi (`/profile/me` → `/v1/profile/me`);
 *   spec'dagi `/v1` prefiksi generatsiya paytida olib tashlanadi (`cleanSwagger`).
 */
const val DEV_BASE_URL = "https://api.studentclub.uz/v1/"
const val PROD_BASE_URL = "https://api.studentclub.uz/v1/"
private const val USE_PROD_API = false

/** Joriy bazaviy URL (bitta manba). */
const val DEFAULT_BASE_URL = DEV_BASE_URL

/**
 * Offline-first sinxronlash yoqilganmi (B4). Yangi spec'da jobs/students/ads/chat endpoint'lari
 * yo'q, shuning uchun bu domenlar local bazadan ishlaydi (seed data).
 */
const val REMOTE_SYNC_ENABLED = false

val networkModule = module {
    single { NetworkConfig(baseUrl = if (USE_PROD_API) PROD_BASE_URL else DEV_BASE_URL) }

    // Sessiya tokenlari platformaning shifrlangan omborida (Android: EncryptedSharedPreferences,
    // iOS: Keychain) — tarmoq qatlami ularni shu orqali o'qiydi/yangilaydi. `SqlDelightTokenStore`
    // faqat eski o'rnatmalardan bir martalik ko'chirish uchun uzatiladi.
    single<TokenStore> { SecureTokenStore(secure = get(), legacy = SqlDelightTokenStore(get())) }

    single<HttpClient> { createHttpClient(get(), get()) }

    // Rasm yuklash — generatsiya qilingan `MediaApi` multipart qismiga `filename` qo'ymagani
    // uchun qo'lda yozilgan (qarang: MediaUploader izohi).
    single { MediaUploader(client = get(), config = get()) }
}

val databaseModule = module {
    single<ElonUzDatabase> { DatabaseFactory.create(get<DriverFactory>()) }
}

val dispatchersModule = module {
    single<AppDispatchers> { DefaultAppDispatchers() }
}

val repositoryModule = module {
    // AuthRepository (backend) auth feature modulida bog'lanadi (authFeatureModule).

    // Barcha domenlar — local DB (SQLDelight) ustidagi repository'lar.
    // --- B4 offline-first: masofaviy manbalar (Ktor) ---
    single<DiscountRemoteDataSource> { KtorDiscountRemoteDataSource(get(), get()) }
    single<JobRemoteDataSource> { KtorJobRemoteDataSource(get()) }
    single<StudentRemoteDataSource> { KtorStudentRemoteDataSource(get()) }
    single<AdRemoteDataSource> { KtorAdRemoteDataSource(get()) }
    single<UniversityRemoteDataSource> { KtorUniversityRemoteDataSource(get()) }
    single<ChatRemoteDataSource> { KtorChatRemoteDataSource(get()) }

    // --- Repository'lar (offline-first: DB + refresh) ---
    single<UniversityRepository> { UniversityRepositoryImpl(get(), get(), get(), REMOTE_SYNC_ENABLED) }
    single<DiscountRepository> { DiscountRepositoryImpl(get(), get(), get(), REMOTE_SYNC_ENABLED) }
    single<JobRepository> { JobRepositoryImpl(get(), get(), get(), REMOTE_SYNC_ENABLED) }
    single<StudentRepository> { StudentRepositoryImpl(get(), get(), get(), REMOTE_SYNC_ENABLED) }
    single<AdRepository> { AdRepositoryImpl(get(), get(), get(), REMOTE_SYNC_ENABLED) }
    // ChatRealtimeSource auth feature'da bog'lanadi (backendда chat endpoint'i yo'q).
    single<ChatRepository> { ChatRepositoryImpl(get(), get(), get(), REMOTE_SYNC_ENABLED, get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get(), get()) }

    // Dizayndagi namuna ma'lumot bilan bazani to'ldiruvchi (bo'sh bo'lsa).
    single { LocalDataSeeder(get(), get()) }
}

val domainModule = module {
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ObserveCurrentUserUseCase(get()) }
    factory { RequestPhoneOtpUseCase(get()) }
    factory { VerifyPhoneOtpUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }
    factory { SetPasswordUseCase(get()) }
    factory { GetDeviceSessionsUseCase(get()) }
    factory { RevokeDeviceSessionUseCase(get()) }
    factory { LogoutAllDevicesUseCase(get()) }
}

/** DriverFactory platformaga bog'liq (Android: Context kerak). */
expect val platformModule: Module

fun coreModules(): List<Module> = listOf(
    platformModule,
    dispatchersModule,
    networkModule,
    databaseModule,
    repositoryModule,
    domainModule,
)
