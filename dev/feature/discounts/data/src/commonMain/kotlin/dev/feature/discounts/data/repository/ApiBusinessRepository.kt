package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.BranchesApi
import dev.core.network.generated.api.BusinessApi
import dev.core.network.generated.model.BranchDto
import dev.core.network.generated.model.BranchRequestDto
import dev.core.network.generated.model.BusinessDto
import dev.core.network.generated.model.CreateBusinessRequestDto
import dev.core.network.generated.model.LocationDto
import dev.core.network.generated.model.UpdateBusinessRequestDto
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.repository.BusinessRepository
import io.ktor.client.call.body
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Biznes repository'sining **backend** implementatsiyasi.
 *
 * Biznes va filiallar backendда alohida resurslar (`/business` va `/business/{id}/branches`),
 * lekin domen [Business] ularni bitta model sifatida ko'radi — shuning uchun bu yerда
 * ikkala API birlashtiriladi.
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
     * kerak bo'lsa flow qayta yig'iladi (Firestore `.snapshots` real-time'idan farqi shu).
     *
     * Xato **yutilmaydi**: flow uni tashqariga chiqaradi, UseCase esa zaxiraga o'tadi.
     * Bo'sh ro'yxat — haqiqiy javob (foydalanuvchida hali biznes yo'q), xato emas.
     */
    override fun observeMine(): Flow<List<Business>> = flow {
        val businesses: List<BusinessDto> = businessApi.getMyBusinesses().body()
        emit(businesses.map { it.toDomain(branchesOf(it.id)) }.sortedByDescending { it.createdAt })
    }

    override suspend fun byId(id: String): Business? = try {
        val dto: BusinessDto = businessApi.getBusiness(id).body()
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
                businessApi.createBusiness(
                    CreateBusinessRequestDto(
                        type = type.name,
                        name = business.name,
                        phone = business.phone,
                    ),
                ).body()
            } else {
                businessApi.updateBusiness(
                    business.id,
                    UpdateBusinessRequestDto(name = business.name, phone = business.phone),
                ).body()
            }

            val branches = syncBranches(saved.id, business.branches)
            Resource.Success(saved.toDomain(branches))
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    /** Backend fizik o'chirmaydi — biznes va uning e'lonlari arxivlanadi. */
    override suspend fun delete(id: String): Resource<Unit> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            businessApi.archiveBusiness(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    private suspend fun branchesOf(businessId: String): List<BranchDto> = try {
        branchesApi.getBranches(businessId).body()
    } catch (e: Exception) {
        emptyList()
    }

    /**
     * Filiallarni backend holatiga keltiradi: yangisi yaratiladi, mavjudi yangilanadi,
     * formadan olib tashlangani o'chiriladi. Domen bitta ro'yxat beradi — farqni shu yer topadi.
     */
    private suspend fun syncBranches(businessId: String, wanted: List<ListingBranch>): List<BranchDto> {
        val existing = branchesOf(businessId).associateBy { it.id }

        val saved = wanted.map { branch ->
            val request = branch.toRequest()
            if (branch.id.isNotBlank() && existing.containsKey(branch.id)) {
                branchesApi.updateBranch(businessId, branch.id, request).body()
            } else {
                branchesApi.createBranch(businessId, request).body()
            }
        }

        (existing.keys - saved.map { it.id }.toSet()).forEach { removed ->
            runCatching { branchesApi.deleteBranch(businessId, removed) }
        }
        return saved
    }
}

// ---------------------------------------------------------------------------
// Mapper'lar — DTO ↔ domen
// ---------------------------------------------------------------------------

private fun BusinessDto.toDomain(branches: List<BranchDto>) = Business(
    id = id,
    ownerId = ownerUserId.orEmpty(),
    name = name,
    phone = phone,
    businessType = BusinessType.entries.firstOrNull { it.name == type },
    branches = branches.map { it.toDomain() },
    createdAt = createdAt?.toEpochMilliseconds() ?: 0L,
    updatedAt = createdAt?.toEpochMilliseconds() ?: 0L,
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
)

/**
 * Domen filiali → so'rov. Ish vaqti hozircha formaда yo'q — backend bo'sh ro'yxatni
 * "belgilanmagan" deb qabul qiladi.
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
    ),
    workingHours = emptyList(),
)
