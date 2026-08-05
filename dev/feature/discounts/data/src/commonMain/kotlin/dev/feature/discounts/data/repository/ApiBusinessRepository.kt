package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.errorOf
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.BranchesApi
import dev.core.network.generated.api.BusinessApi
import dev.core.network.generated.model.BranchDto
import dev.core.network.generated.model.BranchRequestDto
import dev.core.network.generated.model.BranchTradeCenterFieldInputDto
import dev.core.network.generated.model.BusinessDto
import dev.core.network.generated.model.CreateBusinessDto
import dev.core.network.generated.model.DayOfWeekDto
import dev.core.network.generated.model.LocationDto
import dev.core.network.generated.model.UpdateBusinessDto
import dev.core.network.generated.model.WorkingHoursDto
import dev.feature.discounts.domain.model.BranchWorkingHours
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessStatus
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.WeekDay
import dev.feature.discounts.domain.repository.BusinessRepository
import io.ktor.client.call.body
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Biznes repository'sining **backend** implementatsiyasi.
 *
 * Biznes va filiallar backendда alohida resurslar (`/v1/business` va
 * `/v1/business/{id}/branches`), lekin domen [Business] ularni bitta model sifatida
 * ko'radi — shuning uchun bu yerда ikkala API birlashtiriladi.
 *
 * Xato **yutilmaydi** — [Resource.Error] qaytadi va UseCase zaxira ma'lumotga o'tadi
 * ([ApiCatalogRepository] bilan bir xil naqsh).
 */
class ApiBusinessRepository(
    private val businessApi: BusinessApi,
    private val branchesApi: BranchesApi,
    private val connectivity: NetworkConnectivity,
) : BusinessRepository {

    /**
     * REST'да obuna yo'q — ro'yxat bir marta o'qiladi va emit qilinadi. Ekran yangilanishi
     * kerak bo'lsa flow qayta yig'iladi (real-time manba'dan farqi shu).
     *
     * **Filiallar bu yerda so'ralmaydi.** Ilgari har bir biznes uchun alohida
     * `GET /business/{id}/branches` ketardi (N ta biznes → 1 + N so'rov), holbuki ro'yxatda
     * filial kerak emas. Ular biznes ochilganda — [byId] da — bir marta olinadi.
     *
     * Xato **yutilmaydi**: flow uni tashqariga chiqaradi, UseCase esa zaxiraga o'tadi.
     * Bo'sh ro'yxat — haqiqiy javob (foydalanuvchida hali biznes yo'q), xato emas.
     */
    override fun observeMine(): Flow<List<Business>> = flow {
        val businesses: List<BusinessDto> = businessApi.getMy().body()
        emit(businesses.map { it.toDomain() }.sortedByDescending { it.createdAt })
    }

    /**
     * Bitta biznes — **shu yerda** filiallari bilan birga olinadi: e'lon formasi ularni
     * `branchIds` uchun talab qiladi, biznes tahrirlash esa xaritada ko'rsatadi.
     */
    override suspend fun byId(id: String): Business? = try {
        val dto: BusinessDto = businessApi.getById(id).body()
        dto.toDomain(branchesOf(id))
    } catch (e: Exception) {
        null
    }

    override suspend fun save(business: Business): Resource<Business> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        val type = business.businessType
            ?: return errorOf(AppException.Validation("Biznes turini tanlang"))

        return try {
            // Tur faqat yaratishда beriladi — backend uni keyin o'zgartirmaydi (BUSINESS_TYPE_IMMUTABLE).
            val saved: BusinessDto = if (business.id.isBlank()) {
                businessApi.businessCreate(
                    CreateBusinessDto(
                        type = type.key,
                        name = business.name,
                        phone = business.phone,
                    ),
                ).body()
            } else {
                businessApi.businessUpdate(
                    business.id,
                    UpdateBusinessDto(name = business.name, phone = business.phone),
                ).body()
            }

            val branches = syncBranches(saved.id, business.branches)
            Resource.Success(saved.toDomain(branches))
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    /**
     * Moderatsiyaga yuboradi. Filiallar bilan birga qaytariladi, chunki chaqiruvchi ekran
     * ro'yxatdagi kartani shu javob bilan almashtiradi — filialsiz qaytarsak karta ochilgan
     * biznesning manzilini yo'qotardi.
     */
    override suspend fun submit(id: String): Resource<Business> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            val submitted: BusinessDto = businessApi.businessSubmit(id).body()
            Resource.Success(submitted.toDomain(branchesOf(id)))
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    /** Backend fizik o'chirmaydi — biznes va uning e'lonlari arxivlanadi. */
    override suspend fun delete(id: String): Resource<Unit> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            businessApi.businessArchive(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    /**
     * Filiallar ro'yxati. **Xato yutilmaydi** — chaqiruvchi uni ko'radi.
     *
     * Ilgari bu yerda `catch { emptyList() }` turardi va oqibati og'ir edi: so'rov bir marta
     * uzilsa, e'lon formasi biznesni "filialsiz" deb ochar, validator esa "Kamida bitta
     * filialni belgilang" deb e'lonni to'sib qo'yardi — foydalanuvchi uchun chiqish yo'li
     * yo'q edi (filial bu ekranda yaratilmaydi). Endi [byId] `null` qaytaradi va forma
     * "yuklab bo'lmadi" + "Qayta urinish" ko'rsatadi.
     *
     * Bo'sh ro'yxat esa **haqiqiy javob**: onlayn biznesda (`isOnlineOnly`) filial bo'lmaydi.
     */
    private suspend fun branchesOf(businessId: String): List<BranchDto> =
        branchesApi.branchesList(businessId).body()

    /**
     * Filiallarni backend holatiga keltiradi: yangisi yaratiladi, mavjudi yangilanadi,
     * formadan olib tashlangani o'chiriladi. Domen bitta ro'yxat beradi — farqni shu yer topadi.
     */
    private suspend fun syncBranches(businessId: String, wanted: List<ListingBranch>): List<BranchDto> {
        val existing = branchesOf(businessId).associateBy { it.id }

        val saved = wanted.map { branch ->
            val request = branch.toRequest()
            if (branch.id.isNotBlank() && existing.containsKey(branch.id)) {
                branchesApi.branchesUpdate(businessId, branch.id, request).body()
            } else {
                branchesApi.branchesCreate(businessId, request).body()
            }
        }

        (existing.keys - saved.map { it.id }.toSet()).forEach { removed ->
            runCatching { branchesApi.delete(businessId, removed) }
        }
        return saved
    }
}

// ---------------------------------------------------------------------------
// Mapper'lar — DTO ↔ domen
// ---------------------------------------------------------------------------

/**
 * [branches] — faqat biznes **ochilganda** beriladi. Ro'yxatda u bo'sh qoladi va bu normal:
 * `Business.branches` "hali so'ralmagan" degani, "filial yo'q" degani emas.
 */
private fun BusinessDto.toDomain(branches: List<BranchDto> = emptyList()) = Business(
    id = id,
    ownerId = ownerUserId,
    name = name,
    phone = phone,
    // Kalit ochiq — serverdagi istalgan tur o'zgarishsiz o'tadi (ilova ro'yxati bilan
    // cheklanmaydi). Bo'sh qiymat esa "tur ko'rsatilmagan" degani.
    businessType = type.takeIf { it.isNotBlank() }?.let(::BusinessType),
    branches = branches.map { it.toDomain() },
    isOnlineOnly = isOnlineOnly,
    // Serverdagi e'lonlar soni — `/business/my` javobining o'zida keladi, qo'shimcha
    // so'rovsiz. Kartada shu ko'rsatiladi (filial manzili o'rniga, u endi so'ralmaydi).
    listingsCount = listingsCount,
    // Moderatsiya holati — biznes ochilganda ("Mening e'lonlarim" sarlavhasi) ko'rsatiladi.
    status = BusinessStatus.fromKey(status.value),
    rejectionReason = rejectionReason?.takeIf { it.isNotBlank() },
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = createdAt.toEpochMilliseconds(),
)

private fun BranchDto.toDomain() = ListingBranch(
    id = id,
    lat = location.lat,
    lng = location.lng,
    address = location.address,
    name = name,
    landmark = location.landmark,
    regionId = location.regionId,
    districtId = location.districtId,
    metroStation = location.metroStation,
    tradeCenterId = tradeCenter?.id,
    tradeCenterName = tradeCenter?.name,
    tradeCenterFields = tradeCenterFields.associate { it.label to it.value },
    workingHours = workingHours.mapNotNull { it.toDomain() },
)

/**
 * Domen filiali → so'rov.
 *
 * Ish vaqti **yetti kunni ham** o'z ichiga oladi (spec §3.2): forma odatiy jadval bilan
 * ochiladi, shuning uchun bu yerда ro'yxat hech qachon bo'sh qolmaydi — eski, ish vaqtisiz
 * saqlangan filial uchun [BranchWorkingHours.fullWeek] yetishmagan kunlarni to'ldiradi.
 *
 * Savdo markazi maydonlari domenда `label → qiymat` ko'rinishida yotadi, backend esa
 * `fieldId` kutadi: shuning uchun forma to'ldirganда id saqlangan bo'lsa o'sha ishlatiladi.
 */
private fun ListingBranch.toRequest() = BranchRequestDto(
    name = name.orEmpty(),
    location = LocationDto(
        regionId = regionId.orEmpty(),
        districtId = districtId.orEmpty(),
        address = address,
        lat = lat,
        lng = lng,
        landmark = landmark,
        // Bo'sh matn yuborilmaydi — backend uchun "bekat ko'rsatilmagan" `null` bilan
        // ifodalanadi, bo'sh satr esa keyin ro'yxatda bo'sh mo'ljal bo'lib ko'rinardi.
        metroStation = metroStation?.takeIf { it.isNotBlank() },
    ),
    workingHours = BranchWorkingHours.fullWeek(workingHours).map { it.toDto() },
    tradeCenterId = tradeCenterId,
    tradeCenterFields = tradeCenterFieldIds.map { (fieldId, value) ->
        BranchTradeCenterFieldInputDto(fieldId = fieldId, value = value)
    },
)

/** Yopiq kunда `open`/`close` yuborilmaydi — backend `null` kutadi (spec §3.2). */
private fun BranchWorkingHours.toDto() = WorkingHoursDto(
    day = DayOfWeekDto.entries.first { it.value == day.name },
    isClosed = isClosed,
    open = open.takeUnless { isClosed },
    close = close.takeUnless { isClosed },
)

/** Noma'lum kun nomi kelsa yozuv tashlab yuboriladi — qolgan kunlar yo'qolmaydi. */
private fun WorkingHoursDto.toDomain(): BranchWorkingHours? {
    val weekDay = WeekDay.entries.firstOrNull { it.name == day.value } ?: return null
    return BranchWorkingHours(day = weekDay, open = open, close = close, isClosed = isClosed)
}
