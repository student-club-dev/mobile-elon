package dev.feature.discounts.domain.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import kotlinx.coroutines.flow.Flow

/**
 * Foydalanuvchi bizneslariga egalik qiluvchi repository (Firestore `businesses/{id}`).
 *
 * Bir egaga **bir nechta biznes** bo'lishi mumkin. Bosh ekran [observeMine] ro'yxatini
 * ko'rsatadi; bir biznesga bosilгач — o'sha biznesning e'lonlari ochiladi.
 */
interface BusinessRepository {

    /** Joriy foydalanuvchi bizneslarini real-time kuzatadi (ownerId == uid). */
    fun observeMine(): Flow<List<Business>>

    /** Bitta biznesni id bo'yicha oladi (e'lon yuklashda meros olish uchun). */
    suspend fun byId(id: String): Business?

    /** Biznesni yaratadi (id bo'sh bo'lsa yangi) yoki yangilaydi. */
    suspend fun save(business: Business): Resource<Business>

    /** Biznesни o'chiradi. */
    suspend fun delete(id: String): Resource<Unit>
}
