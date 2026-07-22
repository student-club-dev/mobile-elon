package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.EdgeSwipeBack
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_post_ad
import dev.core.uikit.resources.auth_post_ad_short
import dev.core.uikit.resources.auth_tab_discounts
import dev.core.uikit.resources.auth_tab_home
import dev.core.uikit.resources.auth_tab_jobs
import dev.core.uikit.resources.auth_tab_students
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.cardShadow
import dev.core.uikit.theme.ctaShadow
import dev.feature.profile.presentation.EditProfileScreen
import dev.feature.profile.presentation.ProfileScreen
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Pastki navigatsiya bo'limlari. Yorliq matni resursdan olinadi. */
private enum class StudentTab(val route: String, val label: StringResource, val icon: ImageVector) {
    HOME("home", Res.string.auth_tab_home, AppIcons.Home),
    DISCOUNTS("discounts", Res.string.auth_tab_discounts, AppIcons.Tag),
    JOBS("jobs", Res.string.auth_tab_jobs, AppIcons.Briefcase),
    STUDENTS("students", Res.string.auth_tab_students, AppIcons.Users),
}

private const val POST_AD = "post_ad"
private const val PROFILE = "profile"
private const val CHAT = "chat"
private const val NOTIFICATIONS = "notifications"
private const val EDIT_PROFILE = "edit_profile"
private const val SETTINGS = "settings"

private val tabRoutes = StudentTab.entries.map { it.route }.toSet()

/**
 * Talaba karkasi — pastki navigatsiya (Home / Chegirma / Ishlar / Student) + markaziy "Elon" FAB
 * (talaba e'lonlari: ish, sotuv, xizmat). Biznesmen chegirma e'lonlari bu yerda YO'Q — ular
 * BusinessShell da.
 */
@Composable
fun StudentShell(onLoggedOut: () -> Unit) {
    val palette = appPalette
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: StudentTab.HOME.route

    val selectTab: (String) -> Unit = { route ->
        nav.navigate(route) {
            popUpTo(StudentTab.HOME.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // iOS'da chap chetdan surib orqaga qaytish (Androidda hech narsa qilmaydi).
    EdgeSwipeBack(onBack = { if (nav.previousBackStackEntry != null) nav.popBackStack() }) {
        Box(Modifier.fillMaxSize().background(palette.bgBrush)) {
            NavHost(navController = nav, startDestination = StudentTab.HOME.route, modifier = Modifier.fillMaxSize()) {
                composable(StudentTab.HOME.route) {
                    HomeScreen(
                        onOpenProfile = { nav.navigate(PROFILE) },
                        onOpenChat = { nav.navigate(CHAT) },
                        onOpenNotifications = { nav.navigate(NOTIFICATIONS) },
                        onOpenDiscounts = { selectTab(StudentTab.DISCOUNTS.route) },
                        onOpenJobs = { selectTab(StudentTab.JOBS.route) },
                        onOpenStudents = { selectTab(StudentTab.STUDENTS.route) },
                    )
                }
                composable(StudentTab.DISCOUNTS.route) {
                    // Talaba faqat chegirmalarni ko'radi (e'lon qo'yish — biznesmen shell'ida).
                    DiscountsScreen()
                }
                composable(StudentTab.JOBS.route) { JobsScreen() }
                composable(StudentTab.STUDENTS.route) { StudentsScreen() }
                composable(
                    route = "$POST_AD?adId={adId}",
                    arguments = listOf(navArgument("adId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                ) { entry ->
                    PostAdScreen(onClose = { nav.popBackStack() }, editAdId = entry.arguments?.getString("adId"))
                }
                composable(PROFILE) {
                    ProfileScreen(
                        onBack = { nav.popBackStack() },
                        onLoggedOut = onLoggedOut,
                        onEditProfile = { nav.navigate(EDIT_PROFILE) },
                        onOpenSettings = { nav.navigate(SETTINGS) },
                        onEditAd = { adId -> nav.navigate("$POST_AD?adId=$adId") },
                        // Talabada biznes bo'limi ko'rinmaydi.
                        showMyBusiness = false,
                    )
                }
                composable(CHAT) { ChatScreen(onBack = { nav.popBackStack() }) }
                composable(NOTIFICATIONS) { NotificationsScreen(onBack = { nav.popBackStack() }) }
                composable(EDIT_PROFILE) { EditProfileScreen(onBack = { nav.popBackStack() }) }
                composable(SETTINGS) {
                    SettingsScreen(
                        onBack = { nav.popBackStack() },
                        onEditProfile = { nav.navigate(EDIT_PROFILE) },
                        onLoggedOut = onLoggedOut,
                    )
                }
            }

            if (current in tabRoutes) {
                BottomBar(
                    current = current,
                    onSelect = selectTab,
                    onFab = { nav.navigate(POST_AD) { launchSingleTop = true } },
                    palette = palette,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        }
    }

    @Composable
    private fun BottomBar(
        current: String,
        onSelect: (String) -> Unit,
        onFab: () -> Unit,
        palette: AppPalette,
        modifier: Modifier = Modifier,
    ) {
        // Panel yuqori burchaklari yumaloq, uni kontentdan soya ajratadi (chegara emas).
        val barShape = RoundedCornerShape(topStart = AppSpacing.section, topEnd = AppSpacing.section)
        Box(modifier.fillMaxWidth().height(88.dp)) {
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .height(66.dp)
                    .cardShadow(barShape)
                    .clip(barShape)
                    .background(palette.card)
                    .padding(horizontal = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavBarItem(StudentTab.HOME, current, onSelect, palette, Modifier.weight(1f))
                NavBarItem(StudentTab.DISCOUNTS, current, onSelect, palette, Modifier.weight(1f))
                Spacer(Modifier.weight(1f)) // markaziy FAB uchun joy
                NavBarItem(StudentTab.JOBS, current, onSelect, palette, Modifier.weight(1f))
                NavBarItem(StudentTab.STUDENTS, current, onSelect, palette, Modifier.weight(1f))
            }

            // Markaziy "Elon" FAB — talaba e'loni (ish/sotuv/xizmat)
            Column(
                Modifier.align(Alignment.TopCenter).offset(y = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(AppSize.iconButtonLarge)
                        .ctaShadow(AppRadius.lg)
                        .clip(AppRadius.lg)
                        .background(palette.primaryBrush)
                        .clickable(onClick = onFab),
                    contentAlignment = Alignment.Center,
                ) {
                    // Gradient USTIDAGI ikonka — har ikkala rejimda oq.
                    Icon(
                        AppIcons.Plus,
                        stringResource(Res.string.auth_post_ad),
                        tint = palette.onPrimary,
                        modifier = Modifier.size(AppSize.iconFab),
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(Res.string.auth_post_ad_short),
                    style = AppType.navLabel.copy(color = palette.primary),
                )
            }
        }
    }

    @Composable
    private fun NavBarItem(
        tab: StudentTab,
        current: String,
        onSelect: (String) -> Unit,
        palette: AppPalette,
        modifier: Modifier = Modifier,
    ) {
        val active = current == tab.route
        val tint = if (active) palette.primary else palette.inkFaint
        val label = stringResource(tab.label)
        Column(
            modifier.clip(AppRadius.md).clickable { onSelect(tab.route) }.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(tab.icon, label, tint = tint, modifier = Modifier.size(AppSize.iconLg))
            Text(
                label,
                style = AppType.navLabel.copy(
                    fontWeight = if (active) AppType.button.fontWeight else AppType.label.fontWeight,
                    color = tint,
                ),
            )
    }
}
