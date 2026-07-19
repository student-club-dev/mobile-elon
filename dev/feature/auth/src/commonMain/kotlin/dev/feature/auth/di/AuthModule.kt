package dev.feature.auth.di

import dev.core.common.AuthTokenProvider
import dev.core.domain.USE_LOCAL_DATA
import dev.core.domain.repository.AuthRepository
import dev.core.domain.repository.ChatRealtimeSource
import dev.core.domain.repository.SessionProvider
import dev.feature.auth.data.DevAuthRepository
import dev.feature.auth.data.DevSessionProvider
import dev.feature.auth.data.FirebaseAuthRepository
import dev.feature.auth.data.FirebaseSessionProvider
import dev.feature.auth.data.FirebaseTokenProvider
import dev.feature.auth.data.FirestoreChatRealtimeSource
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.main.ChatViewModel
import dev.feature.auth.presentation.main.DiscountsViewModel
import dev.feature.auth.presentation.main.HomeViewModel
import dev.feature.auth.presentation.main.JobsViewModel
import dev.feature.auth.presentation.main.NotificationsViewModel
import dev.feature.auth.presentation.flow.RoleLauncherViewModel
import dev.feature.auth.presentation.main.PostAdViewModel
import dev.feature.auth.presentation.main.RootShellViewModel
import dev.feature.auth.presentation.main.SettingsViewModel
import dev.feature.auth.presentation.main.StudentsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Email ro'yxatдан o'tish 6 xonali kod (Cloud Function) bilanmi?
 * - `false` → native (deploy TALAB QILMAYDI): akkaunt darrov yaratiladi.
 * - `true`  → Cloud Function (`firebase deploy` + Blaze kerak): kod tasdiqлангач yaratiladi.
 * Deploy qilганdан keyin shu yerни `true` qiling.
 */
private const val USE_EMAIL_CODE = false

/**
 * Chat real-time (Firestore) yoqilganmi (B7). Firebase sozlangач `true` qiling.
 * `false` — chat local DB'dan ishlaydi (demo/seed suhbatlar).
 */
private const val CHAT_REALTIME_ENABLED = false

val authFeatureModule = module {
    // Local test rejimida (USE_LOCAL_DATA) — Firebase'siz dev auth; aks holda Firebase.
    single<AuthRepository> {
        if (USE_LOCAL_DATA) DevAuthRepository(get()) else FirebaseAuthRepository(get())
    }

    // Joriy uid manbai — profil/biznes/e'lon egaligini beradi (Firebase'ga bevosita bog'lanmasin).
    single<SessionProvider> {
        if (USE_LOCAL_DATA) DevSessionProvider(get()) else FirebaseSessionProvider()
    }

    // Ktor uchun Firebase ID token beruvchi (network qatlami shuni ishlatadi).
    single<AuthTokenProvider> { FirebaseTokenProvider() }

    // Chat real-time manbasi (Firestore) — ChatRepository shuni ishlatadi (B7).
    single<ChatRealtimeSource> { FirestoreChatRealtimeSource(CHAT_REALTIME_ENABLED) }

    viewModel {
        AuthFlowViewModel(
            loginUseCase = get(),
            registerUseCase = get(),
            sendPasswordResetUseCase = get(),
            requestEmailSignupUseCase = get(),
            confirmEmailSignupUseCase = get(),
            saveProfileUseCase = get(),
            syncExternalUserUseCase = get(),
            hasProfileUseCase = get(),
            observeCurrentUserUseCase = get(),
            settingsRepository = get(),
            useEmailCode = USE_EMAIL_CODE,
        )
    }
    viewModelOf(::RootShellViewModel)
    viewModelOf(::RoleLauncherViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::JobsViewModel)
    viewModelOf(::StudentsViewModel)
    viewModelOf(::DiscountsViewModel)
    viewModelOf(::PostAdViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::NotificationsViewModel)
}
