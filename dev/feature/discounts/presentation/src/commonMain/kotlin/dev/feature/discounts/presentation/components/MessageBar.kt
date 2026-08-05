package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_close
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.stringResource

/**
 * Bir martalik xabar (rasm xatosi, "Qoralama saqlandi", publish xatosi) — bosilganda yopiladi.
 *
 * Ilgari `BranchesSection.kt` ichida turardi; filial tanlash formadan olib tashlangач shu
 * faylga ko'chirildi (u bo'limga umuman bog'liq emas edi).
 */
@Composable
fun MessageBar(message: String, palette: AppPalette, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(AppRadius.md)
            .background(palette.primary.copy(alpha = 0.10f))
            .clickable(onClick = onDismiss)
            .padding(horizontal = AppSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            message,
            style = AppType.fieldLabel.copy(color = palette.primary),
            modifier = Modifier.weight(1f),
        )
        Icon(
            AppIcons.Close,
            stringResource(Res.string.common_close),
            tint = palette.primary,
            modifier = Modifier.size(14.dp),
        )
    }
}
