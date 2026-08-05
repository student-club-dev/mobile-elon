package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.core.common.error.toAppException
import dev.core.common.errorOf
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow

/**
 * Biznes UseCase'lari — **faqat backend**.
 *
 * Namuna ma'lumot yo'q: ilgari xatoда `FakeBusinesses` ro'yxati ("Bon Appetit", "Cyber Arena")
 * chiqar va foydalanuvchi uni o'z biznesi deb o'ylardi — aslida sessiya tugagan yoki internet
 * yo'q bo'lardi. Endi xato yuqoriga uzatiladi va ekran uni ko'rsatadi.
 */

/** Joriy foydalanuvchi bizneslari ro'yxati (bosh ekran). Xato oqimда uzatiladi. */
class ObserveMyBusinessesUseCase(private val repository: BusinessRepository) {
    operator fun invoke(): Flow<List<Business>> = repository.observeMine()
}

/**
 * Bitta biznesni id bo'yicha oladi (e'lon yuklashda meros olish uchun).
 *
 * `null` — chaqiruvchi xato ko'rsatadi va qayta urinish taklif qiladi.
 */
class GetBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(id: String): Business? =
        runCatching { repository.byId(id) }.getOrNull()
}

/**
 * Biznesни yaratadi/yangilaydi (nom, telefon, tur, lokatsiya).
 *
 * Zaxira **yo'q**: ilgari backend javob bermasa forma "saqlandi" deb yopilar, serverда esa
 * biznes yaratilmagan bo'lardi.
 */
class SaveBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(business: Business): Resource<Business> =
        runCatching { repository.save(business) }.getOrElse { errorOf(it.toAppException()) }
}

/**
 * Biznesni moderatsiyaga yuboradi.
 *
 * Zaxira **yo'q** (`SaveBusinessUseCase` dan farqi shu): "yuborildi" deb ko'rsatib, serverда
 * hech narsa o'zgarmagan bo'lsa foydalanuvchi tekshiruvni kutib o'tirardi. Xato uning
 * o'ziga ko'rinishi kerak.
 *
 * Holat oldindan tekshiriladi: backend faqat `DRAFT`/`REJECTED` dan o'tkazadi va boshqasidan
 * `409` qaytaradi — bu tekshiruv shu keraksiz so'rovni oldini oladi.
 */
class SubmitBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(business: Business): Resource<Business> {
        val status = business.status
        if (status != null && !status.canSubmit) {
            return Resource.Error("Bu holatda yuborib bo'lmaydi: ${status.label}")
        }
        return runCatching { repository.submit(business.id) }
            .getOrElse { errorOf(it.toAppException()) }
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

/**
 * Biznes logosini yuklaydi va ochiq URL qaytaradi.
 *
 * Hajm **klientда** tekshiriladi (backend chegarasi ham 5 MB): katta faylni yuklab, keyin
 * `413` olib kutib o'tirish foydalanuvchi uchun bekorga sarflangan trafik va vaqt.
 */
class UploadBusinessLogoUseCase(private val repository: BusinessRepository) {

    suspend operator fun invoke(bytes: ByteArray, fileName: String): Resource<String> {
        if (bytes.isEmpty()) return Resource.Error("Rasm bo'sh")
        if (bytes.size > MAX_LOGO_BYTES) return Resource.Error("Rasm juda katta (maks. 5 MB)")
        return runCatching { repository.uploadLogo(bytes, fileName) }
            .getOrElse { errorOf(it.toAppException()) }
    }

    companion object {
        const val MAX_LOGO_BYTES = 5 * 1024 * 1024
    }
}
