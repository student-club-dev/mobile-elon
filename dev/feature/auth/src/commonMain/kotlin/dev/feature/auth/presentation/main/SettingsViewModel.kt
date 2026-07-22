package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.domain.model.AppLanguage
import dev.core.domain.model.ThemeMode
import dev.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.Default,
    val language: AppLanguage = AppLanguage.Default,
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = false,
)

/** Sozlamalar (mavzu + bildirishnoma bayroqlari) — local DB'da doimiy saqlanadi. */
class SettingsViewModel(
    private val settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        settings.observeThemeMode(),
        settings.observeLanguage(),
        settings.observeFlag(SettingsRepository.KEY_NOTIF_PUSH, default = true),
        settings.observeFlag(SettingsRepository.KEY_NOTIF_EMAIL, default = false),
    ) { theme, language, push, email ->
        SettingsUiState(
            themeMode = theme,
            language = language,
            pushEnabled = push,
            emailEnabled = email,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settings.setLanguage(language) }
    }

    fun setPush(enabled: Boolean) {
        viewModelScope.launch { settings.setFlag(SettingsRepository.KEY_NOTIF_PUSH, enabled) }
    }

    fun setEmail(enabled: Boolean) {
        viewModelScope.launch { settings.setFlag(SettingsRepository.KEY_NOTIF_EMAIL, enabled) }
    }
}
