package dev.feature.business.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GlassCard
import dev.core.uikit.component.GlassRow
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType

/**
 * `BusinessAccountScreen` ning ichki bo'laklari — statistika kartasi, ma'lumot qatori va
 * menyu qatori. Ekran fayli faqat tuzilishni saqlab qolishi uchun alohida chiqarildi.
 *
 * O'lchamlar handoff'dagi "SCREEN 2: Biznes profili" dan olingan.
 */

/** Handoff: menyu va chiqish qatorlarining ichki paddingi. */
internal val RowPaddingHorizontal = 18.dp
internal val RowPaddingVertical = 17.dp

/** Bitta ko'rsatkich — ikonka, qiymat va yorliq ustma-ust. Uch dona yonma-yon turadi. */
@Composable
internal fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    palette: AppPalette,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        palette = palette,
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(AppSize.iconLg))
        Spacer(Modifier.height(8.dp))
        Text(value, style = AppType.statValue.copy(color = palette.ink))
        Spacer(Modifier.height(2.dp))
        Text(label, style = AppType.caption.copy(color = palette.inkMuted))
    }
}

/**
 * "Biznes turi — Kafe va Restoran" ko'rinishidagi ma'lumot qatori (chegarasiz, karta ichida).
 *
 * [showDivider] — qator ostidagi nozik chiziq. Oxirgi qatorda chizilmaydi, aks holda karta
 * ichida "osilib qolgan" chiziq paydo bo'lardi.
 */
@Composable
internal fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    palette: AppPalette,
    showDivider: Boolean = false,
) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Icon(icon, null, tint = palette.primary, modifier = Modifier.size(AppSize.iconMd))
            Text(
                label,
                // Handoff: 14sp / 600 — kulrang yorliq.
                style = AppType.fieldLabel.copy(
                    fontWeight = AppType.secondary.fontWeight,
                    color = palette.inkMuted,
                ),
                modifier = Modifier.weight(1f),
            )
            // Handoff: qiymat 15sp / 800.
            Text(value, style = AppType.button.copy(color = palette.ink))
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(palette.divider))
    }
}

/** Bosiladigan menyu qatori — "Mening e'lonlarim", "Sozlamalar". */
@Composable
internal fun AccountRow(icon: ImageVector, title: String, palette: AppPalette, onClick: () -> Unit) {
    GlassRow(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = RowPaddingHorizontal,
            vertical = RowPaddingVertical,
        ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        palette = palette,
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(20.dp))
        Text(
            title,
            style = AppType.rowTitle.copy(color = palette.ink),
            modifier = Modifier.weight(1f),
        )
        Icon(AppIcons.ChevronRight, null, tint = palette.inkMuted, modifier = Modifier.size(17.dp))
    }
}
