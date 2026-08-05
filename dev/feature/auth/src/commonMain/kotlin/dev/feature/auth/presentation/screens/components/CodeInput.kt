package dev.feature.auth.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.keyboardAware
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType

/**
 * 6 xonali tasdiqlash kodi maydoni — ko'rinishda alohida kataklar, aslida bitta
 * `BasicTextField` (matn ko'rinmas, kataklar `decorationBox` ichida chiziladi).
 *
 * SMS kodi (OTP) va email kodi ekranlarida bir xil naqsh edi — endi bitta manba.
 */
@Composable
fun CodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    palette: AppPalette,
    modifier: Modifier = Modifier,
    length: Int = 6,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        // Kataklar ham klaviatura ostida qolmasin (ekran kichik bo'lsa yoki
        // tepasida uzun matn bo'lsa) — fokusda o'zini ko'rinadigan joyga suradi.
        modifier = modifier.keyboardAware().fillMaxWidth(),
        decorationBox = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                repeat(length) { i ->
                    CodeCell(value.getOrNull(i), i == value.length, palette, Modifier.weight(1f))
                }
            }
        },
    )
}

/** Bitta katak — bo'sh, fokusda yoki to'ldirilgan holatlar uchun alohida fon/chegara. */
@Composable
private fun CodeCell(ch: Char?, focused: Boolean, palette: AppPalette, modifier: Modifier) {
    val shape = AppRadius.sm
    // Bo'sh katak foni — palitradagi ochiq ko'k aksent yuzasi.
    val background = if (ch != null || focused) palette.fieldBg else palette.accentBg
    val border = when {
        focused -> palette.primary
        ch != null -> palette.primary.copy(alpha = 0.20f)
        else -> palette.divider
    }
    Box(
        modifier
            .height(AppSize.fieldHeight)
            .clip(shape)
            .background(background)
            .border(if (focused || ch != null) 1.5.dp else 1.dp, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            ch != null -> Text(ch.toString(), style = AppType.topBarTitle.copy(color = palette.ink))
            focused -> Box(Modifier.width(2.dp).height(24.dp).background(palette.primary))
        }
    }
}

/**
 * "Kodni qayta yuborish · 0:59" qatori — taymer tugagach bosiladigan havolaga aylanadi.
 *
 * [timerPrefix] va [resendLabel] resursdan keladi, chunki OTP va email ekranlarida
 * matn biroz farq qiladi.
 */
@Composable
fun ResendRow(
    seconds: Int,
    timerPrefix: String,
    resendLabel: String,
    onResend: () -> Unit,
    palette: AppPalette,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Clock, null, tint = palette.inkFaint, modifier = Modifier.size(AppSize.iconSm))
        Spacer(Modifier.width(6.dp))
        if (seconds > 0) {
            Text(timerPrefix, style = AppType.link.copy(color = palette.inkFaint))
            Text(formatTimer(seconds), style = AppType.linkAction.copy(fontWeight = AppType.label.fontWeight, color = palette.primary))
        } else {
            Text(
                resendLabel,
                style = AppType.linkAction.copy(fontWeight = AppType.label.fontWeight, color = palette.primary),
                modifier = Modifier.clickableNoRipple(onResend),
            )
        }
    }
}
