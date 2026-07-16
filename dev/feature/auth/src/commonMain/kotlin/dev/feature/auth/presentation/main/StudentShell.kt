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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.profile.presentation.EditProfileScreen
import dev.feature.profile.presentation.ProfileScreen

private enum class StudentTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", AppIcons.Home),
    DISCOUNTS("discounts", "Chegirma", AppIcons.Tag),
    JOBS("jobs", "Ishlar", AppIcons.Briefcase),
    STUDENTS("students", "Student", AppIcons.Users),
}

private const val POST_AD = "post_ad"
private const val PROFILE = "profile"
private const val CHAT = "chat"
private const val NOTIFICATIONS = "notifications"
private const val EDIT_PROFILE = "edit_profile"
private const val SETTINGS = "settings"
private const val CLUBS = "clubs"

private val tabRoutes = StudentTab.entries.map { it.route }.toSet()

/**
 * Talaba karkasi — pastki navigatsiya (Home / Chegirma / Ishlar / Student) + markaziy "Elon" FAB
 * (talaba e'lonlari: ish, sotuv, xizmat). Biznesmen chegirma e'lonlari bu yerda YO'Q — ular
 * [BusinessShell] da.
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

    Box(Modifier.fillMaxSize().background(palette.bgBrush)) {
        NavHost(navController = nav, startDestination = StudentTab.HOME.route, modifier = Modifier.fillMaxSize()) {
            composable(StudentTab.HOME.route) {
                HomeScreen(
                    onOpenProfile = { nav.navigate(PROFILE) },
                    onOpenChat = { nav.navigate(CHAT) },
                    onOpenNotifications = { nav.navigate(NOTIFICATIONS) },
                    onOpenClubs = { nav.navigate(CLUBS) },
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
            composable(CLUBS) { ClubsScreen(onBack = { nav.popBackStack() }) }
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

@Composable
private fun BottomBar(
    current: String,
    onSelect: (String) -> Unit,
    onFab: () -> Unit,
    palette: AppPalette,
    modifier: Modifier = Modifier,
) {
    val barColor = if (palette.dark) Color(0xFF1A1630) else Color.White
    Box(modifier.fillMaxWidth().height(88.dp)) {
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .height(66.dp)
                .background(barColor, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 8.dp),
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
                Modifier.size(54.dp).clip(RoundedCornerShape(18.dp))
                    .background(palette.primaryBrush)
                    .clickable(onClick = onFab),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Plus, "Elon berish", tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(3.dp))
            Text("Elon", style = TextStyle(fontFamily = AppFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = palette.primary))
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
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).clickable { onSelect(tab.route) }.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(tab.icon, tab.label, tint = tint, modifier = Modifier.size(21.dp))
        Text(tab.label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 9.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold, color = tint))
    }
}
