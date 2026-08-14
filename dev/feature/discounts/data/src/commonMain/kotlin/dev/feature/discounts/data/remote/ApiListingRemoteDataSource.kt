package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.errorOf
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.ListingsApi
import dev.core.network.generated.api.RedemptionsApi
import dev.core.network.media.MediaPurpose
import dev.core.network.media.MediaUploader
import dev.core.network.response.safeCall
import dev.core.network.generated.model.ConfirmRedemptionRequestDto
import dev.core.network.generated.model.CreateListingRequestDto
import dev.core.network.generated.model.DiscountDto
import dev.core.network.generated.model.DiscountRequestDto
import dev.core.network.generated.model.DiscountTypeDto
import dev.core.network.generated.model.ListingDto
import dev.core.network.generated.model.ListingStatusDto
import dev.core.network.generated.model.OptionDto
import dev.core.network.generated.model.OptionGroupDto
import dev.core.network.generated.model.PriceUnitDto
import dev.core.network.generated.model.RedemptionDto
import dev.core.network.generated.model.RedemptionInfoDto
import dev.core.network.generated.model.RedemptionMethodDto
import dev.core.network.generated.model.RedemptionPeriodDto
import dev.core.network.generated.model.SelectionTypeDto
import dev.core.network.generated.model.UpdateListingRequestDto
import dev.core.network.generated.model.VerifyRedemptionRequestDto
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.ListingStats
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.OptionGroup
import dev.feature.discounts.domain.model.OptionItem
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.Redemption
import dev.feature.discounts.domain.model.RedemptionCheck
import dev.feature.discounts.domain.model.RedemptionInvalidReason
import dev.feature.discounts.domain.model.RedemptionPage
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.RedemptionPeriod
import dev.feature.discounts.domain.model.SelectionType
import kotlinx.datetime.Instant

/**
 * Real backend — spetsifikatsiya: `dev/api-client-generator/elon-uz.json`
 * (`Listings`, `Media` tag'lari), hujjat: `DISCOUNTS_BUSINESS_API.md`.
 *
 * Ikki qadam: `POST /business/{id}/listings` (DRAFT yaratadi) → `POST /listings/{id}/submit`
 * (moderatsiyaga yuboradi). Yakuniy narxni **server** hisoblaydi, shuning uchun bu yerda
 * `finalPrice` yuborilmaydi.
 *
 * Xatolar `safeCall` orqali **typed** [AppException] ga aylanadi (422 dagi maydon xatolari ham),
 * shuning uchun UI "internet yo'q" ni "server rad etdi" dan ajrata oladi.
 */
class ApiListingRemoteDataSource(
    private val listingsApi: ListingsApi,
    /**
     * Kassir oqimi va foydalanishlar tarixi backendда alohida kontrollerda
     * (`Redemptions` tag'i), shuning uchun generatsiya qilingan klientда ham alohida klass.
     */
    private val redemptionsApi: RedemptionsApi,
    private val mediaApi: MediaUploader,
    private val connectivity: NetworkConnectivity,
) : ListingRemoteDataSource {

    override suspend fun list(
        business: Business,
        status: ListingStatus?,
        categoryKey: String?,
        page: Int,
        size: Int,
    ): Resource<ListingPage> = safeCall(connectivity) {
        val dto = listingsApi.listingsList(
            businessId = business.id,
            status = status?.let { s -> ListingStatusDto.entries.firstOrNull { it.value == s.name } },
            categoryKey = categoryKey,
            page = page,
            size = size,
        ).body()
        ListingPage(
            items = dto.items.map { it.toDomain(business) },
            page = dto.page,
            size = dto.propertySize,
            total = dto.total,
            hasNext = dto.hasNext,
        )
    }

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

    override suspend fun update(listing: Listing): Resource<Listing> {
        val id = listing.id.ifBlank {
            return errorOf(AppException.Validation("E'lon id'si topilmadi"))
        }
        return safeCall(connectivity) {
            val updated = listingsApi.listingUpdate(id, listing.toUpdateRequest()).body()
            // Server tahrirlashдан keyin statusni o'zgartirishi mumkin (masalan qayta moderatsiyaga).
            listing.copy(
                id = updated.id,
                businessId = updated.businessId,
                status = updated.status.toDomain(),
                rejectionReason = updated.rejectionReason,
            )
        }
    }

    override suspend fun submitExisting(id: String): Resource<ListingStatus> =
        // Faqat serverdagi YANGI holat kerak — e'lonning qolgan maydonlari chaqiruvchida bor.
        safeCall(connectivity) { listingsApi.submit(id).body().status.toDomain() }

    override suspend fun archive(id: String): Resource<Unit> =
        safeCall(connectivity) { listingsApi.listingArchive(id).body() }

    /**
     * Har o'tish o'z endpoint'iga boradi va **serverning javobidagi** statusni qaytaradi:
     * `activate` dan keyin e'lon `ACTIVE` emas, `SCHEDULED` bo'lishi mumkin (boshlanish sanasi
     * kelajakda), `withdraw` esa `DRAFT` beradi. Bu qiymatlarni klientda taxmin qilib bo'lmaydi.
     */
    override suspend fun changeStatus(
        id: String,
        transition: ListingTransition,
    ): Resource<ListingStatus> = safeCall(connectivity) {
        val dto = when (transition) {
            ListingTransition.PAUSE -> listingsApi.pause(id)
            ListingTransition.ACTIVATE -> listingsApi.activate(id)
            ListingTransition.WITHDRAW -> listingsApi.withdraw(id)
        }.body()
        dto.status.toDomain()
    }

    override suspend fun duplicate(id: String, business: Business): Resource<Listing> =
        safeCall(connectivity) { listingsApi.duplicate(id).body().toDomain(business) }

    override suspend fun stats(id: String): Resource<ListingStats> = safeCall(connectivity) {
        val dto = listingsApi.stats(id).body()
        ListingStats(
            listingId = dto.listingId,
            views = dto.viewsCount,
            favorites = dto.favoritesCount,
            redemptions = dto.redemptionsCount,
            conversionRate = dto.conversionRate,
            totalRevenue = dto.totalRevenue,
        )
    }

    override suspend fun redemptions(id: String, page: Int, size: Int): Resource<RedemptionPage> =
        safeCall(connectivity) {
            val dto = redemptionsApi.redemptionsList(listingId = id, page = page, size = size).body()
            RedemptionPage(
                items = dto.items.map { it.toDomain() },
                page = dto.page,
                size = dto.propertySize,
                total = dto.total,
                hasNext = dto.hasNext,
            )
        }

    override suspend fun verifyRedemption(id: String, code: String): Resource<RedemptionCheck> =
        safeCall(connectivity) {
            val dto = redemptionsApi.redeemVerify(id, VerifyRedemptionRequestDto(code = code)).body()
            RedemptionCheck(
                isValid = dto.isValid,
                invalidReason = RedemptionInvalidReason.fromKey(dto.invalidReason?.value),
                studentName = dto.student?.fullName,
                studentUsername = dto.student?.username,
                finalPrice = dto.discount?.finalPrice,
                originalPrice = dto.discount?.originalPrice,
            )
        }

    override suspend fun confirmRedemption(
        id: String,
        code: String,
        branchId: String?,
        amount: Long?,
    ): Resource<Redemption> = safeCall(connectivity) {
        redemptionsApi.redeemConfirm(
            listingId = id,
            confirmRedemptionRequestDto = ConfirmRedemptionRequestDto(
                code = code,
                branchId = branchId,
                amount = amount,
            ),
        ).body().toDomain()
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> =
        safeCall(connectivity) { mediaApi.upload(bytes, fileName, MediaPurpose.LISTING).url }
}

private fun RedemptionDto.toDomain() = Redemption(
    id = id,
    listingId = listingId,
    branchId = branchId,
    studentId = student.id,
    studentName = student.fullName,
    studentUsername = student.username,
    amount = amount,
    redeemedAt = redeemedAt?.toEpochMilliseconds(),
)

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

/**
 * Tahrirlash so'rovi (`PUT /listings/{id}`). [toCreateRequest] bilan bir xil, faqat `currency`
 * yo'q (spec: `UpdateListingRequestDto`da yo'q — u yaratishда belgilanadi va o'zgarmaydi).
 */
internal fun Listing.toUpdateRequest() = UpdateListingRequestDto(
    branchIds = branches.map { it.id },
    categoryKey = categoryKey,
    title = title,
    images = images,
    priceUnit = PriceUnitDto.entries.first { it.value == priceUnit.name },
    originalPrice = originalPrice,
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
    ),
    validFrom = Instant.fromEpochMilliseconds(validFrom),
    validTo = Instant.fromEpochMilliseconds(validTo),
    customCategoryName = customCategoryName,
    description = description,
    attributes = attributes,
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

/**
 * `ListingDto` → domen [Listing]. Server javobida biznes nomi/turi va **to'liq** filiallar
 * yo'q (faqat `branchIds`), shuning uchun ular ochilgan [business] dan olinadi: nom va tur
 * to'g'ridan-to'g'ri, filiallar esa `branchIds` bo'yicha biznes filiallaridan tanlab.
 */
internal fun ListingDto.toDomain(business: Business): Listing {
    val ids = branchIds?.toSet().orEmpty()
    return Listing(
        id = id,
        ownerId = business.ownerId,
        businessId = businessId,
        businessType = business.businessType ?: dev.feature.discounts.domain.model.BusinessType(""),
        businessName = business.name,
        categoryKey = categoryKey,
        customCategoryName = customCategoryName,
        title = title,
        description = description,
        images = images,
        priceUnit = PriceUnit.entries.firstOrNull { it.name == priceUnit.value } ?: PriceUnit.PER_ITEM,
        originalPrice = originalPrice,
        currency = currency,
        discount = discount.toDomain(),
        redemption = redemption?.toDomain() ?: ListingRedemption(),
        // Filiallar biznesnikilaridan tanlanadi; `branchIds` bo'sh — biznesning barcha filiallari (§3.5).
        branches = if (ids.isEmpty()) business.branches else business.branches.filter { it.id in ids },
        validFrom = validFrom.toEpochMilliseconds(),
        validTo = validTo.toEpochMilliseconds(),
        attributes = attributes.orEmpty(),
        optionGroups = optionGroups.orEmpty().map { it.toDomain() },
        status = status.toDomain(),
        rejectionReason = rejectionReason,
        viewsCount = viewsCount,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds(),
    )
}

private fun DiscountDto.toDomain() = ListingDiscount(
    type = DiscountType.entries.firstOrNull { it.name == type.value } ?: DiscountType.PERCENT,
    value = value,
    conditions = conditions,
    appliesToOptions = appliesToOptions,
)

private fun RedemptionInfoDto.toDomain() = ListingRedemption(
    method = RedemptionMethod.entries.firstOrNull { it.name == method.value } ?: RedemptionMethod.STUDENT_ID,
    promoCode = promoCode,
    url = url,
    perUserLimit = perUserLimit,
    perUserPeriod = perUserPeriod?.let { p -> RedemptionPeriod.entries.firstOrNull { it.name == p.value } }
        ?: RedemptionPeriod.DAY,
    totalLimit = totalLimit,
    usedCount = usedCount ?: 0,
)

private fun OptionGroupDto.toDomain() = OptionGroup(
    name = name,
    selectionType = SelectionType.entries.firstOrNull { it.name == selectionType.value } ?: SelectionType.SINGLE,
    isRequired = isRequired ?: false,
    minSelect = minSelect,
    maxSelect = maxSelect,
    // Server `sortOrder` bo'yicha tartibni saqlaymiz (forma ko'rsatgan tartib).
    options = options.sortedBy { it.sortOrder ?: 0 }.map { it.toDomain() },
)

private fun OptionDto.toDomain() = OptionItem(
    name = name,
    priceDelta = priceDelta,
    isAvailable = isAvailable ?: true,
)
