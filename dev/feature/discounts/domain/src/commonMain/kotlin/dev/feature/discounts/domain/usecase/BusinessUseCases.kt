package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow

/** Joriy foydalanuvchi bizneslari ro'yxatini real-time kuzatadi (bosh ekran). */
class ObserveMyBusinessesUseCase(private val repository: BusinessRepository) {
    operator fun invoke(): Flow<List<Business>> = repository.observeMine()
}

/** Bitta biznesni id bo'yicha oladi (e'lon yuklashda meros olish uchun). */
class GetBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(id: String): Business? = repository.byId(id)
}

/** Biznesни yaratadi/yangilaydi (nom, telefon, tur, lokatsiya). */
class SaveBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(business: Business): Resource<Business> = repository.save(business)
}

/** Biznesни o'chiradi. */
class DeleteBusinessUseCase(private val repository: BusinessRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> = repository.delete(id)
}
