package dev.core.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/**
 * Ekran tepasidagi oddiy sarlavha qatori: (orqaga) + sarlavha (+ izoh) + o'ng tomondagi
 * harakat tugmalari.
 *
 * Bu naqsh chegirmalar bo'limida beshta ekranда so'zma-so'z takrorlangan edi —
 * har birida `Row` + `IconSquareButton` + ikkita `TextStyle(fontFamily = ...)` qo'lda
 * yozilardi. Endi uslub va oraliqlar bitta joydan keladi.
 *
 * Yuqori/yon padding ATAYLAB berilmagan — u ekranга bog'liq, [modifier] orqali qo'yiladi.
 */
@Composable
fun ScreenTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
    titleFontSize: TextUnit = 18.sp,
    palette: AppPalette = appPalette,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        if (onBack != null) {
            BackButton(
                onClick = onBack,
                contentDescription = backContentDescription,
                iconSize = 18.dp,
                palette = palette,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = AppType.screenTitle.copy(fontSize = titleFontSize, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(subtitle, style = AppType.hint.copy(color = palette.inkFaint))
            }
        }
        if (trailing != null) trailing()
    }
}
