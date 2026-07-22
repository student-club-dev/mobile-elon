package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/**
 * Bo'sh ro'yxat holati — gradientli katta ikonka, sarlavha, izoh va bitta harakat tugmasi.
 *
 * "Hali e'lon yo'q", "Hali biznes yo'q" kabi ekranlar shu bitta komponentдан chiziladi;
 * ilgari har biri gradientni va `Color.White` ikonka rangini qo'lda yozardi.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    palette: AppPalette = appPalette,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shape = RoundedCornerShape(26.dp)
        Box(
            Modifier.size(84.dp)
                .shadow(18.dp, shape, spotColor = palette.primary.copy(alpha = 0.5f))
                .clip(shape)
                .background(palette.primaryBrush),
            contentAlignment = Alignment.Center,
        ) {
            // Gradient FONI USTIDA — bu yerda rang palitradan emas, kontent rangidan olinadi.
            Icon(icon, null, tint = palette.onPrimary, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(
            title,
            style = AppType.sectionTitle.copy(
                fontWeight = AppType.screenTitle.fontWeight,
                color = palette.ink,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = AppType.link.copy(lineHeight = 18.sp, color = palette.inkMuted),
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            PrimaryButton(actionText, onAction, trailingIcon = actionIcon, palette = palette)
        }
    }
}

/**
 * Xato holati — yumshoq aksent fonli ikonka, sabab matni va "Qayta urinish" tugmasi.
 *
 * [EmptyState] dan farqi: bu yerда gradient yo'q (xato — bayram emas) va matn
 * foydalanuvchiga tushunarli sabab bo'lgani uchun tana matni uslubida beriladi.
 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.Close,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    palette: AppPalette = appPalette,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shape = RoundedCornerShape(22.dp)
        Box(
            Modifier.size(72.dp).clip(shape).background(palette.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = palette.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = AppType.bodyStrong.copy(color = palette.ink),
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            PrimaryButton(actionText, onAction, palette = palette)
        }
    }
}
