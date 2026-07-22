package dev.feature.business.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.business_center_label
import dev.core.uikit.resources.business_my_listings_title
import dev.core.uikit.resources.business_new_listing
import dev.core.uikit.resources.business_profile_action
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.stringResource
import dev.core.uikit.theme.rowShadow
import dev.core.uikit.theme.AppRadius

/**
 * `BusinessShell` ning "bezagi" — yuqori panel va suzuvchi tugma. Navigatsiya grafi
 * o'qilishi oson qolishi uchun karkas faylidan ajratildi.
 */

/** Yuqori panel: back tugmasi + "E'lonlarim" sarlavhasi + gradient profil tugmasi. */
@Composable
internal fun BusinessTopBar(
    onBack: () -> Unit,
    onProfile: () -> Unit,
    palette: AppPalette,
) {
    Column(
        Modifier.fillMaxWidth().padding(start = AppSpacing.lg, end = AppSpacing.lg, top = 52.dp, bottom = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Orqaga qaytish — "Bizneslarim" ro'yxatiga.
            Box(
                Modifier.size(42.dp)
                    .rowShadow(AppRadius.pill)
                    .clip(CircleShape).background(palette.card)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.ArrowLeft,
                    stringResource(Res.string.common_back),
                    tint = palette.ink,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.width(AppSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.business_center_label),
                    style = AppType.caption.copy(
                        fontWeight = AppType.screenTitle.fontWeight,
                        letterSpacing = 1.4.sp,
                        color = palette.primary,
                    ),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(Res.string.business_my_listings_title),
                    style = AppType.screenTitle.copy(fontSize = 23.sp, letterSpacing = 0.sp, color = palette.ink),
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
                // Gradient ustidagi ikonka — palitraning `onPrimary` rangi (ikkala rejimda oq).
                Icon(
                    AppIcons.Store,
                    stringResource(Res.string.business_profile_action),
                    tint = palette.onPrimary,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        // Chegirma/E'lon segment olib tashlandi — hammasi (chegirma + oddiy) bitta ro'yxatda.
    }
}

/** O'ng-past extended FAB — yangi e'lon (chegirma/oddiy forma ichida tanlanadi). */
@Composable
internal fun CreateFab(
    palette: AppPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier
            .shadow(AppSpacing.lg, shape, spotColor = palette.primary.copy(alpha = 0.6f))
            .clip(shape).background(palette.primaryBrush)
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 22.dp, top = 15.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(AppIcons.Plus, null, tint = palette.onPrimary, modifier = Modifier.size(22.dp))
        Text(
            stringResource(Res.string.business_new_listing),
            style = AppType.button.copy(
                fontSize = 14.5f.sp,
                fontWeight = AppType.screenTitle.fontWeight,
                color = palette.onPrimary,
            ),
        )
    }
}
