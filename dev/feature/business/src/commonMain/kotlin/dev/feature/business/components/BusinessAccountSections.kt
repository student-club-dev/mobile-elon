package dev.feature.business.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.sp
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
 */

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
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        palette = palette,
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(AppSize.iconMd))
        Text(
            value,
            style = AppType.sectionTitle.copy(
                fontSize = 19.sp,
                fontWeight = AppType.screenTitle.fontWeight,
                color = palette.ink,
            ),
        )
        Text(
            label,
            style = AppType.navLabel.copy(fontWeight = AppType.bodyStrong.fontWeight, color = palette.inkFaint),
        )
    }
}

/** "Biznes turi — Kafe va Restoran" ko'rinishidagi ma'lumot qatori (chegarasiz, karta ichida). */
@Composable
internal fun InfoRow(icon: ImageVector, label: String, value: String, palette: AppPalette) {
    Row(
        Modifier.fillMaxWidth().padding(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(AppSize.iconMd))
        Text(
            label,
            style = AppType.hint.copy(fontSize = 12.5f.sp, color = palette.inkFaint),
            modifier = Modifier.weight(1f),
        )
        Text(value, style = AppType.fieldLabel.copy(fontSize = 12.5f.sp, color = palette.ink))
    }
}

/** Bosiladigan menyu qatori — "Mening e'lonlarim", "Sozlamalar". */
@Composable
internal fun AccountRow(icon: ImageVector, title: String, palette: AppPalette, onClick: () -> Unit) {
    GlassRow(onClick = onClick, palette = palette) {
        Icon(icon, null, tint = palette.ink, modifier = Modifier.size(19.dp))
        Text(
            title,
            style = AppType.label.copy(fontSize = 13.5f.sp, color = palette.ink),
            modifier = Modifier.weight(1f),
        )
        Icon(AppIcons.ChevronRight, null, tint = palette.chevron, modifier = Modifier.size(16.dp))
    }
}
