package dev.feature.discounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.usecase.DeleteListingUseCase
import dev.feature.discounts.domain.usecase.ObserveMyListingsUseCase
import dev.feature.discounts.domain.usecase.ToggleListingPausedUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * "Mening e'lonlarim" ekran holati — loading/content/empty/error, offline banner.
 *
 * [error] `null` bo'lmasa — ma'lumot o'qishда xato (retry ko'rsatiladi). [offline] — internet
 * yo'qligini bildiruvchi banner uchun. Ro'yxatning bo'sh-ligi UI'da ([listings].isEmpty).
 */
data class MyListingsUiState(
    val listings: List<Listing> = emptyList(),
    val loading: Boolean = true,
    val error: AppException? = null,
)

/** "Mening e'lonlarim" — biznes egasi yuklagan chegirmalar (barcha statuslar). */
class MyListingsViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeMyListings: ObserveMyListingsUseCase,
    private val togglePaused: ToggleListingPausedUseCase,
    private val deleteListing: DeleteListingUseCase,
    private val connectivity: NetworkConnectivity,
) : ViewModel() {

    /** "Qayta urinish" bosilganда oqimni qaytadan ishga tushiradigan trigger. */
    private val retryTrigger = MutableStateFlow(0)

    /** Real-time internet holati — UI "internet yo'q" banner'ini ko'rsatishi uchun. */
    val online: StateFlow<Boolean> = connectivity.online
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), connectivity.isOnline())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MyListingsUiState> =
        combine(observeCurrentUser(), retryTrigger) { user, _ -> user }
            // Foydalanuvchi almashsa (yoki retry) ro'yxat qaytadan kuzatiladi.
            .flatMapLatest { user -> observeMyListings((user?.id ?: 0L).toString()) }
            .map { MyListingsUiState(listings = it, loading = false) }
            // Oqim xato bersa (ruxsat/tarmoq) — typed xatoga aylantirib ko'rsatamiz.
            .catch { e ->
                emit(MyListingsUiState(loading = false, error = e.toAppException(connectivity.isOnline())))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyListingsUiState(loading = true))

    fun retry() {
        retryTrigger.value += 1
    }

    fun togglePaused(listing: Listing) {
        viewModelScope.launch { togglePaused.invoke(listing) }
    }

    fun delete(listing: Listing) {
        viewModelScope.launch { deleteListing(listing.id) }
    }
}
