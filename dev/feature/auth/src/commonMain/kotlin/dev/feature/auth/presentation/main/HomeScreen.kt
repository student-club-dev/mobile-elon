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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GradientHeader
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_chat_title
import dev.core.uikit.resources.auth_home_greeting
import dev.core.uikit.resources.auth_notifications_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.main.components.DiscountsSection
import dev.feature.auth.presentation.main.components.JobsSection
import dev.feature.auth.presentation.main.components.StudentsSection
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenDiscounts: () -> Unit = {},
    onOpenJobs: () -> Unit = {},
    onOpenStudents: () -> Unit = {},
    vm: HomeViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        HomeHeader(state, palette, onOpenProfile, onOpenChat, onOpenNotifications)
        Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 18.dp)) {
            DiscountsSection(state.categories, state.featured, palette, onOpenDiscounts)
            Spacer(Modifier.height(AppSpacing.xl))
            JobsSection(state.jobs, palette, onOpenJobs) { vm.toggleBookmark(it) }
            Spacer(Modifier.height(AppSpacing.xl))
            StudentsSection(state.students, palette, onOpenStudents) { vm.toggleFriend(it) }
            Spacer(Modifier.height(110.dp)) // pastki navigatsiya uchun joy
        }
    }
}

// ---------------------------------------------------------------------------
// Header — gradient blok. Ustidagi kontent (matn/ikonka) palitraga bog'liq emas:
// binafsha gradient har ikkala rejimda ham quyuq, shuning uchun oq bo'lib qoladi.
// ---------------------------------------------------------------------------
@Composable
private fun HomeHeader(
    state: HomeUiState,
    palette: AppPalette,
    onOpenProfile: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    GradientHeader(palette = palette) {
        Row(
            Modifier.padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = AppSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(46.dp).clip(AppRadius.pill)
                    .background(Color.White.copy(alpha = 0.20f))
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    state.userName.take(1).uppercase(),
                    style = AppType.button.copy(fontSize = 18.sp, color = Color.White),
                )
            }
            Spacer(Modifier.width(AppSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.auth_home_greeting),
                    style = AppType.link.copy(color = Color.White.copy(alpha = 0.85f)),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    state.userName,
                    style = AppType.screenTitle.copy(fontSize = 18.sp, letterSpacing = 0.sp, color = Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val badge = listOfNotNull(state.universityMonogram, state.courseLabel).joinToString(" · ")
                if (badge.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.clip(AppRadius.pill).background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcons.GraduationCap, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            badge,
                            style = AppType.caption.copy(
                                fontSize = 10.5f.sp,
                                fontWeight = AppType.label.fontWeight,
                                color = Color.White,
                            ),
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                HeaderAction(AppIcons.MessageSquare, stringResource(Res.string.auth_chat_title), onOpenChat)
                Box {
                    HeaderAction(AppIcons.Bell, stringResource(Res.string.auth_notifications_title), onOpenNotifications)
                    if (state.hasUnreadNotifications) {
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(10.dp).size(7.dp)
                                .clip(AppRadius.pill).background(palette.badge),
                        )
                    }
                }
            }
        }
    }
}

/** Gradient header ustidagi kvadrat ikonka tugmasi — shaffof oq fon. */
@Composable
private fun HeaderAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}
