package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/**
 * Tanlanadigan filtr chipi — ro'yxat tepasidagi "Barchasi / IT / Masofaviy" kabi.
 *
 * Faol holatda brend rangi bilan to'ldiriladi, aks holda shisha fon + nozik chegara.
 * Ilgari bu naqsh JobsScreen va StudentsScreen'da so'zma-so'z takrorlangan edi.
 */
@Composable
fun FilterPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    val shape = AppRadius.pill
    Box(
        modifier
            .clip(shape)
            .background(if (active) palette.primary else palette.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = AppType.label.copy(
                fontSize = 12.5f.sp,
                color = if (active) palette.onPrimary else palette.inkMuted,
            ),
        )
    }
}

/**
 * Yumshoq aksentli yorliq — teg, qiziqish, promokod, "Yana" kabi mayda chiplar.
 *
 * Fon aksent rangining shaffof variantidan olinadi, shuning uchun qorong'i rejimda ham
 * o'z-o'zidan to'g'ri ko'rinadi (ilgari har joyda `primary.copy(alpha = ...)` qo'lda yozilardi).
 */
@Composable
fun SoftPill(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = appPalette.primary,
    backgroundAlpha: Float = 0.08f,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    textStyle: TextStyle = AppType.caption.copy(fontSize = 10.5f.sp, fontWeight = AppType.label.fontWeight),
    contentPadding: PaddingValues = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier
            .clip(shape)
            .background(accent.copy(alpha = backgroundAlpha))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = textStyle.copy(color = accent))
    }
}
