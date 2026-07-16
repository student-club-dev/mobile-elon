package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.domain.model.DiscountCategory
import dev.core.domain.model.DiscountOffer
import dev.core.domain.repository.DiscountRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Chegirmalar (1q grid + 1w kategoriya ichi) ekranining holati. */
data class DiscountsUiState(
    val categories: List<DiscountCategory> = emptyList(),
    val selected: DiscountCategory? = null,
    val offers: List<DiscountOffer> = emptyList(),
    val query: String = "",
    val savedIds: Set<String> = emptySet(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class DiscountsViewModel(
    private val discountRepository: DiscountRepository,
) : ViewModel() {

    private val selectedId = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")

    private val offersFlow = selectedId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else discountRepository.observeOffers(id)
    }
    private val savedIdsFlow = discountRepository.observeSaved().map { list -> list.map { it.id }.toSet() }

    val state: StateFlow<DiscountsUiState> = combine(
        discountRepository.observeCategories(),
        selectedId,
        offersFlow,
        query,
        savedIdsFlow,
    ) { categories, id, offers, q, savedIds ->
        val selected = categories.firstOrNull { it.id == id }
        val filtered = if (q.isBlank()) offers
        else offers.filter { it.merchant.contains(q, ignoreCase = true) || it.title.contains(q, ignoreCase = true) }
        DiscountsUiState(categories, selected, filtered, q, savedIds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiscountsUiState())

    init {
        // Offline-first: ekran ochilганда fonда backend'dan sinxronlashga urinamiz.
        // UI DB'ni kuzatadi — xato/tarmoqsiz bo'lsa cache ko'rinaveradi (refresh no-op yoki Error).
        viewModelScope.launch { discountRepository.refresh() }
    }

    fun open(category: DiscountCategory) {
        query.value = ""
        selectedId.value = category.id
    }

    fun close() {
        selectedId.value = null
    }

    fun onQuery(q: String) {
        query.value = q
    }

    fun toggleSaved(offer: DiscountOffer, currentlySaved: Boolean) {
        viewModelScope.launch { discountRepository.setSaved(offer.id, !currentlySaved) }
    }
}
