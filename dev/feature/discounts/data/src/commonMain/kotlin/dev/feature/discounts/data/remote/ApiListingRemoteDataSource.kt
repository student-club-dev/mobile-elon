package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.errorOf
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.ListingsApi
import dev.core.network.media.MediaPurpose
import dev.core.network.media.MediaUploader
import dev.core.network.response.safeCall
import dev.core.network.generated.model.CreateListingRequestDto
import dev.core.network.generated.model.DiscountRequestDto
import dev.core.network.generated.model.DiscountTypeDto
import dev.core.network.generated.model.ListingStatusDto
import dev.core.network.generated.model.OptionDto
import dev.core.network.generated.model.OptionGroupDto
import dev.core.network.generated.model.PriceUnitDto
import dev.core.network.generated.model.RedemptionInfoDto
import dev.core.network.generated.model.RedemptionMethodDto
import dev.core.network.generated.model.RedemptionPeriodDto
import dev.core.network.generated.model.SelectionTypeDto
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingStatus
import kotlinx.datetime.Instant

/**
 * Real backend — spetsifikatsiya: `dev/api-client-generator/elon-uz.json`
 * (`Listings`, `Media` tag'lari), hujjat: `DISCOUNTS_BUSINESS_API.md`.
 *
 * Ikki qadam: `POST /business/{id}/listings` (DRAFT yaratadi) → `POST /listings/{id}/submit`
 * (moderatsiyaga yuboradi). Yakuniy narxni **server** hisoblaydi, shuning uchun bu yerda
 * `finalPrice` yuborilmaydi.
 *
 * Xatolar `safeCall` orqali **typed** [AppException] ga aylanadi (422 dagi maydon xatolari ham).
 * Bu shart: [FallbackListingRemoteDataSource] aynan shu turga qarab "backendga yetib bo'lmadi"
 * ni "server rad etdi" dan ajratadi.
 */
class ApiListingRemoteDataSource(
    private val listingsApi: ListingsApi,
    private val mediaApi: MediaUploader,
    private val connectivity: NetworkConnectivity,
) : ListingRemoteDataSource {

    override suspend fun publish(listing: Listing): Resource<Listing> {
        // Backend e'lonni biznesga bog'laydi. Biznes profili hali yaratilmagan bo'lsa,
        // e'lonni jo'natishning ma'nosi yo'q — foydalanuvchini avval biznes ochishga yo'naltiramiz.
        val businessId = listing.businessId
            ?: return errorOf(AppException.Validation("Avval biznes profilini yarating"))

        return safeCall(connectivity) {
            val created = listingsApi.listingsCreate(businessId, listing.toCreateRequest()).body()
            val submitted = listingsApi.submit(created.id).body()
            listing.copy(
                id = submitted.id,
                businessId = submitted.businessId,
                status = submitted.status.toDomain(),
                rejectionReason = submitted.rejectionReason,
            )
        }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> =
        safeCall(connectivity) { mediaApi.upload(bytes, fileName, MediaPurpose.LISTING).url }
}

internal fun Listing.toCreateRequest() = CreateListingRequestDto(
    // Backendda filial alohida obyekt (`POST /business/{id}/branches`) va e'lon unga
    // `branchIds` orqali bog'lanadi. Formada biznesning mavjud filiallaridan tanlanadi —
    // shu sabab bu yerda id'lar allaqachon serverdagi id'lar. Bo'sh ro'yxat spec bo'yicha
    // "biznesning barcha faol filiallari" degani (§3.5).
    branchIds = branches.map { it.id },
    categoryKey = categoryKey,
    title = title,
    images = images,
    priceUnit = PriceUnitDto.entries.first { it.value == priceUnit.name },
    originalPrice = originalPrice,
    // `finalPrice` YO'Q — uni server hisoblaydi (spec §3.5).
    discount = DiscountRequestDto(
        type = DiscountTypeDto.entries.first { it.value == discount.type.name },
        value = discount.value,
        conditions = discount.conditions,
        appliesToOptions = discount.appliesToOptions,
    ),
    redemption = RedemptionInfoDto(
        method = RedemptionMethodDto.entries.first { it.value == redemption.method.name },
        promoCode = redemption.promoCode,
        url = redemption.url,
        perUserLimit = redemption.perUserLimit,
        perUserPeriod = RedemptionPeriodDto.entries.first { it.value == redemption.perUserPeriod.name },
        totalLimit = redemption.totalLimit,
        // `usedCount` YO'Q — uni server hisoblaydi.
    ),
    validFrom = Instant.fromEpochMilliseconds(validFrom),
    validTo = Instant.fromEpochMilliseconds(validTo),
    customCategoryName = customCategoryName,
    description = description,
    currency = currency,
    attributes = attributes,
    // Guruh va variantlar tartibi formadagi tartib bilan bir xil bo'lishi uchun `sortOrder`
    // ro'yxatdagi o'rindan olinadi — backend ularni shu tartibda ko'rsatadi.
    optionGroups = optionGroups.mapIndexed { groupIndex, group ->
        OptionGroupDto(
            name = group.name,
            selectionType = SelectionTypeDto.entries.first { it.value == group.selectionType.name },
            options = group.options.mapIndexed { index, option ->
                OptionDto(
                    name = option.name,
                    priceDelta = option.priceDelta,
                    isAvailable = option.isAvailable,
                    sortOrder = index,
                )
            },
            isRequired = group.isRequired,
            minSelect = group.minSelect,
            maxSelect = group.maxSelect,
            sortOrder = groupIndex,
        )
    },
)

private fun ListingStatusDto.toDomain(): ListingStatus =
    ListingStatus.entries.firstOrNull { it.name == value } ?: ListingStatus.PENDING_REVIEW
