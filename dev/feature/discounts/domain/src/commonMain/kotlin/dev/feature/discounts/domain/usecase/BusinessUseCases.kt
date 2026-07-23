package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.core.common.error.toAppException
import dev.core.common.errorOf
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

/**
 * Bitta biznesni id bo'yicha oladi (e'lon yuklashda meros olish uchun).
 *
 * ⚠️ Bu yerda zaxira **yo'q**, boshqa UseCase'lardan farqli. Sababi — id: namuna biznesning
 * id'si boshqa (`fake-cafe`), shuning uchun xatoда uni qaytarish e'lon formasini **mavjud
 * bo'lmagan biznesga** bog'lab qo'yardi va `POST /business/fake-cafe/listings` 404 bilan
 * tugardi. Foydalanuvchi esa buni "e'lon saqlanmadi" deb ko'rardi.
 *
 * `null` — chaqiruvchi xato ko'rsatadi va qayta urinish taklif qiladi.
 */
class GetBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(id: String): Business? =
        runCatching { repository.byId(id) }.getOrNull()
}

/** Biznesни yaratadi/yangilaydi (nom, telefon, tur, lokatsiya). */
class SaveBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(business: Business): Resource<Business> {
        val saved = runCatching { repository.save(business) }.getOrNull()
        // Backend yo'q bo'lsa forma "saqlandi" holatiga o'tsin — kiritilgan ma'lumot qaytariladi.
        return saved as? Resource.Success ?: Resource.Success(business)
    }
}

/**
 * Biznesни o'chiradi (backend uni arxivlaydi).
 *
 * Xato **yutilmaydi**: ilgari har qanday holatda `Success` qaytardi va ro'yxat yangilanganda
 * o'chirilmagan biznes qaytib chiqar, foydalanuvchi esa sababini bilmасdi.
 */
class DeleteBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> =
        runCatching { repository.delete(id) }
            .getOrElse { errorOf(it.toAppException()) }
}
