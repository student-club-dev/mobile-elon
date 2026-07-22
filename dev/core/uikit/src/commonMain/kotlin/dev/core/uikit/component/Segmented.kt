package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.rowShadow

/**
 * Segment tanlagich — oq konteyner ichida teng bo'laklar, faoli ochiq ko'k fon bilan
 * (`design_handoff_studentclub_elonuz`: Mavzu va Til tanlash).
 *
 * @param options ko'rsatiladigan yorliqlar
 * @param selectedIndex faol bo'lakning indeksi
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    Row(
        modifier
            .fillMaxWidth()
            .rowShadow(AppRadius.lg)
            .clip(AppRadius.lg)
            .background(palette.card)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (active) palette.accentBg else palette.card)
                    .clickable { onSelect(index) }
                    .padding(vertical = 11.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = AppType.segment.copy(
                        fontSize = if (options.size > 2) 13.sp else AppType.segment.fontSize,
                        color = if (active) palette.primary else palette.inkMuted,
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Toggle tugmachasi — yoqilganda ko'k gradient, o'chirilganda kulrang yo'lak.
 * Handoff'dagi bildirishnoma sozlamalari shu ko'rinishda.
 */
@Composable
fun AppToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    Box(
        modifier
            .size(width = AppSize.toggleWidth, height = AppSize.toggleHeight)
            .clip(AppRadius.pill)
            .then(
                if (checked) Modifier.background(palette.primaryBrush)
                else Modifier.background(palette.divider),
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .padding(horizontal = 3.dp)
                .size(20.dp)
                .clip(AppRadius.pill)
                .background(palette.card),
        )
    }
}

/** Bo'lim ustidagi kichik kulrang yorliq — "Hisob", "Mavzu", "Til". */
@Composable
fun GroupLabel(text: String, modifier: Modifier = Modifier, palette: AppPalette = appPalette) {
    Text(
        text,
        modifier = modifier.padding(start = 2.dp, bottom = AppSpacing.sm),
        style = AppType.groupLabel.copy(color = palette.inkMuted),
    )
}

/**
 * Sozlama qatori — chapda ikonka nishoni, o'rtada sarlavha, o'ngda qiymat va chevron.
 * Handoff'da bu naqsh sozlamalar va profil ekranlarida takrorlanadi.
 */
@Composable
fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
    palette: AppPalette = appPalette,
    trailing: @Composable (() -> Unit)? = null,
) {
    GlassRow(modifier = modifier, onClick = onClick, palette = palette) {
        IconTile(icon, tint = palette.primary, background = palette.accentBg)
        Text(
            title,
            style = AppType.rowTitle.copy(color = palette.ink),
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(value, style = AppType.secondary.copy(color = palette.inkMuted))
        }
        trailing?.invoke()
        if (showChevron && trailing == null) {
            Icon(
                AppIcons.ChevronRight,
                null,
                tint = palette.inkMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
