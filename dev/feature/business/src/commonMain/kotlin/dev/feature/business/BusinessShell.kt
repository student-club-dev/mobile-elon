package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette
import dev.feature.business.components.BusinessTopBar
import dev.feature.business.components.CreateFab
import dev.feature.discounts.presentation.AddBusinessScreen
import dev.feature.discounts.presentation.MyBusinessesScreen
import dev.feature.discounts.presentation.MyListingsScreen
import dev.feature.discounts.presentation.PostListingScreen

private const val BUSINESSES = "businesses"
private const val ADD_BUSINESS = "add_business"
private const val LISTINGS = "listings"
private const val POST_LISTING = "post_listing"
private const val PROFILE = "business_profile"
private const val BUSINESS_EDIT = "business_edit"
private const val SETTINGS = "settings"

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
    settingsContent: @Composable (onBack: () -> Unit) -> Unit,
) {
    val palette = appPalette
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: BUSINESSES

    Box(Modifier.fillMaxSize().background(palette.bgBrush)) {
        NavHost(navController = nav, startDestination = BUSINESSES, modifier = Modifier.fillMaxSize()) {
            // 1. Bosh ekran — Mening bizneslarim (ro'yxat + "+" tugma).
            composable(BUSINESSES) {
                MyBusinessesScreen(
                    onOpenBusiness = { biz -> nav.navigate("$LISTINGS/${biz.id}") },
                    onEditBusiness = { biz -> nav.navigate("$ADD_BUSINESS?businessId=${biz.id}") },
                    onAddBusiness = { nav.navigate(ADD_BUSINESS) },
                    onProfile = { nav.navigate(PROFILE) },
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
                )
            }
            // 3. Bitta biznesning e'lonlari.
            composable(
                route = "$LISTINGS/{businessId}",
                arguments = listOf(navArgument("businessId") { type = NavType.StringType }),
            ) { entry ->
                val businessId = entry.arguments?.getString("businessId").orEmpty()
                Column(Modifier.fillMaxSize()) {
                    BusinessTopBar(
                        onBack = { nav.popBackStack() },
                        onProfile = { nav.navigate(PROFILE) },
                        palette = palette,
                    )
                    MyListingsScreen(
                        // launchSingleTop — tez ikki marta bosishда ekran ikki nusxada ochilmasin.
                        onCreate = { nav.navigate("$POST_LISTING?businessId=$businessId") { launchSingleTop = true } },
                        onEdit = { listingId ->
                            nav.navigate("$POST_LISTING?listingId=$listingId&businessId=$businessId") { launchSingleTop = true }
                        },
                        showHeaderCreate = false,
                        showHeader = false,
                        // Chegirma + oddiy e'lonlar — hammasi bitta ro'yxatda.
                        filterDiscount = null,
                        businessId = businessId,
                    )
                }
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
                    onEdit = { nav.navigate(BUSINESS_EDIT) },
                    onOpenListings = { nav.popBackStack() },
                    onOpenSettings = { nav.navigate(SETTINGS) },
                    onLoggedOut = onLoggedOut,
                )
            }
            composable(BUSINESS_EDIT) {
                BusinessEditScreen(onBack = { nav.popBackStack() })
            }
            composable(SETTINGS) {
                settingsContent { nav.popBackStack() }
            }
        }

        // O'ng-past: "Yangi" extended pill — faqat bitta biznes e'lonlari ekranida.
        // (Bizneslarim ro'yxati o'z "+" tugmasiga ega.)
        if (current.startsWith("$LISTINGS/")) {
            val businessId = backStack?.arguments?.getString("businessId").orEmpty()
            CreateFab(
                palette = palette,
                onClick = { nav.navigate("$POST_LISTING?businessId=$businessId") { launchSingleTop = true } },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = AppSpacing.screenBottom),
            )
        }
    }
}
