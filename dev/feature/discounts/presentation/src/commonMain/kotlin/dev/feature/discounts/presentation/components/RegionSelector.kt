package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_region_select
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.GeoCatalog
import org.jetbrains.compose.resources.stringResource

/**
 * Viloyat tanlash — bosilganda ro'yxat ochiladi.
 *
 * Viloyat xaritadan joy tanlanganda teskari geokodlashdan avtomatik to'ladi, lekin bu har
 * doim ham ishlamaydi: Nominatim viloyat nomini [GeoCatalog] id'siga bog'lay olmasa `null`
 * qaytadi va biznes viloyat bo'yicha filtrga tushmay qolardi. Shuning uchun bu maydon
 * ko'rinadigan va tuzatsa bo'ladigan qilindi.
 */
@Composable
fun RegionSelector(
    regionName: String?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
    palette: AppPalette,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth()
                .clip(AppRadius.lg)
                .background(palette.fieldBg)
                .border(1.dp, palette.border, AppRadius.lg)
                .clickable(onClick = onToggle)
                .padding(horizontal = AppSpacing.md, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppIcons.Building, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(9.dp))
            Text(
                regionName ?: stringResource(Res.string.discounts_region_select),
                style = AppType.bodyStrong.copy(
                    color = if (regionName != null) palette.ink else palette.inkFaint,
                ),
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) AppIcons.ChevronDown else AppIcons.ChevronRight,
                null,
                tint = palette.inkFaint,
                modifier = Modifier.size(18.dp),
            )
        }

        if (expanded) {
            Spacer(Modifier.padding(top = AppSpacing.xs))
            Column(
                Modifier.fillMaxWidth()
                    .clip(AppRadius.lg)
                    .background(palette.glass)
                    .border(1.dp, palette.border, AppRadius.lg)
                    // Viloyatlar 14 ta — ro'yxat ekranni to'liq egallamasin.
                    .heightIn(max = 260.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                GeoCatalog.regions().forEach { region ->
                    val selected = region.name == regionName
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onSelect(region.id) }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            region.name,
                            style = AppType.label.copy(
                                color = if (selected) palette.primary else palette.ink,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        if (selected) {
                            Icon(
                                AppIcons.Check,
                                null,
                                tint = palette.primary,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
