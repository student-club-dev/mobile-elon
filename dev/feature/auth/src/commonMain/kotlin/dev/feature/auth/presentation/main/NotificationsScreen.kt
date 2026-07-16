package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.AppNotification
import dev.core.domain.model.NotificationType
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
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
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(palette.glass)
                    .border(1.dp, palette.border, RoundedCornerShape(12.dp)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(AppIcons.ArrowLeft, "Orqaga", tint = palette.ink, modifier = Modifier.size(18.dp)) }
            Text("Bildirishnomalar", style = TextStyle(fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Black, color = palette.ink), modifier = Modifier.weight(1f))
            if (state.unreadCount > 0) {
                Text(
                    "Hammasini o'qildi",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.primary),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { vm.markAllRead() }.padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        if (state.items.isEmpty()) {
            EmptyNotifications(palette)
        } else {
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
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
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (n.read) palette.glass else palette.primary.copy(alpha = 0.06f))
            .border(1.dp, if (n.read) palette.border else palette.primary.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(n.title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink), modifier = Modifier.weight(1f))
                if (!n.read) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(999.dp)).background(palette.primary))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(n.body, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = palette.inkMuted))
            Spacer(Modifier.height(4.dp))
            Text(n.timeLabel, style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, fontWeight = FontWeight.Bold, color = palette.inkFaint))
        }
    }
}

@Composable
private fun EmptyNotifications(palette: AppPalette) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(palette.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Icon(AppIcons.Bell, null, tint = palette.primary, modifier = Modifier.size(34.dp)) }
            Text("Hozircha bildirishnoma yo'q", style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black, color = palette.ink))
            Text(
                "Yangi elonlar, ish takliflari va xabarlar shu yerda ko'rinadi.",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = palette.inkMuted, textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 40.dp),
            )
        }
    }
}

private fun NotificationType.iconAndAccent(palette: AppPalette): Pair<ImageVector, Color> = when (this) {
    NotificationType.JOB -> AppIcons.Briefcase to Color(0xFF2563EB)
    NotificationType.DISCOUNT -> AppIcons.Tag to Color(0xFFF97316)
    NotificationType.AD -> AppIcons.FileText to Color(0xFF059669)
    NotificationType.CHAT -> AppIcons.MessageSquare to palette.primary
    NotificationType.SYSTEM -> AppIcons.Bell to Color(0xFFBE185D)
}
