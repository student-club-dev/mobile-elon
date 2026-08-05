package dev.feature.auth.di

import dev.core.domain.repository.AuthRepository
import dev.core.domain.repository.ChatRealtimeSource
import dev.core.domain.repository.SessionProvider
import dev.core.network.NetworkConfig
import dev.core.network.generated.api.AuthBusinessApi
import dev.feature.auth.data.ApiAuthRepository
import dev.feature.auth.data.LocalChatRealtimeSource
import dev.feature.auth.data.TokenSessionProvider
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.flow.RoleLauncherViewModel
import dev.feature.auth.presentation.main.ChatViewModel
import dev.feature.auth.presentation.main.DiscountsViewModel
import dev.feature.auth.presentation.main.HomeViewModel
import dev.feature.auth.presentation.main.JobsViewModel
import dev.feature.auth.presentation.main.NotificationsViewModel
import dev.feature.auth.presentation.main.PostAdViewModel
import dev.feature.auth.presentation.main.RootShellViewModel
import dev.feature.auth.presentation.main.SettingsViewModel
import dev.feature.auth.presentation.main.StudentsViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Auth feature'ining bog'lanishlari.
 *
 * Sessiya to'liq **backendда** (`/v1/auth/business/…`): kirish/ro'yxat telefon yoki email +
 * parol bilan, SMS kod esa faqat raqamni tasdiqlash va parolni tiklash uchun. Tokenlar
 * `TokenStore` da (core:data), ularni yangilash tarmoq qatlamida avtomatik.
 */
val authFeatureModule = module {

    // Biznes ilovasi — auth endpoint'lari `/auth/business/…`.
    single { AuthBusinessApi(baseUrl = get<NetworkConfig>().baseUrl, httpClient = get<HttpClient>()) }

    single<AuthRepository> {
        ApiAuthRepository(
            api = get(),
            tokenStore = get(),
            database = get(),
            httpClient = get(),
            connectivity = get(),
            profileRepository = get(),
        )
    }

    // Joriy uid manbai — profil/biznes/e'lon egaligini beradi (JWT `sub`).
    single<SessionProvider> { TokenSessionProvider(get()) }

    // Chat real-time manbasi — backendда chat endpoint'lari yo'q, local bazadan ishlaydi.
    single<ChatRealtimeSource> { LocalChatRealtimeSource() }

    viewModel {
        AuthFlowViewModel(
            loginUseCase = get(),
            loginWithGoogleUseCase = get(),
            registerUseCase = get(),
            requestPhoneOtpUseCase = get(),
            verifyPhoneOtpUseCase = get(),
            forgotPasswordUseCase = get(),
            resetPasswordUseCase = get(),
            observeCurrentUserUseCase = get(),
            logoutUseCase = get(),
            // Ro'yxatning oxirgi qadami — ism/familiya/rasm/email profilга yoziladi.
            observeProfileUseCase = get(),
            hasProfileUseCase = get(),
            saveProfileUseCase = get(),
            uploadAvatarUseCase = get(),
            settingsRepository = get(),
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
