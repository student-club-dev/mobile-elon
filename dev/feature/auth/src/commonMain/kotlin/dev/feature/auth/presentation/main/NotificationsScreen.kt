package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.AppNotification
import dev.core.domain.model.NotificationType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_notifications_empty_body
import dev.core.uikit.resources.auth_notifications_empty_title
import dev.core.uikit.resources.auth_notifications_mark_all_read
import dev.core.uikit.resources.auth_notifications_title
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.rowShadow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Bildirishnomalar ekrani (C1). Local DB'dan (`NotificationEntity`) real ro'yxat.
 * Bosilganda o'qilgan deb belgilanadi; "Hammasini o'qildi" — barchasini.
 */
@Composable
fun NotificationsScreen(onBack: () -> Unit, vm: NotificationsViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            BackButton(onBack, contentDescription = stringResource(Res.string.common_back), iconSize = 18.dp)
            Text(
                stringResource(Res.string.auth_notifications_title),
                style = AppType.topBarTitle.copy(color = palette.ink),
                modifier = Modifier.weight(1f),
            )
            if (state.unreadCount > 0) {
                Text(
                    stringResource(Res.string.auth_notifications_mark_all_read),
                    style = AppType.fieldLabel.copy(color = palette.primary),
                    modifier = Modifier.clip(AppRadius.sm).clickable { vm.markAllRead() }
                        .padding(horizontal = 6.dp, vertical = AppSpacing.xs),
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        if (state.items.isEmpty()) {
            EmptyNotifications(palette)
        } else {
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(state.items, key = { it.id }) { n ->
                    NotificationRow(n, palette) { vm.markRead(n.id) }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, palette: AppPalette, onClick: () -> Unit) {
    val (icon, accent) = n.type.iconAndAccent(palette)
    val shape = AppRadius.row
    Row(
        // Chegara yo'q: qatorni soya ajratadi, o'qilmagani esa ochiq ko'k fon bilan belgilanadi.
        Modifier.fillMaxWidth().rowShadow(shape).clip(shape)
            .background(if (n.read) palette.card else palette.accentBg)
            .clickable(onClick = onClick).padding(AppSpacing.lg),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        IconTile(
            icon,
            tint = accent,
            background = accent.copy(alpha = 0.14f),
            size = 38.dp,
            iconSize = AppSize.iconSm,
            shape = AppRadius.sm,
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    n.title,
                    style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                    modifier = Modifier.weight(1f),
                )
                if (!n.read) {
                    Box(Modifier.size(AppSpacing.sm).clip(AppRadius.pill).background(palette.primary))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                n.body,
                style = AppType.fieldLabel.copy(fontWeight = AppType.subtitle.fontWeight, color = palette.inkMuted),
            )
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                n.timeLabel,
                style = AppType.caption.copy(fontWeight = AppType.label.fontWeight, color = palette.inkFaint),
            )
        }
    }
}

@Composable
private fun EmptyNotifications(palette: AppPalette) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            IconTile(
                AppIcons.Bell,
                tint = palette.primary,
                background = palette.primary.copy(alpha = 0.12f),
                size = 72.dp,
                iconSize = 34.dp,
                shape = AppRadius.card,
            )
            Text(
                stringResource(Res.string.auth_notifications_empty_title),
                style = AppType.cardTitle.copy(color = palette.ink),
            )
            Text(
                stringResource(Res.string.auth_notifications_empty_body),
                style = AppType.subtitle.copy(color = palette.inkMuted, textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 40.dp),
            )
        }
    }
}

/** Bildirishnoma turiga mos ikonka va modul aksenti. */
private fun NotificationType.iconAndAccent(palette: AppPalette): Pair<ImageVector, Color> = when (this) {
    NotificationType.JOB -> AppIcons.Briefcase to palette.accentStudy
    NotificationType.DISCOUNT -> AppIcons.Tag to palette.accentFood
    NotificationType.AD -> AppIcons.FileText to palette.success
    NotificationType.CHAT -> AppIcons.MessageSquare to palette.primary
    NotificationType.SYSTEM -> AppIcons.Bell to palette.accentBeauty
}
