package dev.feature.auth.presentation.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.components.AuthTab
import dev.core.designsystem.components.LogoTile
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
import dev.core.designsystem.theme.appPalette
import dev.feature.auth.social.rememberSocialAuthController
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
    // shunda ro'yxatdan o'tishда to'g'ri rol saqlanadi va rol tanlash ekrani o'tkazib yuboriladi.
    LaunchedEffect(flow) { if (flow != null) vm.onRoleChange(flow.role) }

    // Local keshdagi sessiyani tekshiramiz: kirgan bo'lsa to'g'ridan-to'g'ri HOME.
    val loggedIn by vm.loggedIn.collectAsStateWithLifecycle()
    if (loggedIn == null) {
        BootSplash() // kesh o'qilmagунча qisqa splash
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
                AuthEvent.OtpSent -> nav.navigate(Route.OTP)
                // Kod ishlatildi — orqaga qaytib bekor bo'lgan OTP ekraniga tushmasin.
                AuthEvent.OtpVerified -> nav.navigate(Route.SIGNUP) {
                    popUpTo(Route.OTP) { inclusive = true }
                }
                AuthEvent.EmailVerificationSent -> nav.navigate(Route.VERIFY_EMAIL)
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
            val afterOnboarding = { if (flow == null) nav.navigate(Route.ROLE) else nav.navigate(Route.WELCOME) }
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
                    if (role == Role.BUSINESS) nav.navigate(Route.BUSINESS_WELCOME)
                    else nav.navigate(Route.WELCOME)
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
                onEmail = { nav.navigate(Route.EMAIL) },
            )
        }
        composable(Route.WELCOME) {
            WelcomeScreen(
                state = state, vm = vm, tab = welcomeTab, onTab = { welcomeTab = it },
                onContinue = {
                    if (welcomeTab == AuthTab.PHONE) vm.sendOtp(socialAuth)
                    else nav.navigate(Route.EMAIL)
                },
                onSignUp = { nav.navigate(Route.REGISTER_CHOICE) },
                onGoogle = { vm.signInWithGoogle(socialAuth) },
                onApple = { vm.signInWithApple(socialAuth) },
                onTelegram = { vm.signInWithTelegram(socialAuth) },
            )
        }
        composable(Route.PHONE) {
            PhoneScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onSwitchEmail = { nav.navigate(Route.EMAIL) },
                onGetCode = { vm.sendOtp(socialAuth) },
                onSignIn = { nav.navigate(Route.EMAIL) },
                onGoogle = { vm.signInWithGoogle(socialAuth) },
                onApple = { vm.signInWithApple(socialAuth) },
                onTelegram = { vm.signInWithTelegram(socialAuth) },
            )
        }
        composable(Route.EMAIL) {
            val biometric = rememberBiometricAuthenticator()
            val bioScope = rememberCoroutineScope()
            EmailLoginScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onSwitchPhone = { nav.navigate(Route.PHONE) },
                onLogin = { vm.login() },
                onForgot = { nav.navigate(Route.FORGOT) },
                onBiometric = {
                    if (!biometric.canAuthenticate()) {
                        vm.biometricError("Qurilmada biometrika sozlanmagan (Face ID / barmoq izi).")
                    } else {
                        bioScope.launch {
                            when (biometric.authenticate("Kirish", "Face ID bilan tasdiqlang", "Bekor qilish")) {
                                BiometricOutcome.SUCCESS -> vm.onBiometricAuthenticated()
                                BiometricOutcome.FAILED -> vm.biometricError("Biometrika tanilmadi. Qayta urinib ko'ring.")
                                BiometricOutcome.UNAVAILABLE -> vm.biometricError("Biometrika mavjud emas.")
                                BiometricOutcome.CANCELLED -> Unit
                            }
                        }
                    }
                },
                onSignUp = { nav.navigate(Route.REGISTER_CHOICE) },
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
                onPhone = { nav.navigate(Route.PHONE) },
                onEmail = { nav.navigate(Route.REGISTER) },
                onSignIn = { nav.navigate(Route.EMAIL) },
            )
        }
        composable(Route.REGISTER) {
            RegisterScreen(
                state = state, vm = vm,
                onBack = { nav.popBackStack() },
                onCreate = { vm.registerWithEmail() },
                onSignIn = { nav.navigate(Route.EMAIL) },
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
            // oqimга mos login ekraniga qaytamiz (biznes -> biznes welcome, talaba -> welcome,
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
            LogoTile(size = 72, radius = 22, iconSize = 38)
        }
    }
}

