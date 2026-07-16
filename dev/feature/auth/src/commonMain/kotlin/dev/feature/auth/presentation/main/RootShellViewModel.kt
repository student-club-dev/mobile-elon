package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.feature.profile.domain.usecase.ObserveProfileUseCase
import dev.feature.profile.domain.usecase.RefreshProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Login'dan keyingi ildiz ekran holati — rolga qarab qaysi shell ochilishini hal qiladi. */
data class RootShellState(
    /** Profil hali aniqlanmadi — qisqa splash ko'rsatiladi. */
    val loading: Boolean = true,
    /** Rol BUSINESS bo'lsa biznesmen shell'i, aks holda talaba shell'i. */
    val isBusiness: Boolean = false,
)

/**
 * Profil rolini kuzatib, qaysi shell/Activity ochilishini aniqlaydi.
 * Rol signup'da tanlanadi (STUDENT/BUSINESS) va profil bilan saqlanadi.
 *
 * MUHIM: yangi sessiyada local kesh hali bo'sh bo'lishi mumkin (profil `null`). Bunday paytda
 * DARROV "talaba" deb qaror qilmaymiz — aks holda biznesmen ham StudentActivity'ga tushib qolardi.
 * Shuning uchun profil kelmaguncha YOKI remote yangilanish tugamaguncha `loading` da turamiz.
 */
class RootShellViewModel(
    observeProfileUseCase: ObserveProfileUseCase,
    private val refreshProfileUseCase: RefreshProfileUseCase,
) : ViewModel() {

    private val refreshDone = MutableStateFlow(false)

    init {
        // Rol masofaviy profilda bo'lishi mumkin — keshni yangilaymiz, so'ng "tugadi" deb belgilaymiz.
        viewModelScope.launch {
            refreshProfileUseCase()
            refreshDone.value = true
        }
    }

    val state: StateFlow<RootShellState> =
        combine(observeProfileUseCase(), refreshDone) { profile, done ->
            when {
                // Profil aniq — rol bo'yicha hal qilamiz.
                profile != null -> RootShellState(loading = false, isBusiness = profile.role == ROLE_BUSINESS)
                // Yangilanish tugadi, lekin profil yo'q — talaba (default).
                done -> RootShellState(loading = false, isBusiness = false)
                // Hali kutmoqdamiz.
                else -> RootShellState(loading = true, isBusiness = false)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RootShellState())

    private companion object {
        const val ROLE_BUSINESS = "BUSINESS"
    }
}
