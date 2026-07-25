package dev.feature.auth.presentation.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.LogoTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_biometric_failed
import dev.core.uikit.resources.auth_biometric_not_configured
import dev.core.uikit.resources.auth_biometric_prompt_subtitle
import dev.core.uikit.resources.auth_biometric_unavailable
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.common_cancel
import dev.feature.auth.biometric.BiometricOutcome
import dev.feature.auth.biometric.rememberBiometricAuthenticator
import dev.feature.auth.presentation.main.SettingsScreen
import dev.feature.auth.presentation.screens.EmailLoginScreen
import dev.feature.auth.presentation.screens.ForgotPasswordScreen
import dev.feature.auth.presentation.screens.GoogleSignInButton
import dev.feature.auth.presentation.screens.OtpScreen
import dev.feature.auth.presentation.screens.RegisterScreen
import dev.feature.auth.presentation.screens.ResetCodeScreen
import dev.feature.auth.presentation.screens.ResetPasswordScreen
import dev.feature.business.BusinessShell
import dev.feature.business.BusinessWelcomeScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private object Route {
    /** Biznes kirish — telefon + parol (ilovaning boshlang'ich ekrani). */
    const val LOGIN = "login"

    /** Email + parol bilan kirish. */
    const val EMAIL_LOGIN = "email_login"

    /** Ro'yxatdan o'tish — telefon yoki email + parol. */
    const val REGISTER = "register"

    /** SMS kod — raqamni tasdiqlash (ro'yxatdan keyin). */
    const val VERIFY_PHONE = "verify_phone"

    /** Parolni tiklash: raqam kiritish. */
    const val FORGOT = "forgot"

    /** Parolni tiklash: SMS kodni kiritish. */
    const val RESET_CODE = "reset_code"

    /** Parolni tiklash: yangi parol (kod allaqachon holatда). */
    const val RESET_PASSWORD = "reset_password"

    const val HOME = "home"
}

/**
 * Auth oqimining navigatsiya grafi. ElonUz — **biznes** ilovasi, shuning uchun graf bitta
 * oqimdan iborat: kirish → (ro'yxat / parolni tiklash) → bosh ekran.
 *
 * commonMain'da yashaydi, shu bois Android va iOS'da bir xil ishlaydi.
 */
@Composable
fun AuthNavHost(
    onExit: (() -> Unit)? = null,
    vm: AuthFlowViewModel = koinViewModel(),
) {
    // Local sessiya keshini tekshiramiz: kirgan bo'lsa to'g'ridan-to'g'ri HOME.
    val loggedIn by vm.loggedIn.collectAsStateWithLifecycle()
    if (loggedIn == null) {
        BootSplash() // kesh o'qilmaguncha qisqa splash
        return
    }

    val nav = rememberNavController()
    val state by vm.state.collectAsStateWithLifecycle()

    // Bir martalik hodisalar navigatsiyani boshqaradi (async auth natijalari).
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                AuthEvent.PhoneVerificationSent -> nav.navigate(Route.VERIFY_PHONE) {
                    launchSingleTop = true
                }
                AuthEvent.ResetCodeSent -> nav.navigate(Route.RESET_CODE) { launchSingleTop = true }
                // Parol yangilandi — kirish ekraniga qaytamiz (yangi parol bilan kiradi).
                AuthEvent.PasswordReset -> nav.navigate(Route.LOGIN) {
                    popUpTo(Route.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
                is AuthEvent.Authenticated -> nav.navigate(Route.HOME) {
                    popUpTo(Route.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = if (loggedIn == true) Route.HOME else Route.LOGIN,
    ) {
        composable(Route.LOGIN) {
            BusinessWelcomeScreen(
                phone = state.phone,
                onPhoneChange = vm::onPhoneChange,
                password = state.password,
                onPasswordChange = vm::onPasswordChange,
                passwordVisible = state.passwordVisible,
                onTogglePasswordVisible = vm::togglePasswordVisible,
                canSubmit = state.phoneLoginReady,
                isLoading = state.isLoading,
                error = state.error,
                onSignIn = vm::loginWithPhone,
                onForgot = { nav.navigate(Route.FORGOT) { launchSingleTop = true } },
                onEmail = { nav.navigate(Route.EMAIL_LOGIN) { launchSingleTop = true } },
                onRegister = { nav.navigate(Route.REGISTER) { launchSingleTop = true } },
                // Google oqimi auth modulida (`rememberGoogleSignIn`), ekran esa business
                // modulida — shu sabab tugma slot sifatida uzatiladi.
                googleButton = { GoogleSignInButton(vm) },
            )
        }

        composable(Route.EMAIL_LOGIN) {
            val biometric = rememberBiometricAuthenticator()
            val bioScope = rememberCoroutineScope()
            // Biometrika matnlari kompozitsiya doirasida o'qiladi — lambda ichida
            // `stringResource` chaqirib bo'lmaydi (u @Composable emas).
            val bioTitle = stringResource(Res.string.auth_sign_in)
            val bioSubtitle = stringResource(Res.string.auth_biometric_prompt_subtitle)
            val bioCancel = stringResource(Res.string.common_cancel)
            val bioNotConfigured = stringResource(Res.string.auth_biometric_not_configured)
            val bioFailed = stringResource(Res.string.auth_biometric_failed)
            val bioUnavailable = stringResource(Res.string.auth_biometric_unavailable)
            EmailLoginScreen(
                state = state,
                vm = vm,
                onBack = { nav.popBackStack() },
                onSwitchPhone = { nav.popBackStack() },
                onLogin = vm::loginWithEmail,
                onForgot = { nav.navigate(Route.FORGOT) { launchSingleTop = true } },
                onBiometric = {
                    if (!biometric.canAuthenticate()) {
                        vm.biometricError(bioNotConfigured)
                    } else {
                        bioScope.launch {
                            when (biometric.authenticate(bioTitle, bioSubtitle, bioCancel)) {
                                BiometricOutcome.SUCCESS -> vm.onBiometricAuthenticated()
                                BiometricOutcome.FAILED -> vm.biometricError(bioFailed)
                                BiometricOutcome.UNAVAILABLE -> vm.biometricError(bioUnavailable)
                                BiometricOutcome.CANCELLED -> Unit
                            }
                        }
                    }
                },
                onSignUp = { nav.navigate(Route.REGISTER) { launchSingleTop = true } },
            )
        }

        composable(Route.REGISTER) {
            RegisterScreen(
                state = state,
                vm = vm,
                onBack = { nav.popBackStack() },
                onCreate = vm::register,
                onSignIn = { nav.popBackStack() },
            )
        }

        composable(Route.VERIFY_PHONE) {
            OtpScreen(
                state = state,
                vm = vm,
                onBack = { nav.popBackStack() },
                onVerify = vm::verifyPhone,
                onResend = vm::resendCode,
                onSkip = vm::skipPhoneVerification,
            )
        }

        composable(Route.FORGOT) {
            ForgotPasswordScreen(
                state = state,
                vm = vm,
                onBack = { nav.popBackStack() },
                onSend = vm::requestPasswordReset,
                onBackToLogin = { nav.popBackStack() },
            )
        }

        // Tiklash ikki qadamga bo'lingan: avval kod, keyin parol. Sabab — bitta ekranда
        // 6 raqam va ikki marta parol yozishga ulgurmasdan kod eskirib qolardi.
        composable(Route.RESET_CODE) {
            ResetCodeScreen(
                state = state,
                vm = vm,
                onBack = { nav.popBackStack() },
                onContinue = {
                    // Kod hali serverга yuborilmaydi — u parol bilan birga ketadi
                    // (`/password/reset` uchalasini bitta so'rovда kutadi).
                    vm.clearError()
                    nav.navigate(Route.RESET_PASSWORD) { launchSingleTop = true }
                },
                onResend = vm::resendCode,
            )
        }

        composable(Route.RESET_PASSWORD) {
            ResetPasswordScreen(
                state = state,
                vm = vm,
                // Orqага — kodni qayta kiritish yoki qaytadan so'rash uchun.
                onBack = { nav.popBackStack() },
                onSubmit = vm::resetPassword,
            )
        }

        composable(Route.HOME) {
            // Chiqish (logout): navigatsiya grafida to'g'ridan-to'g'ri LOGIN (telefon + parol)
            // ekraniga qaytamiz va HOME'ni stackdan butunlay olib tashlaymiz.
            //
            // MUHIM — nega `onExit` (Android Activity.recreate) GA TAYANMAYMIZ: `recreate()`
            // ViewModel'larni config-change kabi SAQLAB qoladi, shu bois `AuthFlowViewModel.loggedIn`
            // (sessiya keshi) qayta yaratishда hali eski `true` qiymatда bo'lib, `startDestination`
            // HOME hisoblanardi — foydalanuvchi "chiqdim" desa ham HOME'ga tushib qolardi.
            // Grafik ichida ochiq navigatsiya bu poygani yo'q qiladi va ikkala platformada ham
            // bir xil deterministik ishlaydi.
            val loggedOut: () -> Unit = {
                nav.navigate(Route.LOGIN) {
                    popUpTo(Route.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
            BusinessShell(
                onLoggedOut = loggedOut,
                settingsContent = { onBack, onEditProfile ->
                    SettingsScreen(onBack = onBack, onEditProfile = onEditProfile, onLoggedOut = loggedOut)
                },
                // Chat ekranlari auth modulida — karkasga slot sifatida beriladi.
                messagesScreen = { onBack -> dev.feature.auth.presentation.main.ChatScreen(onBack = onBack) },
                supportScreen = { onBack -> dev.feature.auth.presentation.main.SupportChatScreen(onBack = onBack) },
            )
        }
    }
}

/** Kesh o'qilguncha ko'rsatiladigan qisqa boshlang'ich ekran (session restore). */
@Composable
private fun BootSplash() {
    AppScreenScaffold {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LogoTile(size = 72.dp, radius = 22.dp, iconSize = 38.dp)
        }
    }
}
