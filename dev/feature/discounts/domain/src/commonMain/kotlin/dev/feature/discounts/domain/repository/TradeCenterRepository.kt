package dev.feature.discounts.domain.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.TradeCenter
import dev.feature.discounts.domain.model.TradeCenterDetail

/**
 * Savdo markazlari (`GET /v1/trade-centers`).
 *
 * Filial savdo markazi ichida bo'lsa, forma markazning **dinamik maydonlarini** so'raydi
 * (qator, do'kon raqami...) — ular markazdan markazga farq qilgani uchun ro'yxat backenddan
 * olinadi ([detail]).
 */
interface TradeCenterRepository {

    /** Barcha savdo markazlari (tanlash ro'yxati uchun). */
    suspend fun all(): Resource<List<TradeCenter>>

    /** Bitta markaz + uning maydonlari. */
    suspend fun detail(id: String): Resource<TradeCenterDetail>
}
