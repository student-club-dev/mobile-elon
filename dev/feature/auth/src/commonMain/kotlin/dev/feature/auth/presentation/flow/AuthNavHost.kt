package dev.feature.auth.presentation.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.LogoTile
import dev.feature.auth.presentation.main.SettingsScreen
import dev.feature.auth.presentation.screens.AccountSetupScreen
import dev.feature.auth.presentation.screens.ForgotPasswordScreen
import dev.feature.auth.presentation.screens.GoogleSignInButton
import dev.feature.auth.presentation.screens.OtpScreen
import dev.feature.auth.presentation.screens.RegisterScreen
import dev.feature.auth.presentation.screens.ResetCodeScreen
import dev.feature.auth.presentation.screens.ResetPasswordScreen
import dev.feature.business.BusinessShell
import dev.feature.business.BusinessWelcomeScreen
import org.koin.compose.viewmodel.koinViewModel

private object Route {
    /** Biznes kirish — telefon + parol yoki Google (ilovaning boshlang'ich ekrani). */
    const val LOGIN = "login"

    /** Ro'yxatdan o'tish — telefon + parol. */
    const val REGISTER = "register"

    /** SMS kod — raqamni tasdiqlash (ro'yxatdan keyin). */
    const val VERIFY_PHONE = "verify_phone"

    /** Hisob ma'lumotlari — ism, familiya, rasm, email (ro'yxatning oxirgi qadami). */
    const val ACCOUNT_SETUP = "account_setup"

    /** Parolni tiklash: raqam kiritish. */
    const val FORGOT = "forgot"

    /** Parolni tiklash: SMS kodni kiritish. */
    const val RESET_CODE = "reset_code"

    /** Parolni tiklash: yangi parol (kod allaqachon holatда). */
    const val RESET_PASSWORD = "reset_password"

    const val HOME = "home"

    /**
     * Grafning o'zining marshruti. Kerak, chunki auth oqimi tugaganда butun stackni tozalash
     * uchun `popUpTo(<graf>) { inclusive = true }` ishlatiladi: bitta ekranга `popUpTo` qilish
     * ishonchsiz — o'sha ekran oqim ichida allaqachon olib tashlangan bo'lishi mumkin
     * (masalan tasdiqlangan OTP), va u holда hech narsa tozalanmasdi. `NavController.graph.id`
     * esa faqat Android'да bor, KMP'да yo'q — shuning uchun marshrut nomi bilan.
     */
    const val GRAPH = "auth_graph"
}

/**
 * Auth oqimining navigatsiya grafi. ElonUz — **biznes** ilovasi, shuning uchun graf bitta
 * oqimdan iborat: kirish → (ro'yxat / parolni tiklash) → bosh ekran.
 *
 * Kirish va ro'yxat faqat **telefon** orqali (email oqimi olib tashlangan), muqobil usul —
 * Google.
 *
 * commonMain'da yashaydi, shu bois Android va iOS'da bir xil ishlaydi.
 */
@Composable
fun AuthNavHost(
    onExit: (() -> Unit)? = null,
    vm: AuthFlowViewModel = koinViewModel(),
) {
    // Local sessiya keshi + tugallanmagan ro'yxat bosqichi: qaysi ekrandan boshlashni
    // ViewModel hal qiladi.
    val start by vm.start.collectAsStateWithLifecycle()
    val resolved = start ?: run {
        BootSplash() // kesh o'qilmaguncha qisqa splash
        return
    }

    val nav = rememberNavController()
    val state by vm.state.collectAsStateWithLifecycle()

    // MUHIM — boshlang'ich yo'nalish FAQAT birinchi qiymatdan olinadi va keyin o'zgarmaydi.
    // `NavHost` grafni `startDestination` bo'yicha eslab qoladi: qiymat oqim o'rtasida
    // o'zgarsa graf QAYTA quriladi va navigatsiya stack'i yangi boshlang'ich ekranga
    // tashlanadi. Aynan shu sabab ro'yxatdan o'tishда `register` sessiya ochishi bilan
    // ekran "Mening biznesларim"ga sakrab ketar, SMS kod ekrani esa uning ustidan
    // ochilardi — foydalanuvchi tasdiqlamasdan ilova ichiga tushib qolardi.
    val startDestination = rememberSaveable {
        when (resolved) {
            AuthStart.LOGIN -> Route.LOGIN
            AuthStart.VERIFY_PHONE -> Route.VERIFY_PHONE
            AuthStart.ACCOUNT_SETUP -> Route.ACCOUNT_SETUP
            AuthStart.HOME -> Route.HOME
        }
    }

    // Bir martalik hodisalar navigatsiyani boshqaradi (async auth natijalari).
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                AuthEvent.PhoneVerificationSent -> nav.navigate(Route.VERIFY_PHONE) {
                    launchSingleTop = true
                }
                // Kod tasdiqlandi — orqaga qaytishда OTP ekrani qolmasin (kod allaqachon ishlatilgan).
                AuthEvent.AccountSetupRequired -> nav.navigate(Route.ACCOUNT_SETUP) {
                    popUpTo(Route.VERIFY_PHONE) { inclusive = true }
                    launchSingleTop = true
                }
                AuthEvent.ResetCodeSent -> nav.navigate(Route.RESET_CODE) { launchSingleTop = true }
                // Parol yangilandi — kirish ekraniga qaytamiz (yangi parol bilan kiradi).
                AuthEvent.PasswordReset -> nav.navigate(Route.LOGIN) {
                    popUpTo(Route.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
                // Ro'yxat bekor qilindi — butun oqimni stackdan olib tashlaymiz.
                AuthEvent.SignupCancelled -> nav.navigate(Route.LOGIN) {
                    // Butun grafni tozalaymiz — ro'yxat ekranlariga orqaga qaytish yo'q.
                    popUpTo(Route.GRAPH) { inclusive = true }
                    launchSingleTop = true
                }
                is AuthEvent.Authenticated -> nav.navigate(Route.HOME) {
                    // Auth oqimi tugadi — orqaga bosilganда unga qaytib bo'lmasin.
                    popUpTo(Route.GRAPH) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = nav, startDestination = startDestination, route = Route.GRAPH) {
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
                onRegister = { nav.navigate(Route.REGISTER) { launchSingleTop = true } },
                // Google oqimi auth modulida (`rememberGoogleSignIn`), ekran esa business
                // modulida — shu sabab tugma slot sifatida uzatiladi.
                googleButton = { GoogleSignInButton(vm) },
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

        // Tasdiqlash — ro'yxatning majburiy qadami. Orqaga qaytish REGISTER ga EMAS
        // (hisob serverда allaqachon ochilgan, qayta ro'yxat "raqam band" der edi), balki
        // oqimni bekor qilib kirish ekraniga olib chiqadi.
        composable(Route.VERIFY_PHONE) {
            OtpScreen(
                state = state,
                vm = vm,
                onBack = vm::cancelSignup,
                onVerify = vm::verifyPhone,
                onResend = vm::resendCode,
            )
        }

        composable(Route.ACCOUNT_SETUP) {
            AccountSetupScreen(
                state = state,
                vm = vm,
                onBack = vm::cancelSignup,
                onContinue = vm::completeAccountSetup,
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
