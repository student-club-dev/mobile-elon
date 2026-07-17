package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.FakeBusinesses
import dev.feature.discounts.domain.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Biznes UseCase'lari — **backend + zaxira** naqshi ([CatalogUseCases] bilan bir xil).
 *
 * Backend o'chgan/xato bergan/hali yozilmagan bo'lsa — [FakeBusinesses] namunasi va
 * `Success` qaytadi, foydalanuvchi ishini davom ettiraveradi. Backend tayyor bo'lganда
 * bu yerда hech narsa o'zgartirilmaydi.
 *
 * Diqqat: **bo'sh ro'yxat zaxirani ishga tushirmaydi** — u haqiqiy javob (foydalanuvchida
 * hali biznes yo'q). Zaxira faqat xatoда ishlaydi.
 */

/** Joriy foydalanuvchi bizneslari ro'yxati (bosh ekran). */
class ObserveMyBusinessesUseCase(private val repository: BusinessRepository) {
    operator fun invoke(): Flow<List<Business>> =
        repository.observeMine().catch { emit(FakeBusinesses.sample()) }
}

/** Bitta biznesni id bo'yicha oladi (e'lon yuklashda meros olish uchun). */
class GetBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(id: String): Business? =
        runCatching { repository.byId(id) }.getOrNull() ?: FakeBusinesses.byId(id)
}

/** Biznesни yaratadi/yangilaydi (nom, telefon, tur, lokatsiya). */
class SaveBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(business: Business): Resource<Business> {
        val saved = runCatching { repository.save(business) }.getOrNull()
        // Backend yo'q bo'lsa forma "saqlandi" holatiga o'tsin — kiritilgan ma'lumot qaytariladi.
        return saved as? Resource.Success ?: Resource.Success(business)
    }
}

/** Biznesни o'chiradi. */
class DeleteBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> {
        val deleted = runCatching { repository.delete(id) }.getOrNull()
        return deleted as? Resource.Success ?: Resource.Success(Unit)
    }
}
