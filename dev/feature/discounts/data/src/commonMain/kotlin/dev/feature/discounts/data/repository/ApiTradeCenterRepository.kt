package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.TradeCentersApi
import dev.core.network.generated.model.TradeCenterDetailDto
import dev.core.network.generated.model.TradeCenterDto
import dev.core.network.generated.model.TradeCenterFieldDto
import dev.core.network.generated.model.TradeCenterFieldTypeDto
import dev.core.network.response.safeCall
import dev.feature.discounts.domain.model.TradeCenter
import dev.feature.discounts.domain.model.TradeCenterDetail
import dev.feature.discounts.domain.model.TradeCenterField
import dev.feature.discounts.domain.model.TradeCenterFieldKind
import dev.feature.discounts.domain.repository.TradeCenterRepository
import io.ktor.client.call.body

/**
 * Savdo markazlarining backend implementatsiyasi (`GET /v1/trade-centers`).
 *
 * Zaxira yo'q: markazlar ro'yxati faqat serverда bor. Xato bo'lsa forma savdo markazi
 * tanlashni ko'rsatmaydi va filial oddiy manzil bilan saqlanadi.
 */
class ApiTradeCenterRepository(
    private val api: TradeCentersApi,
    private val connectivity: NetworkConnectivity,
) : TradeCenterRepository {

    override suspend fun all(): Resource<List<TradeCenter>> = safeCall(connectivity) {
        api.tradeCentersList().body().map { it.toDomain() }
    }

    override suspend fun detail(id: String): Resource<TradeCenterDetail> = safeCall(connectivity) {
        api.get(id).body().toDomain()
    }
}

// ---------------------------------------------------------------------------
// Mapper'lar — DTO ↔ domen
// ---------------------------------------------------------------------------

private fun TradeCenterDto.toDomain() = TradeCenter(id = id, name = name, slug = slug)

private fun TradeCenterDetailDto.toDomain() = TradeCenterDetail(
    center = TradeCenter(id = id, name = name, slug = slug),
    fields = fields.map { it.toDomain() }.sortedBy { it.sortOrder },
)

private fun TradeCenterFieldDto.toDomain() = TradeCenterField(
    id = id,
    label = label,
    kind = when (type) {
        TradeCenterFieldTypeDto.TEXT -> TradeCenterFieldKind.TEXT
        TradeCenterFieldTypeDto.NUMBER -> TradeCenterFieldKind.NUMBER
    },
    required = required,
    sortOrder = sortOrder,
)
