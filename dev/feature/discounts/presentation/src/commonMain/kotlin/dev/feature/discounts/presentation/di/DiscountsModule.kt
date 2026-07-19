package dev.feature.discounts.presentation.di

import dev.core.domain.USE_LOCAL_DATA
import dev.core.network.NetworkConfig
import dev.core.network.createPublicHttpClient
import dev.core.network.generated.api.BranchesApi
import dev.core.network.generated.api.BusinessApi
import dev.core.network.generated.api.DiscountsApi
import dev.core.network.generated.api.GeoApi
import dev.core.network.generated.api.ListingsApi
import dev.core.network.generated.api.MediaApi
import dev.feature.discounts.data.remote.ApiGeoRepository
import dev.feature.discounts.data.remote.ApiListingRemoteDataSource
import dev.feature.discounts.data.remote.FallbackGeoRepository
import dev.feature.discounts.data.remote.FallbackListingRemoteDataSource
import dev.feature.discounts.data.remote.ListingRemoteDataSource
import dev.feature.discounts.data.remote.LocalListingRemoteDataSource
import dev.feature.discounts.data.remote.NominatimGeoRepository
import dev.feature.discounts.data.repository.ApiBusinessRepository
import dev.feature.discounts.data.repository.ApiCatalogRepository
import dev.feature.discounts.data.repository.ApiDiscountFeedRepository
import dev.feature.discounts.data.repository.ListingRepositoryImpl
import dev.feature.discounts.data.repository.LocalBusinessRepository
import dev.feature.discounts.data.repository.LocalCatalogRepository
import dev.feature.discounts.data.repository.LocalDiscountFeedRepository
import dev.feature.discounts.domain.repository.CatalogRepository
import dev.feature.discounts.domain.repository.DiscountFeedRepository
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.ListingRepository
import dev.feature.discounts.domain.usecase.CreateBranchFromPointUseCase
import dev.feature.discounts.domain.usecase.GetBusinessTypesUseCase
import dev.feature.discounts.domain.usecase.GetCategoriesUseCase
import dev.feature.discounts.domain.usecase.GetNearbyDiscountsUseCase
import dev.feature.discounts.domain.usecase.DeleteListingUseCase
import dev.feature.discounts.domain.usecase.GetListingUseCase
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
 * Barcha ma'lumot **backenddan** keladi (OpenAPI'dan generatsiya qilingan klient orqali) —
 * Firestore faqat ilovaga kirish uchun qoladi. Backend javob bermasa har bir oqim o'z
 * zaxirasiga tushadi (katalog → `ListingCatalog`, biznes → `FakeBusinesses`, e'lon → local
 * baza), shuning uchun rejimni tanlaydigan bayroq kerak emas.
 */
fun discountsModule() = module {

    single { ListingsApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { MediaApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { BusinessApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { BranchesApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }

    // Katalog (biznes turlari + kategoriyalar) — backenddan. Backend javob bermasa
    // UseCase klientдаgi ListingCatalog'ga qaytadi, shuning uchun bayroq kerak emas.
    single<CatalogRepository> {
        if (USE_LOCAL_DATA) LocalCatalogRepository() else ApiCatalogRepository(get(), get())
    }
    factory { GetBusinessTypesUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }

    // E'lonlar backendда (`POST /business/{id}/listings` + `/submit`, rasm `/media/upload`).
    // Backend javob bermasa — local zaxira: e'lon darrov faol, rasm `data:` URI.
    single<ListingRemoteDataSource> {
        FallbackListingRemoteDataSource(
            api = ApiListingRemoteDataSource(get(), get()),
            local = LocalListingRemoteDataSource(),
        )
    }

    // Offline-first: UI local bazani kuzatadi, masofaviy manba publish/upload uchun.
    single<ListingRepository> { ListingRepositoryImpl(get(), get(), get()) }

    // Talaba qidiruvi — `GET /discounts`. Backend javob bermasa UseCase local faol
    // e'lonlarga qaytadi.
    single { DiscountsApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single<DiscountFeedRepository> {
        if (USE_LOCAL_DATA) LocalDiscountFeedRepository() else ApiDiscountFeedRepository(get(), get())
    }
    factory { GetNearbyDiscountsUseCase(get(), get()) }

    // Biznes (nom, telefon, tur, filiallar) — backend `/business` + `/business/{id}/branches`.
    // Backend javob bermasa UseCase namuna ma'lumotga qaytadi, shuning uchun bayroq kerak emas.
    single<dev.feature.discounts.domain.repository.BusinessRepository> {
        if (USE_LOCAL_DATA) LocalBusinessRepository(get())
        else ApiBusinessRepository(get(), get(), get())
    }
    factory { dev.feature.discounts.domain.usecase.ObserveMyBusinessesUseCase(get()) }
    factory { dev.feature.discounts.domain.usecase.GetBusinessUseCase(get()) }
    factory { dev.feature.discounts.domain.usecase.SaveBusinessUseCase(get()) }
    factory { dev.feature.discounts.domain.usecase.DeleteBusinessUseCase(get()) }
    viewModelOf(::MyBusinessesViewModel)
    viewModelOf(::AddBusinessViewModel)

    single { GeoApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }

    // Geokodlash — backend (`/geo/geocode`, `/geo/reverse-geocode`), u ishlamasa Nominatim.
    // Nominatim'ga ilovaning umumiy klienti berilmaydi: unda Firebase Bearer tokeni bor,
    // uni begona serverga yuborib bo'lmaydi.
    single<GeoRepository> {
        FallbackGeoRepository(
            api = ApiGeoRepository(get(), get()),
            nominatim = NominatimGeoRepository(createPublicHttpClient()),
        )
    }

    factory { CreateBranchFromPointUseCase(get()) }
    factory { SearchPlacesUseCase(get()) }
    factory { ObserveMyListingsUseCase(get()) }
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
