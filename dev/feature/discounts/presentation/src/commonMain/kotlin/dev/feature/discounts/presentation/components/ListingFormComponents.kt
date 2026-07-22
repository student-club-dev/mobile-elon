package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.rememberKeyboardDismiss
import dev.core.uikit.component.SoftPill
import dev.core.uikit.resources.Res
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.rowShadow
import org.jetbrains.compose.resources.stringResource

/** Bo'lim yon (horizontal) padding'i — flat dizaynда sarlavha/maydonlar shu qadar ichkariда. */
val SectionHPad = AppSpacing.lg

/**
 * Forma bo'limi — FLAT (kartasiz): faqat sarlavha + izoh + tarkib. Kartachalar yo'q, hamma narsa
 * fonда to'g'ridan-to'g'ri. Yon padding shu yerda beriladi (horizontal scroll bo'limlari esa
 * o'zi edge-to-edge chiqadi).
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    error: String? = null,
    palette: AppPalette = appPalette,
    content: @Composable () -> Unit,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = SectionHPad),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader(title, subtitle, palette)
        content()
        if (error != null) {
            // Xato rangi palitradan: qorong'i rejimda ochroq qizil bo'lib o'qiladi.
            Text(error, style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.danger))
        }
    }
}

/** Bo'lim sarlavhasi (flat). Horizontal-scroll bo'limlarда alohida ishlatiladi. */
@Composable
fun SectionHeader(title: String, subtitle: String?, palette: AppPalette = appPalette) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            style = AppType.cardTitle.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.ink),
        )
        if (subtitle != null) {
            Text(subtitle, style = AppType.hint.copy(color = palette.inkFaint))
        }
    }
}

/**
 * Tanlanadigan chip — 36dp balandlik, yumshoq squircle shakl, tanlanганда SOLID brand fon
 * (gradient emas), tanlanmaganда nozik chegara.
 */
@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    palette: AppPalette = appPalette,
) {
    // Chip bosilganda klaviatura yopiladi — u ochiq bo'lsa variantlar ostida qolib ketadi.
    val dismissKeyboard = rememberKeyboardDismiss()
    val shape = AppRadius.sm
    // Tanlanmagan chip — ochiq ko'k aksent yuzasi (chegara emas).
    Row(
        Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) palette.primary else palette.accentBg)
            .clickable { dismissKeyboard(); onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (selected) {
            Icon(AppIcons.Check, null, tint = palette.onPrimary, modifier = Modifier.size(14.dp))
        }
        Text(
            text,
            style = AppType.label.copy(
                fontWeight = if (selected) AppType.label.fontWeight else FontWeight.SemiBold,
                color = if (selected) palette.onPrimary else palette.inkMuted,
            ),
        )
    }
}

/** Chip'lar qatori — sig'masa keyingi qatorga o'tadi. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipFlow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) { content() }
}

/** Ochiluvchi ro'yxat (viloyat, tuman, SELECT turidagi maydonlar). */
@Composable
fun FormDropdown(
    value: String?,
    placeholder: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    palette: AppPalette = appPalette,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.firstOrNull { it.first == value }?.second
    val shape = AppRadius.lg

    Box(modifier.fillMaxWidth()) {
        Row(
            // Maydon — oq yuza va yumshoq soya; chegara yangi dizaynda yo'q.
            Modifier.fillMaxWidth()
                .height(AppSize.fieldHeight)
                .rowShadow(shape)
                .clip(shape)
                .background(palette.fieldBg)
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label ?: placeholder,
                style = AppType.label.copy(
                    fontWeight = if (label != null) AppType.label.fontWeight else FontWeight.Normal,
                    color = when {
                        !enabled -> palette.inkFaint
                        label != null -> palette.ink
                        else -> palette.inkFaint
                    },
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(AppIcons.ChevronDown, null, tint = palette.inkMuted, modifier = Modifier.size(AppSize.iconSm))
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text,
                            style = AppType.label.copy(
                                fontWeight = if (key == value) AppType.buttonSecondary.fontWeight else FontWeight.Normal,
                                color = if (key == value) palette.primary else palette.ink,
                            ),
                        )
                    },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Ha/yo'q almashtirgich (BOOLEAN turidagi maydonlar). */
@Composable
fun FormSwitch(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    palette: AppPalette = appPalette,
) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = AppType.link.copy(fontWeight = AppType.label.fontWeight, color = palette.inkMuted),
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier.size(width = AppSize.toggleWidth, height = AppSize.toggleHeight)
                .clip(AppRadius.pill)
                .background(if (checked) palette.primary else palette.accentBg),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            // Rangli yo'lak USTIDAGI tugmacha — kontent rangi (`onPrimary`) ishlatiladi.
            Box(Modifier.padding(horizontal = 3.dp).size(20.dp).clip(AppRadius.pill).background(palette.onPrimary))
        }
    }
}

/** Statusni ko'rsatuvchi rangli yorliq ("Faol", "Qoralama"). */
@Composable
fun StatusPill(text: String, color: Color) {
    SoftPill(
        text,
        accent = color,
        backgroundAlpha = 0.14f,
        shape = AppRadius.sm,
        textStyle = AppType.caption.copy(
            fontWeight = AppType.buttonSecondary.fontWeight,
        ),
        contentPadding = PaddingValues(horizontal = 9.dp, vertical = AppSpacing.xs),
    )
}
