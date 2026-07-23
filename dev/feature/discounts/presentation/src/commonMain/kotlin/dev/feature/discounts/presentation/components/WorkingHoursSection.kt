package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_working_hours_closed
import dev.core.uikit.resources.discounts_working_hours_day_off
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.BranchWorkingHours
import dev.feature.discounts.domain.model.WeekDay
import org.jetbrains.compose.resources.stringResource

/**
 * Filial ish vaqti — yetti kunning har biri alohida qator.
 *
 * Nega yetti kun ham ko'rinadi: backend `BranchRequestDto.workingHours` da **hamma kunni**
 * kutadi (`DISCOUNTS_BUSINESS_API.md` §3.2), shu sabab bu yerда kun qo'shish/o'chirish yo'q —
 * faqat "ochiq/yopiq" va vaqtlar tahrirlanadi.
 *
 * Vaqt maydoniga faqat raqam kiritiladi, ikki nuqta o'zi qo'yiladi (`formatTime`).
 * `close` `open` dan kichik bo'lsa xato emas — backend uni "tungacha ishlaydi" deb oladi.
 */
@Composable
fun WorkingHoursSection(
    hours: List<BranchWorkingHours>,
    onClosedChange: (WeekDay, Boolean) -> Unit,
    onOpenChange: (WeekDay, String) -> Unit,
    onCloseChange: (WeekDay, String) -> Unit,
    palette: AppPalette,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        hours.forEach { day ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    day.day.label,
                    style = AppType.fieldLabel.copy(color = palette.ink),
                    modifier = Modifier.width(88.dp),
                )

                if (day.isClosed) {
                    // Yopiq kunда vaqt maydonlari kerak emas — ular `null` bo'lib yuboriladi.
                    Text(
                        stringResource(Res.string.discounts_working_hours_day_off),
                        style = AppType.hint.copy(color = palette.inkFaint),
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    TimeField(day.open.orEmpty(), { onOpenChange(day.day, it) }, Modifier.weight(1f), palette)
                    Text("—", style = AppType.body.copy(color = palette.inkFaint))
                    TimeField(day.close.orEmpty(), { onCloseChange(day.day, it) }, Modifier.weight(1f), palette)
                }

                ClosedToggle(
                    closed = day.isClosed,
                    onClick = { onClosedChange(day.day, !day.isClosed) },
                    palette = palette,
                )
            }
        }
    }
}

/** "HH:MM" maydoni — raqamli klaviatura, matn markazda (qator tor bo'lgani uchun). */
@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    palette: AppPalette,
) {
    GlassTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = BranchWorkingHours.DEFAULT_OPEN,
        modifier = modifier,
        height = 42.dp,
        type = AppFieldType.Number,
        palette = palette,
    )
}

/** "Yopiq" belgisi — bosilganда kun yopiladi/ochiladi (holat rang bilan ko'rinadi). */
@Composable
private fun ClosedToggle(closed: Boolean, onClick: () -> Unit, palette: AppPalette) {
    Box(
        Modifier
            .height(42.dp)
            .width(64.dp)
            .clip(AppRadius.sm)
            .background(if (closed) palette.primary else palette.accentBg)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(Res.string.discounts_working_hours_closed),
            style = AppType.caption.copy(color = if (closed) palette.onPrimary else palette.primary),
            textAlign = TextAlign.Center,
        )
    }
}
