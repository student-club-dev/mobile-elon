package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import dev.core.uikit.locale.LocaleRestore
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.core.uikit.component.EdgeSwipeBack
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.rowShadow
import dev.feature.discounts.presentation.AddBusinessScreen
import dev.feature.discounts.presentation.MyBusinessesScreen
import dev.feature.discounts.presentation.MyListingsScreen
import dev.feature.discounts.presentation.PostListingScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.feature.profile.presentation.EditProfileScreen
import dev.feature.profile.presentation.ProfileViewModel
import dev.feature.profile.presentation.components.ProfileAvatar
import org.koin.compose.viewmodel.koinViewModel

/** Sarlavhadagi hisob tugmasi bilan bir o'lchamda (`MyBusinessesScreen`). */
private val AccountAvatarSize = 46.dp

private const val BUSINESSES = "businesses"
private const val ADD_BUSINESS = "add_business"
private const val LISTINGS = "listings"
private const val POST_LISTING = "post_listing"
private const val PROFILE = "business_profile"
private const val PROFILE_EDIT = "profile_edit"
private const val SETTINGS = "settings"
private const val MESSAGES = "messages"
private const val SUPPORT = "support"

/**
 * Til almashgandan keyin tiklanadigan marshrutlar — argumentsizlari.
 *
 * Biznes e'lonlari va e'lon formasi ro'yxatga kirmaydi: ular `businessId`/`listingId` ga
 * bog'liq va marshrut shablonining o'zi bilan qayta ochilmaydi.
 */
private val RESTORABLE_ROUTES = setOf(PROFILE, PROFILE_EDIT, SETTINGS)

/**
 * Biznesmen karkasi. Oqim: **Bizneslarim** (ro'yxat + "+") → biznesga bosilса uning
 * **e'lonlari** (chegirma + oddiy — bitta ro'yxatда) → "+E'lon" bilan e'lon qo'shish.
 * E'lon turi biznesdан meros olinadi (tur tanlash grid'i yo'q); chegirma/oddiy esa forma
 * ichидаgi toggle. Talaba bo'limlari umuman yo'q.
 */
@Composable
fun BusinessShell(
    onLoggedOut: () -> Unit,
    // Sozlamalar ekrani auth modulida — shu slot orqali beriladi (business auth'ga bog'lanmaydi).
    // `onEditProfile` ni karkas beradi, chunki profil tahrirlash marshruti shu yerda.
    settingsContent: @Composable (onBack: () -> Unit, onEditProfile: () -> Unit) -> Unit,
    // Chat ekranlari ham auth modulida. Bog'liqlik faqat `auth -> business` yo'nalishida
    // bo'lgani uchun bu yerdan import qilib bo'lmaydi — ekranlar slot sifatida uzatiladi.
    messagesScreen: @Composable (onBack: () -> Unit) -> Unit = {},
    supportScreen: @Composable (onBack: () -> Unit) -> Unit = {},
) {
    val palette = appPalette
    val nav = rememberNavController()

    // Profil — "Biznes +" bosilганда raqam bor-yo'qligini shu yerdan bilamiz. Google bilan
    // kirilgan hisobda raqam bo'lmaydi, biznes esa tasdiqlangan raqamsiz yaratilmaydi.
    val profileVm: ProfileViewModel = koinViewModel()
    val profileState by profileVm.state.collectAsStateWithLifecycle()
    val hasPhone = !profileState.profile?.phoneNumber.isNullOrBlank()
    var accountGateOpen by remember { mutableStateOf(false) }

    // Til almashganда butun daraxt qayta yaratiladi va `NavHost` boshlang'ich ekranga
    // qaytadi. Foydalanuvchi Sozlamalarда tilni o'zgartirsa, o'sha yerда qolishi kerak —
    // shuning uchun joriy marshrutni kompozitsiyadan tashqarida saqlab boramiz va qayta
    // yaratilishда bir marta tiklaymiz.
    val backStack by nav.currentBackStackEntryAsState()
    LaunchedEffect(backStack) {
        backStack?.destination?.route?.let { LocaleRestore.lastRoute = it }
    }
    LaunchedEffect(Unit) {
        // Faqat oddiy (argumentsiz) marshrutlar tiklanadi: e'lonlar/forma ekranlari
        // argumentга bog'liq va ularni marshrut shabloni bo'yicha qayta ochib bo'lmaydi.
        LocaleRestore.consumeRoute()
            ?.takeIf { it in RESTORABLE_ROUTES }
            ?.let { route -> nav.navigate(route) { launchSingleTop = true } }
    }

    // iOS'da chap chetdan surib orqaga qaytish (Androidda hech narsa qilmaydi).
    EdgeSwipeBack(onBack = { if (nav.previousBackStackEntry != null) nav.popBackStack() }) {
        Box(Modifier.fillMaxSize().background(palette.bgBrush)) {
            NavHost(navController = nav, startDestination = BUSINESSES, modifier = Modifier.fillMaxSize()) {
                // 1. Bosh ekran — Mening bizneslarim (ro'yxat + "+" tugma).
                composable(BUSINESSES) {
                    MyBusinessesScreen(
                        // Sarlavha chapida — foydalanuvchining o'z rasmi (yo'q bo'lsa ismining
                        // bosh harfi). Profil ma'lumoti shu karkasda, shuning uchun tugma
                        // ekranga slot sifatida beriladi.
                        accountButton = { onClick ->
                            ProfileAvatar(
                                name = profileState.name,
                                size = AccountAvatarSize,
                                fontSize = 18.sp,
                                palette = palette,
                                avatarUrl = profileState.profile?.avatarUrl,
                                modifier = Modifier
                                    .rowShadow(AppRadius.pill)
                                    .clip(CircleShape)
                                    .clickable(onClick = onClick),
                            )
                        },
                        onOpenBusiness = { biz -> nav.navigate("$LISTINGS/${biz.id}") { launchSingleTop = true } },
                        onEditBusiness = { biz -> nav.navigate("$ADD_BUSINESS?businessId=${biz.id}") { launchSingleTop = true } },
                        // Raqam yo'q bo'lsa avval hisobga kirish oynasi — forma keyin ochiladi.
                        onAddBusiness = {
                            if (hasPhone) nav.navigate(ADD_BUSINESS) { launchSingleTop = true }
                            else accountGateOpen = true
                        },
                        onProfile = { nav.navigate(PROFILE) { launchSingleTop = true } },
                        onMessages = { nav.navigate(MESSAGES) { launchSingleTop = true } },
                        onSupport = { nav.navigate(SUPPORT) { launchSingleTop = true } },
                    )
                }
                // 2. Biznes qo'shish / tahrirlash (nom, telefon, tur, lokatsiya).
                composable(
                    route = "$ADD_BUSINESS?businessId={businessId}",
                    arguments = listOf(navArgument("businessId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                ) { entry ->
                    AddBusinessScreen(
                        onClose = { nav.popBackStack() },
                        onSaved = { nav.popBackStack() },
                        businessId = entry.arguments?.getString("businessId"),
                        // Backend tasdiqlangan raqam talab qilsa — raqam + SMS kod oynasi.
                        phoneGate = { onVerified, onCancel ->
                            BusinessPhoneGate(onVerified = onVerified, onCancel = onCancel)
                        },
                    )
                }
                // 3. Bitta biznesning e'lonlari.
                composable(
                    route = "$LISTINGS/{businessId}",
                    arguments = listOf(navArgument("businessId") { type = NavType.StringType }),
                ) { entry ->
                    val businessId = entry.arguments?.getString("businessId").orEmpty()
                    // Sarlavha (orqaga + logo + nom + tahrirlash) ekranning O'ZIDA chiziladi:
                    // statistika/amallar oynalari shundagina uning ustidan qoplanadi. Ilgari
                    // header shu yerda, karkasда edi va modal oyna ochilganda uning ustiga
                    // chiqib qolardi.
                    MyListingsScreen(
                        // launchSingleTop — tez ikki marta bosishда ekran ikki nusxada ochilmasin.
                        onCreate = { nav.navigate("$POST_LISTING?businessId=$businessId") { launchSingleTop = true } },
                        onEdit = { listingId ->
                            nav.navigate("$POST_LISTING?listingId=$listingId&businessId=$businessId") { launchSingleTop = true }
                        },
                        onBack = { nav.popBackStack() },
                        onEditBusiness = {
                            nav.navigate("$ADD_BUSINESS?businessId=$businessId") { launchSingleTop = true }
                        },
                        showHeader = false,
                        // Chegirma + oddiy e'lonlar — hammasi bitta ro'yxatda.
                        filterDiscount = null,
                        businessId = businessId,
                    )
                }
                composable(
                    route = "$POST_LISTING?listingId={listingId}&discount={discount}&businessId={businessId}",
                    arguments = listOf(
                        navArgument("listingId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("discount") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("businessId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    ),
                ) { entry ->
                    PostListingScreen(
                        onClose = { nav.popBackStack() },
                        onPublished = { nav.popBackStack() },
                        editListingId = entry.arguments?.getString("listingId"),
                        initialDiscount = entry.arguments?.getString("discount")?.toBooleanStrictOrNull(),
                        businessId = entry.arguments?.getString("businessId"),
                    )
                }
                composable(PROFILE) {
                    BusinessAccountScreen(
                        onBack = { nav.popBackStack() },
                        onEdit = { nav.navigate(PROFILE_EDIT) { launchSingleTop = true } },
                        onOpenSettings = { nav.navigate(SETTINGS) { launchSingleTop = true } },
                        onLoggedOut = onLoggedOut,
                    )
                }
                // Profildagi qalam SHAXSIY ma'lumotni tahrirlaydi (ism, familiya, telefon).
                // Ilgari u biznes tahrirlash ekranini ochardi — u yerda biznes nomi va turi
                // so'ralardi, holbuki bu foydalanuvchining o'z profili. Biznes esa
                // "Bizneslarim" ro'yxatidagi qalam orqali tahrirlanadi.
                composable(PROFILE_EDIT) {
                    EditProfileScreen(
                        onBack = { nav.popBackStack() },
                        // Biznes egasi talaba emas — universitet va kurs so'ralmaydi.
                    )
                }
                composable(SETTINGS) {
                    settingsContent(
                        { nav.popBackStack() },
                        { nav.navigate(PROFILE_EDIT) { launchSingleTop = true } },
                    )
                }
                // Xabarlar — suhbatlar ro'yxati (qo'llab-quvvatlashsiz).
                composable(MESSAGES) {
                    messagesScreen { nav.popBackStack() }
                }
                // Qo'llab-quvvatlash — ro'yxatsiz, to'g'ridan-to'g'ri support suhbati.
                composable(SUPPORT) {
                    supportScreen { nav.popBackStack() }
                }
            }

            // O'ng-past suzuvchi "+ E'lon" tugmasi OLIB TASHLANDI: bo'sh holatда "E'lon
            // qo'shish" bilan ikkilanardi, ro'yxat ustida esa modal oynalar USTIDAN chizilib
            // qolardi. Endi tugma ekranning o'zida, biznes sarlavhasi ostida turadi.

            // Raqamsiz hisobda "Biznes +" bosilgan — mavjud hisobga kirish oynasi.
            // Kirgach forma darhol ochiladi, foydalanuvchi "+" ni qayta bosmaydi.
            if (accountGateOpen) {
                BusinessAccountGate(
                    onLoggedIn = {
                        accountGateOpen = false
                        nav.navigate(ADD_BUSINESS) { launchSingleTop = true }
                    },
                    onCancel = { accountGateOpen = false },
                )
            }
        }
    }
}
