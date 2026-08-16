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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.keyboardAware
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_working_hours_closed
import dev.core.uikit.resources.discounts_working_hours_day_off
import dev.core.uikit.resources.discounts_working_hours_open
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.rowShadow
import dev.feature.discounts.domain.model.BranchWorkingHours
import dev.feature.discounts.domain.model.WeekDay
import dev.feature.discounts.presentation.localizedLabel
import org.jetbrains.compose.resources.stringResource

/**
 * Filial ish vaqti — yetti kunning har biri alohida qator.
 *
 * Nega yetti kun ham ko'rinadi: backend `BranchRequestDto.workingHours` da **hamma kunni**
 * kutadi (`DISCOUNTS_BUSINESS_API.md` §3.2), shu sabab bu yerда kun qo'shish/o'chirish yo'q —
 * faqat "ochiq/yopiq" va vaqtlar tahrirlanadi.
 *
 * Ikkala vaqt **bitta** karta ichida: ilgari ular ikkita alohida maydon edi va har biri o'z
 * 16dp yon paddingi bilan kelardi — natijada yozuvga joy qolmay, soat `07:0` bo'lib kesilardi.
 * Endi padding bir marta, o'rtada `—` bilan; qolgan joy raqamlarga ketadi.
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
                    // `WeekDay.label` EMAS: u domain'da qattiq o'zbekcha yozilgan va rus/ingliz
                    // tilida ham "Dushanba" bo'lib chiqardi.
                    day.day.localizedLabel(),
                    style = AppType.fieldLabel.copy(fontSize = 13.sp, color = palette.ink),
                    modifier = Modifier.width(DayLabelWidth),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (day.isClosed) {
                    // Yopiq kunда vaqt maydonlari kerak emas — ular `null` bo'lib yuboriladi.
                    Text(
                        stringResource(Res.string.discounts_working_hours_day_off),
                        style = AppType.hint.copy(color = palette.inkFaint),
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    TimeRangeField(
                        open = day.open.orEmpty(),
                        close = day.close.orEmpty(),
                        onOpenChange = { onOpenChange(day.day, it) },
                        onCloseChange = { onCloseChange(day.day, it) },
                        modifier = Modifier.weight(1f),
                        palette = palette,
                    )
                }

                OpenClosedToggle(
                    closed = day.isClosed,
                    onClick = { onClosedChange(day.day, !day.isClosed) },
                    palette = palette,
                )
            }
        }
    }
}

/**
 * Kun nomi ustuni — eng uzun variantga qarab o'lchandi.
 *
 * O'lchov ruscha bo'yicha: "Понедельник"/"Воскресенье" (11 belgi) o'zbekcha "Chorshanba" va
 * inglizcha "Wednesday" dan uzun. Ustun hamma tilда bir xil kenglikdа qoladi, aks holda
 * jadval qatorlari til almashganda siljib turardi.
 */
private val DayLabelWidth = 86.dp

/** Qator balandligi — vaqt kartasi va holat chipi bir xil balandlikда. */
private val RowHeight = 42.dp

/** "07:00 — 22:00" — ikkala vaqt bitta kartada. */
@Composable
private fun TimeRangeField(
    open: String,
    close: String,
    onOpenChange: (String) -> Unit,
    onCloseChange: (String) -> Unit,
    modifier: Modifier,
    palette: AppPalette,
) {
    val shape = AppRadius.lg
    Row(
        modifier
            .height(RowHeight)
            .rowShadow(shape)
            .clip(shape)
            .background(palette.card)
            .padding(horizontal = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeInput(open, onOpenChange, Modifier.weight(1f), palette)
        Text(
            "—",
            style = AppType.body.copy(color = palette.inkFaint),
            modifier = Modifier.padding(horizontal = AppSpacing.xs),
        )
        TimeInput(close, onCloseChange, Modifier.weight(1f), palette)
    }
}

/**
 * "HH:MM" kiritish — o'z foni yo'q, chunki u [TimeRangeField] kartasining ichida turadi.
 * Matn markazda: qator tor va o'ngga/chapga surilgan raqam juft bo'lib ko'rinmaydi.
 */
@Composable
private fun TimeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    palette: AppPalette,
) {
    Box(modifier.keyboardAware(), contentAlignment = Alignment.Center) {
        if (value.isEmpty()) {
            Text(
                BranchWorkingHours.DEFAULT_OPEN,
                style = AppType.bodyStrong.copy(color = palette.inkFaint),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(AppFieldType.Number.sanitize(it)) },
            singleLine = true,
            textStyle = AppType.bodyStrong.copy(color = palette.ink, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(palette.primary),
            keyboardOptions = AppFieldType.Number.keyboardOptions,
            visualTransformation = AppFieldType.Number.visualTransformation,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Kunning HOLATI — "Ochiq" yoki "Yopiq"; bosilganда almashadi.
 *
 * Ilgari chip har doim "Yopiq" deb yozilardi (u holatni emas, HARAKATNI bildirardi) va
 * ish vaqti to'ldirilgan bo'lsa ham forma "har kuni yopiq" bo'lib ko'rinardi. Endi yozuv
 * kunning joriy holatini aytadi: ochiq kun yashil, yopiq kun kulrang.
 */
@Composable
private fun OpenClosedToggle(closed: Boolean, onClick: () -> Unit, palette: AppPalette) {
    val accent = if (closed) palette.inkMuted else palette.success
    Box(
        Modifier
            .height(RowHeight)
            .width(72.dp)
            .clip(AppRadius.sm)
            .background(accent.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(
                if (closed) {
                    Res.string.discounts_working_hours_closed
                } else {
                    Res.string.discounts_working_hours_open
                },
            ),
            style = AppType.caption.copy(fontWeight = AppType.label.fontWeight, color = accent),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
