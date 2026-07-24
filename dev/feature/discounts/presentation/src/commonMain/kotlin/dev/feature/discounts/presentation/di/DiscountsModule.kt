package dev.feature.discounts.presentation.di

import dev.core.domain.USE_LOCAL_DATA
import dev.core.network.NetworkConfig
import dev.core.network.createPublicHttpClient
import dev.core.network.generated.api.BranchesApi
import dev.core.network.generated.api.BusinessApi
import dev.core.network.generated.api.CatalogApi
import dev.core.network.generated.api.GeoApi
import dev.core.network.generated.api.ListingsApi
import dev.core.network.generated.api.TradeCentersApi
import dev.feature.discounts.data.remote.ApiGeoRepository
import dev.feature.discounts.data.remote.ApiListingRemoteDataSource
import dev.feature.discounts.data.remote.FallbackGeoRepository
import dev.feature.discounts.data.remote.FallbackListingRemoteDataSource
import dev.feature.discounts.data.remote.ListingRemoteDataSource
import dev.feature.discounts.data.remote.LocalListingRemoteDataSource
import dev.feature.discounts.data.remote.NominatimGeoRepository
import dev.feature.discounts.data.repository.ApiBusinessRepository
import dev.feature.discounts.data.repository.ApiCatalogRepository
import dev.feature.discounts.data.repository.ApiRegionRepository
import dev.feature.discounts.data.repository.ApiTradeCenterRepository
import dev.feature.discounts.data.repository.ListingRepositoryImpl
import dev.feature.discounts.data.repository.LocalBusinessRepository
import dev.feature.discounts.data.repository.LocalCatalogRepository
import dev.feature.discounts.data.repository.LocalDiscountFeedRepository
import dev.feature.discounts.domain.repository.BusinessRepository
import dev.feature.discounts.domain.repository.CatalogRepository
import dev.feature.discounts.domain.repository.DiscountFeedRepository
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.ListingRepository
import dev.feature.discounts.domain.repository.RegionRepository
import dev.feature.discounts.domain.repository.TradeCenterRepository
import dev.feature.discounts.domain.usecase.CreateBranchFromPointUseCase
import dev.feature.discounts.domain.usecase.DeleteBusinessUseCase
import dev.feature.discounts.domain.usecase.DeleteListingUseCase
import dev.feature.discounts.domain.usecase.GetBusinessListingsUseCase
import dev.feature.discounts.domain.usecase.GetBusinessTypesUseCase
import dev.feature.discounts.domain.usecase.GetBusinessUseCase
import dev.feature.discounts.domain.usecase.GetCategoriesUseCase
import dev.feature.discounts.domain.usecase.GetListingUseCase
import dev.feature.discounts.domain.usecase.GetNearbyDiscountsUseCase
import dev.feature.discounts.domain.usecase.GetTradeCenterDetailUseCase
import dev.feature.discounts.domain.usecase.GetTradeCentersUseCase
import dev.feature.discounts.domain.usecase.ObserveMyBusinessesUseCase
import dev.feature.discounts.domain.usecase.ObserveMyListingsUseCase
import dev.feature.discounts.domain.usecase.PublishListingUseCase
import dev.feature.discounts.domain.usecase.SaveBusinessUseCase
import dev.feature.discounts.domain.usecase.SaveDraftUseCase
import dev.feature.discounts.domain.usecase.SearchPlacesUseCase
import dev.feature.discounts.domain.usecase.ToggleListingPausedUseCase
import dev.feature.discounts.domain.usecase.UploadListingImageUseCase
import dev.feature.discounts.presentation.AddBusinessViewModel
import dev.feature.discounts.presentation.MyBusinessesViewModel
import dev.feature.discounts.presentation.MyListingsViewModel
import dev.feature.discounts.presentation.NearbyDiscountsViewModel
import dev.feature.discounts.presentation.PostListingViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Chegirmalar feature'ining barcha qatlamlarini bog'laydi (domain / data / presentation).
 *
 * Barcha ma'lumot **backenddan** keladi (OpenAPI'dan generatsiya qilingan klient orqali).
 * Backend javob bermasa har bir oqim o'z zaxirasiga tushadi (katalog → `ListingCatalog`,
 * biznes → `FakeBusinesses`, e'lon → local baza), shuning uchun rejimni tanlaydigan bayroq
 * kerak emas.
 */
fun discountsModule() = module {

    single { ListingsApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { BusinessApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { BranchesApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { CatalogApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { TradeCentersApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }
    single { GeoApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }

    // Viloyat/tuman ma'lumotnomasi — serverdan (xato bo'lsa klientdagi `GeoCatalog`).
    single<RegionRepository> { ApiRegionRepository(get()) }

    // Katalog (biznes turlari + kategoriyalar) — backenddan. Backend javob bermasa
    // UseCase klientдаgi ListingCatalog'ga qaytadi, shuning uchun bayroq kerak emas.
    single<CatalogRepository> {
        if (USE_LOCAL_DATA) LocalCatalogRepository() else ApiCatalogRepository(get(), get())
    }
    factory { GetBusinessTypesUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }

    // Savdo markazlari — filial shu markaz ichida bo'lsa, uning dinamik maydonlari
    // (`GET /v1/trade-centers/{id}`) formaга qo'shiladi.
    single<TradeCenterRepository> { ApiTradeCenterRepository(get(), get()) }
    factory { GetTradeCentersUseCase(get()) }
    factory { GetTradeCenterDetailUseCase(get()) }

    // E'lonlar backendда (`POST /v1/business/{id}/listings` + `/submit`, rasm `/v1/media/upload`).
    // Backendga YETIB BO'LMASA — local zaxira: e'lon darrov faol, rasm `data:` URI. Server rad
    // etsa xato yutilmaydi (qarang FallbackListingRemoteDataSource).
    single<ListingRemoteDataSource> {
        FallbackListingRemoteDataSource(
            api = ApiListingRemoteDataSource(get(), get(), get()),
            local = LocalListingRemoteDataSource(),
        )
    }

    // Offline-first: UI local bazani kuzatadi, masofaviy manba publish/upload uchun.
    single<ListingRepository> { ListingRepositoryImpl(get(), get(), get()) }

    // Talaba qidiruvi — yangi spec'da chegirma feed'i endpoint'i yo'q (bu biznes ilovasi),
    // shuning uchun local manba: UseCase local faol e'lonlarga qaytadi.
    single<DiscountFeedRepository> { LocalDiscountFeedRepository() }
    factory { GetNearbyDiscountsUseCase(get(), get()) }

    // Biznes (nom, telefon, tur, filiallar) — backend `/v1/business` + `.../branches`.
    // Backend javob bermasa UseCase namuna ma'lumotga qaytadi, shuning uchun bayroq kerak emas.
    single<BusinessRepository> {
        if (USE_LOCAL_DATA) LocalBusinessRepository(get())
        else ApiBusinessRepository(get(), get(), get())
    }
    factory { ObserveMyBusinessesUseCase(get()) }
    factory { GetBusinessUseCase(get()) }
    factory { SaveBusinessUseCase(get()) }
    factory { DeleteBusinessUseCase(get()) }
    viewModelOf(::MyBusinessesViewModel)
    viewModelOf(::AddBusinessViewModel)

    // Geokodlash — backend (`/v1/geo/geocode`, `/v1/geo/reverse-geocode`), u ishlamasa Nominatim.
    // Nominatim'ga ilovaning umumiy klienti berilmaydi: unda sessiya tokeni bor, uni begona
    // serverga yuborib bo'lmaydi.
    //
    // Local rejimda backend UMUMAN chaqirilmaydi: aks holda xaritadan joy tanlashda har safar
    // mavjud bo'lmagan serverga so'rov ketib, timeout kutilardi va manzil bir necha soniyadan
    // keyin chiqardi. Qolgan repository'lar ham shu bayroqqa amal qiladi.
    single<GeoRepository> {
        val nominatim = NominatimGeoRepository(createPublicHttpClient())
        if (USE_LOCAL_DATA) nominatim
        else FallbackGeoRepository(api = ApiGeoRepository(get(), get()), nominatim = nominatim)
    }

    factory { CreateBranchFromPointUseCase(get()) }
    factory { SearchPlacesUseCase(get()) }
    factory { ObserveMyListingsUseCase(get()) }
    factory { GetBusinessListingsUseCase(get()) }
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
