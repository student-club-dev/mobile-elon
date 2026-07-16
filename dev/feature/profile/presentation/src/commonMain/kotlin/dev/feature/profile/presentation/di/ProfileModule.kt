package dev.feature.profile.presentation.di

import dev.core.network.NetworkConfig
import dev.core.network.generated.api.ProfileApi
import dev.feature.profile.data.remote.ApiProfileRemoteDataSource
import dev.feature.profile.data.remote.FirestoreProfileRemoteDataSource
import dev.feature.profile.data.remote.ProfileRemoteDataSource
import dev.feature.profile.data.repository.ProfileRepositoryImpl
import dev.feature.profile.domain.repository.ProfileRepository
import dev.feature.profile.domain.usecase.HasProfileUseCase
import dev.feature.profile.domain.usecase.ObserveProfileUseCase
import dev.feature.profile.domain.usecase.RefreshProfileUseCase
import dev.feature.profile.domain.usecase.SaveProfileUseCase
import dev.feature.profile.domain.usecase.UploadAvatarUseCase
import dev.feature.profile.presentation.ProfileViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Profil feature'ining barcha qatlamlarini bog'laydi (domain / data / presentation).
 *
 * [useRemoteApi] — masofaviy manba tanlovi:
 * - `true`  → real backend: `/v1/profile/me` (OpenAPI'dan generatsiya qilingan [ProfileApi]),
 * - `false` → backendsiz rejim: Firestore `users/{uid}`.
 *
 * Bayroq `CoreModules.REMOTE_SYNC_ENABLED` dan keladi — backend tayyor bo'lganda
 * o'sha bitta joyni `true` qilasiz va profil avtomatik REST'ga o'tadi.
 */
fun profileModule(useRemoteApi: Boolean) = module {

    // Generatsiya qilingan klientga ilovaning umumiy Ktor klienti uzatiladi —
    // shunda Firebase ID token (Bearer) har so'rovga avtomatik qo'shiladi.
    single { ProfileApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }

    single<ProfileRemoteDataSource> {
        if (useRemoteApi) ApiProfileRemoteDataSource(get()) else FirestoreProfileRemoteDataSource()
    }

    single<ProfileRepository> { ProfileRepositoryImpl(get(), get(), get()) }

    factory { ObserveProfileUseCase(get()) }
    factory { SaveProfileUseCase(get()) }
    factory { HasProfileUseCase(get()) }
    factory { RefreshProfileUseCase(get()) }
    factory { UploadAvatarUseCase(get()) }

    viewModelOf(::ProfileViewModel)
}
