package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
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
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 26.dp),
            )
        }
    }
}

/** Yuqori panel: back tugmasi + "E'lonlarim" sarlavhasi + gradient profil tugmasi. */
@Composable
private fun BusinessTopBar(
    onBack: () -> Unit,
    onProfile: () -> Unit,
    palette: AppPalette,
) {
    Column(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 52.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Orqaga qaytish — "Bizneslarim" ro'yxatiga.
            Box(
                Modifier.size(42.dp)
                    .clip(CircleShape).background(palette.glass)
                    .border(1.dp, palette.border, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.ArrowLeft, "Orqaga", tint = palette.ink, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "BIZNES MARKAZI",
                    style = TextStyle(
                        fontFamily = AppFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp,
                        color = palette.primary,
                    ),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "E'lonlarim",
                    style = TextStyle(
                        fontFamily = AppFontFamily,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.ink,
                    ),
                )
            }
            // Gradient profil tugmasi.
            Box(
                Modifier.size(46.dp)
                    .shadow(10.dp, CircleShape, spotColor = palette.primary.copy(alpha = 0.5f))
                    .clip(CircleShape).background(palette.primaryBrush)
                    .clickable(onClick = onProfile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Store, "Profil", tint = Color.White, modifier = Modifier.size(21.dp))
            }
        }
        // Chegirma/E'lon segment olib tashlandi — hammasi (chegirma + oddiy) bitta ro'yxatda.
    }
}

/** O'ng-past extended FAB — yangi e'lon (chegirma/oddiy forma ichida tanlanadi). */
@Composable
private fun CreateFab(
    palette: AppPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = palette.primary.copy(alpha = 0.6f))
            .clip(RoundedCornerShape(20.dp)).background(palette.primaryBrush)
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 22.dp, top = 15.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(AppIcons.Plus, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Text(
            "E'lon",
            style = TextStyle(
                fontFamily = AppFontFamily,
                fontSize = 14.5f.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            ),
        )
    }
}
