package dev.feature.discounts.presentation.di

import dev.core.network.NetworkConfig
import dev.core.network.createPublicHttpClient
import dev.core.network.generated.api.ListingsApi
import dev.core.network.generated.api.MediaApi
import dev.feature.discounts.data.remote.ApiListingRemoteDataSource
import dev.feature.discounts.data.remote.ListingRemoteDataSource
import dev.feature.discounts.data.remote.LocalListingRemoteDataSource
import dev.feature.discounts.data.remote.NominatimGeoRepository
import dev.feature.discounts.data.repository.FirestoreListingRepository
import dev.feature.discounts.data.repository.ListingRepositoryImpl
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.ListingRepository
import dev.feature.discounts.domain.usecase.CreateBranchFromPointUseCase
import dev.feature.discounts.domain.usecase.DeleteListingUseCase
import dev.feature.discounts.domain.usecase.GetListingUseCase
import dev.feature.discounts.domain.usecase.ObserveActiveListingsUseCase
import dev.feature.discounts.domain.usecase.ObserveMyListingsUseCase
import dev.feature.discounts.domain.usecase.PublishListingUseCase
import dev.feature.discounts.domain.usecase.SaveDraftUseCase
import dev.feature.discounts.domain.usecase.SearchPlacesUseCase
import dev.feature.discounts.domain.usecase.ToggleListingPausedUseCase
import dev.feature.discounts.domain.usecase.UploadListingImageUseCase
import dev.feature.discounts.presentation.MyListingsViewModel
import dev.feature.discounts.presentation.NearbyDiscountsViewModel
import dev.feature.discounts.presentation.AddBusinessViewModel
import dev.feature.discounts.presentation.MyBusinessesViewModel
import dev.feature.discounts.presentation.PostListingViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Chegirmalar feature'ining barcha qatlamlarini bog'laydi (domain / data / presentation).
 *
 * [useRemoteApi] — masofaviy manba tanlovi:
 * - `true`  → real backend: `POST /v1/business/{id}/listings` + `/submit`, rasm `/media/upload`
 *   (OpenAPI'dan generatsiya qilingan [ListingsApi] / [MediaApi]),
 * - `false` → backendsiz rejim: e'lon local bazada, rasm `data:` URI sifatida saqlanadi.
 *
 * Bayroq `CoreModules.REMOTE_SYNC_ENABLED` dan keladi — profil moduli bilan bir xil naqsh.
 */
fun discountsModule(useRemoteApi: Boolean, useFirestore: Boolean = false) = module {

    single { ListingsApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { MediaApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }

    single<ListingRemoteDataSource> {
        if (useRemoteApi) ApiListingRemoteDataSource(get(), get()) else LocalListingRemoteDataSource()
    }

    // Firebase rejim: e'lonlar Firestore'da (real-time, qurilmalararo). Aks holda offline-first
    // local repository (SQLDelight + masofaviy manba).
    single<ListingRepository> {
        if (useFirestore) FirestoreListingRepository(get()) else ListingRepositoryImpl(get(), get(), get())
    }

    // Biznes (nom, telefon, tur, lokatsiya) — Firestore `businesses/{id}`.
    single<dev.feature.discounts.domain.repository.BusinessRepository> {
        dev.feature.discounts.data.repository.FirestoreBusinessRepository(get())
    }
    factory { dev.feature.discounts.domain.usecase.ObserveMyBusinessesUseCase(get()) }
    factory { dev.feature.discounts.domain.usecase.GetBusinessUseCase(get()) }
    factory { dev.feature.discounts.domain.usecase.SaveBusinessUseCase(get()) }
    factory { dev.feature.discounts.domain.usecase.DeleteBusinessUseCase(get()) }
    viewModelOf(::MyBusinessesViewModel)
    viewModelOf(::AddBusinessViewModel)

    // Teskari geokodlash — OpenStreetMap Nominatim (tekin). Ilovaning umumiy klienti EMAS:
    // unda Firebase Bearer tokeni bor, uni begona serverga yuborib bo'lmaydi.
    single<GeoRepository> { NominatimGeoRepository(createPublicHttpClient()) }

    factory { CreateBranchFromPointUseCase(get()) }
    factory { SearchPlacesUseCase(get()) }
    factory { ObserveMyListingsUseCase(get()) }
    factory { ObserveActiveListingsUseCase(get()) }
    factory { SaveDraftUseCase(get()) }
    factory { PublishListingUseCase(get()) }
    factory { ToggleListingPausedUseCase(get()) }
    factory { DeleteListingUseCase(get()) }
    factory { UploadListingImageUseCase(get()) }
    factory { GetListingUseCase(get()) }

    viewModelOf(::PostListingViewModel)
    viewModelOf(::MyListingsViewModel)
    viewModelOf(::NearbyDiscountsViewModel)
}
