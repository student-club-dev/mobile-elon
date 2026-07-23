package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.TradeCenter
import dev.feature.discounts.domain.model.TradeCenterDetail
import dev.feature.discounts.domain.repository.TradeCenterRepository

/** Savdo markazlari ro'yxati — filial formasidagi tanlash uchun. */
class GetTradeCentersUseCase(private val repository: TradeCenterRepository) {
    suspend operator fun invoke(): Resource<List<TradeCenter>> = repository.all()
}

/** Tanlangan markazning dinamik maydonlari (qator, do'kon raqami...). */
class GetTradeCenterDetailUseCase(private val repository: TradeCenterRepository) {
    suspend operator fun invoke(id: String): Resource<TradeCenterDetail> = repository.detail(id)
}
