package dev.feature.auth.presentation.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import dev.feature.auth.biometric.BiometricOutcome
import dev.feature.auth.biometric.rememberBiometricAuthenticator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.AuthTab
import dev.core.uikit.component.LogoTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_biometric_failed
import dev.core.uikit.resources.auth_biometric_not_configured
import dev.core.uikit.resources.auth_biometric_prompt_subtitle
import dev.core.uikit.resources.auth_biometric_unavailable
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.common_cancel
import dev.feature.auth.presentation.screens.EmailLoginScreen
import dev.feature.auth.presentation.screens.ForgotPasswordScreen
import dev.feature.auth.presentation.screens.OnboardingScreen
import dev.feature.auth.presentation.screens.OtpScreen
import dev.feature.auth.presentation.screens.PhoneScreen
import dev.feature.auth.presentation.screens.EmailVerifyScreen
import dev.feature.auth.presentation.screens.RegisterChoiceScreen
import dev.feature.auth.presentation.screens.RegisterScreen
import dev.feature.auth.presentation.screens.RoleChoiceScreen
import dev.feature.business.BusinessWelcomeScreen
import dev.feature.business.BusinessShell
import dev.feature.auth.presentation.main.SettingsScreen
import dev.feature.auth.presentation.screens.SignUpScreen
import dev.feature.auth.presentation.screens.WelcomeScreen
import dev.feature.auth.social.rememberSocialAuthController
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private object Route {
    const val ONBOARDING = "onboarding"
    const val ROLE = "role"
    const val WELCOME = "welcome"
    // Biznesmen uchun alohida oqim
    const val BUSINESS_WELCOME = "business_welcome"
    const val PHONE = "phone"
    const val EMAIL = "email"
    const val OTP = "otp"
    const val SIGNUP = "signup"
    const val REGISTER_CHOICE = "register_choice"
    const val REGISTER = "register"
    const val VERIFY_EMAIL = "verify_email"
    const val FORGOT = "forgot"
    const val HOME = "home"
}

/**
 * Auth oqimining butun navigatsiya grafi — barcha dizayn ekranlari.
 * commonMain'da yashaydi, shu bois Android va iOS'da bir xil ishlaydi.
 */
@Composable
fun AuthNavHost(
    flow: AuthUserFlow? = null,
    onExit: (() -> Unit)? = null,
    vm: AuthFlowViewModel = koinViewModel(),
) {
    // Rol-scoped oqim (Android'dagi StudentActivity/BusinessActivity) — rolni darrov o'rnatamiz,
    // shunda ro'yxatdan o'tishda to'g'ri rol saqlanadi va rol tanlash ekrani o'tkazib yuboriladi.
    LaunchedEffect(flow) { if (flow != null) vm.onRoleChange(flow.role) }

    // Local keshdagi sessiyani tekshiramiz: kirgan bo'lsa to'g'ridan-to'g'ri HOME.
    val loggedIn by vm.loggedIn.collectAsStateWithLifecycle()
    if (loggedIn == null) {
        BootSplash() // kesh o'qilmaguncha qisqa splash
        return
    }
    val startDestination = when {
        loggedIn == true -> Route.HOME
        flow == AuthUserFlow.BUSINESS -> Route.BUSINESS_WELCOME
        else -> Route.ONBOARDING
    }

    val nav = rememberNavController()
    val state by vm.state.collectAsStateWithLifecycle()
    val socialAuth = rememberSocialAuthController()
    var welcomeTab by remember { mutableStateOf(AuthTab.PHONE) }

    // Bir martalik hodisalar navigatsiyani boshqaradi (async auth natijalari).
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                AuthEvent.OtpSent -> nav.navigate(Route.OTP) { launchSingleTop = true }
                // Kod ishlatildi — orqaga qaytib bekor bo'lgan OTP ekraniga tushmasin.
                AuthEvent.OtpVerified -> nav.navigate(Route.SIGNUP) {
                    popUpTo(Route.OTP) { inclusive = true }
                }
                AuthEvent.EmailVerificationSent -> nav.navigate(Route.VERIFY_EMAIL) { launchSingleTop = true }
                // Hisob yaratildi — biznes profili to'ldirish qadami olib tashlandi;
                // to'g'ridan-to'g'ri asosiy ekranga (biznes ma'lumoti keyin "Biznes qo'shish" orqali).
                AuthEvent.Registered -> nav.navigate(Route.HOME) {
                    popUpTo(Route.ONBOARDING) { inclusive = true }
                    launchSingleTop = true
                }
                // Google/ijtimoiy kirish ham to'g'ridan-to'g'ri asosiy ekranga o'tadi.
                is AuthEvent.Authenticated -> nav.navigate(Route.HOME) {
                    popUpTo(Route.ONBOARDING) { inclusive = true }
                    launchSingleTop = true
                }
                AuthEvent.ProfileSaved -> nav.navigate(Route.HOME) {
                    popUpTo(Route.ONBOARDING) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = nav, startDestination = startDestination) {
        composable(Route.ONBOARDING) {
            // Rol-scoped oqimda (Activity) rol allaqachon tanlangan — to'g'ridan-to'g'ri login'ga.
            val afterOnboarding = {
                val next = if (flow == null) Route.ROLE else Route.WELCOME
                nav.navigate(next) { launchSingleTop = true }
            }
            OnboardingScreen(
                onNext = afterOnboarding,
                onSkip = afterOnboarding,
            )
        }
        composable(Route.ROLE) {
            // Login'dan oldin rol tanlash — biznesmen va talaba ALOHIDA oqimga ketadi.
            RoleChoiceScreen(
                onPick = { role ->
                    vm.onRoleChange(role)
                    if (role == Role.BUSINESS) nav.navigate(Route.BUSINESS_WELCOME) { launchSingleTop = true }
                    else nav.navigate(Route.WELCOME) { launchSingleTop = true }
                },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Route.BUSINESS_WELCOME) {
            BusinessWelcomeScreen(
                phone = state.phone,
                onPhoneChange = vm::onPhoneChange,
                phoneValid = state.phoneValid,
                isLoading = state.isLoading,
                onBack = { nav.popBackStack() },
                onGetCode = { vm.sendOtp(socialAuth) },
                onGoogle = { vm.signInWithGoogle(socialAuth) },
                onEmail = { nav.navigate(Route.EMAIL) { launchSingleTop = true } },
                onDevLogin = if (dev.core.domain.USE_LOCAL_DATA) ({ vm.devLogin() }) else null,
            )
        }
        composable(Route.WELCOME) {
            WelcomeScreen(
                state = state, vm = vm, tab = welcomeTab, onTab = { welcomeTab = it },
                onContinue = {
                    if (welcomeTab == AuthTab.PHONE) vm.sendOtp(socialAuth)
                    else nav.navigate(Route.EMAIL) { launchSingleTop = true }
                },
                onSignUp = { nav.navigate(Route.REGISTER_CHOICE) { launchSingleTop = true } },
                onGoogle = { vm.signInWithGoogle(socialAuth) },
                onApple = { vm.signInWithApple(socialAuth) },
                onTelegram = { vm.signInWithTelegram(socialAuth) },
            )
        }
        composable(Route.PHONE) {
            PhoneScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onSwitchEmail = { nav.navigate(Route.EMAIL) { launchSingleTop = true } },
                onGetCode = { vm.sendOtp(socialAuth) },
                onSignIn = { nav.navigate(Route.EMAIL) { launchSingleTop = true } },
                onGoogle = { vm.signInWithGoogle(socialAuth) },
                onApple = { vm.signInWithApple(socialAuth) },
                onTelegram = { vm.signInWithTelegram(socialAuth) },
            )
        }
        composable(Route.EMAIL) {
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
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onSwitchPhone = { nav.navigate(Route.PHONE) { launchSingleTop = true } },
                onLogin = { vm.login() },
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
                onSignUp = { nav.navigate(Route.REGISTER_CHOICE) { launchSingleTop = true } },
            )
        }
        composable(Route.OTP) {
            OtpScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onVerify = { vm.confirmOtp(socialAuth) },
                onResend = { vm.resend(socialAuth) },
                onTelegram = { vm.signInWithTelegram(socialAuth) },
            )
        }
        composable(Route.SIGNUP) {
            SignUpScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onCreate = { vm.register() },
            )
        }
        composable(Route.REGISTER_CHOICE) {
            RegisterChoiceScreen(
                onBack = { nav.popBackStack() },
                onPhone = { nav.navigate(Route.PHONE) { launchSingleTop = true } },
                onEmail = { nav.navigate(Route.REGISTER) { launchSingleTop = true } },
                onSignIn = { nav.navigate(Route.EMAIL) { launchSingleTop = true } },
            )
        }
        composable(Route.REGISTER) {
            RegisterScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onCreate = { vm.registerWithEmail() },
                onSignIn = { nav.navigate(Route.EMAIL) { launchSingleTop = true } },
            )
        }
        composable(Route.VERIFY_EMAIL) {
            EmailVerifyScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onVerify = { vm.verifyEmailCode() },
                onResend = { vm.resendEmailCode() },
            )
        }
        composable(Route.FORGOT) {
            ForgotPasswordScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onSend = { vm.requestPasswordReset() },
                onBackToLogin = { nav.popBackStack() },
            )
        }
        composable(Route.HOME) {
            // Chiqish: Activity oqimida — ildiz router'ga qaytamiz (onExit); aks holda (iOS)
            // oqimga mos login ekraniga qaytamiz (biznes -> biznes welcome, talaba -> welcome,
            // bo'linmagan -> rol tanlash).
            val loggedOut: () -> Unit = onExit ?: {
                val dest = when (flow) {
                    AuthUserFlow.BUSINESS -> Route.BUSINESS_WELCOME
                    AuthUserFlow.STUDENT -> Route.WELCOME
                    null -> Route.ROLE
                }
                nav.navigate(dest) {
                    popUpTo(Route.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
            when (flow) {
                AuthUserFlow.BUSINESS -> BusinessShell(
                    onLoggedOut = loggedOut,
                    settingsContent = { onBack ->
                        SettingsScreen(onBack = onBack, onEditProfile = {}, onLoggedOut = loggedOut)
                    },
                    // Chat ekranlari auth modulida — karkasga slot sifatida beriladi.
                    messagesScreen = { onBack -> dev.feature.auth.presentation.main.ChatScreen(onBack = onBack) },
                    supportScreen = { onBack -> dev.feature.auth.presentation.main.SupportChatScreen(onBack = onBack) },
                )
                AuthUserFlow.STUDENT -> dev.feature.auth.presentation.main.StudentShell(onLoggedOut = loggedOut)
                // iOS / bo'linmagan rejim — rolga qarab RootShell hal qiladi.
                null -> dev.feature.auth.presentation.main.RootShell(onLoggedOut = loggedOut)
            }
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

