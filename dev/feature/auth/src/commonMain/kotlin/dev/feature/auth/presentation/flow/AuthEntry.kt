package dev.feature.auth.presentation.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppScreenScaffold
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.LogoTile
import dev.core.domain.repository.SettingsRepository
import dev.feature.auth.presentation.screens.RoleChoiceScreen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Foydalanuvchi turi — ilova Android'da har bir turga ALOHIDA Activity ochadi
 * (StudentActivity / BusinessActivity), login oqimlari ham shu bo'yicha ajraladi.
 */
enum class AuthUserFlow(val role: Role) {
    STUDENT(Role.STUDENT),
    BUSINESS(Role.BUSINESS),
}

/** Ildiz router holati — tanlangan rolga qarab. */
enum class RoleGate { LOADING, CHOOSE, STUDENT, BUSINESS }

/**
 * Tanlangan rolni LOCAL saqlaydi (AppSetting) va shundan o'qiydi. Bir marta tanlangач,
 * **logout qilgunча** doim shu rol ishlatiladi — Firebase sessiyasi/profil holatiga bog'liq emas.
 */
class RoleLauncherViewModel(
    private val settings: SettingsRepository,
) : ViewModel() {

    val gate: StateFlow<RoleGate> =
        settings.observeValue(SettingsRepository.KEY_SELECTED_ROLE)
            .map { saved ->
                when (saved) {
                    ROLE_BUSINESS -> RoleGate.BUSINESS
                    ROLE_STUDENT -> RoleGate.STUDENT
                    else -> RoleGate.CHOOSE
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoleGate.LOADING)

    /**
     * Tanlovni saqlaydi, so'ng [onSaved] chaqiriladi. MUHIM: navigatsiya (Activity almashish +
     * MainActivity.finish) faqat saqlashdan KEYIN bo'lishi kerak — aks holda ViewModel scope
     * bekor bo'lib, yozuv amalga oshmay qoladi.
     */
    fun choose(business: Boolean, onSaved: () -> Unit) {
        viewModelScope.launch {
            settings.setValue(SettingsRepository.KEY_SELECTED_ROLE, if (business) ROLE_BUSINESS else ROLE_STUDENT)
            onSaved()
        }
    }

    private companion object {
        const val ROLE_BUSINESS = "BUSINESS"
        const val ROLE_STUDENT = "STUDENT"
    }
}

/**
 * Ildiz router (MainActivity): saqlangan rolga qarab mos Activity'ni ochadi. Rol saqlanmagan
 * bo'lsa (yoki logout'dan keyin) rol tanlash ekranini ko'rsatadi va tanlovni saqlaydi.
 */
@Composable
fun RoleLauncher(
    onStudent: () -> Unit,
    onBusiness: () -> Unit,
    vm: RoleLauncherViewModel = koinViewModel(),
) {
    val gate by vm.gate.collectAsStateWithLifecycle()

    when (gate) {
        RoleGate.LOADING -> LauncherSplash()
        RoleGate.BUSINESS -> LaunchedEffect(Unit) { onBusiness() }
        RoleGate.STUDENT -> LaunchedEffect(Unit) { onStudent() }
        RoleGate.CHOOSE -> RoleChoiceScreen(
            onPick = { role ->
                // Avval saqlaymiz, KEYIN Activity ochamiz (finish ViewModel scope'ni bekor qiladi).
                vm.choose(role == Role.BUSINESS) {
                    if (role == Role.BUSINESS) onBusiness() else onStudent()
                }
            },
            onBack = {},
        )
    }
}

@Composable
private fun LauncherSplash() {
    AppScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LogoTile(size = 72.dp, radius = 22.dp, iconSize = 38.dp)
        }
    }
}
