package dev.feature.auth.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.LogoTile
import dev.feature.business.BusinessShell
import org.koin.compose.viewmodel.koinViewModel

/**
 * Login'dan keyingi ildiz — foydalanuvchi rolini o'qib, mos shell'ni ko'rsatadi:
 * - BUSINESS  -> [BusinessShell] (faqat biznesmen bo'limlari)
 * - STUDENT/… -> [StudentShell]  (talaba bo'limlari)
 *
 * Shu tariqa biznesmen va talaba ilovaning butunlay alohida qismini ko'radi.
 */
@Composable
fun RootShell(onLoggedOut: () -> Unit, vm: RootShellViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    when {
        state.loading -> BootSplash()
        state.isBusiness -> BusinessShell(
            onLoggedOut = onLoggedOut,
            settingsContent = { onBack ->
                SettingsScreen(onBack = onBack, onEditProfile = {}, onLoggedOut = onLoggedOut)
            },
        )
        else -> StudentShell(onLoggedOut = onLoggedOut)
    }
}

/** Profil keshdan o'qilguncha qisqa splash. */
@Composable
private fun BootSplash() {
    AppScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LogoTile(size = 72.dp, radius = 22.dp, iconSize = 38.dp)
        }
    }
}
