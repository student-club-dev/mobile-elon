package dev.core.uikit.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/**
 * Standart matn uslublari — [AppType] tokenlarini joriy palitra rangi bilan bog'laydi.
 * Ekranlar `TextStyle(...)` ni qo'lda yig'masligi uchun shu yordamchilar ishlatiladi.
 */

@Composable
fun ScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = AppType.screenTitle.fontSize,
    palette: AppPalette = appPalette,
) {
    Text(text, modifier, style = AppType.screenTitle.copy(fontSize = fontSize, color = palette.ink))
}

@Composable
fun ScreenSubtitle(text: String, modifier: Modifier = Modifier, palette: AppPalette = appPalette) {
    Text(text, modifier, style = AppType.subtitle.copy(color = palette.inkMuted))
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier, palette: AppPalette = appPalette) {
    Text(text, modifier, style = AppType.fieldLabel.copy(color = palette.ink))
}

@Composable
fun HintText(text: String, modifier: Modifier = Modifier, palette: AppPalette = appPalette) {
    Text(text, modifier, style = AppType.hint.copy(color = palette.inkFaint))
}

/**
 * Xato xabari — bo'sh bo'lsa umuman chizilmaydi.
 *
 * Rang [AppPalette.danger] dan olinadi, shuning uchun qorong'i rejimda ochroq qizil
 * ko'rinadi (qora fonda `0xFFDC2626` ni o'qib bo'lmaydi).
 */
@Composable
fun ColumnScope.ErrorText(message: String?, palette: AppPalette = appPalette) {
    if (message.isNullOrBlank()) return
    Spacer(Modifier.height(AppSpacing.md))
    Text(message, style = AppType.error.copy(color = palette.danger))
}

/** Bir qatorli xato matni — ustun (`ColumnScope`) ichida bo'lmagan joylar uchun. */
@Composable
fun InlineErrorText(message: String, modifier: Modifier = Modifier, palette: AppPalette = appPalette) {
    Text(message, modifier, style = AppType.error.copy(color = palette.danger))
}
