package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/**
 * Monogramma plitkasi — rasm o'rniga bosh harf(lar) ko'rsatiladigan avatar.
 *
 * Talaba avatari, kompaniya logotipi va suhbatdosh rasmi shu bitta komponentdan chiziladi;
 * ilgari har bir ekran `Box(...).background(primary.copy(alpha = 0.14f))` ni qo'lda yozardi.
 * [shape] orqali dumaloq (avatar) yoki yumaloq kvadrat (kompaniya) ko'rinishi tanlanadi.
 */
@Composable
fun MonogramTile(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    shape: RoundedCornerShape = AppRadius.pill,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight = AppType.button.fontWeight ?: FontWeight.ExtraBold,
    accent: Color = appPalette.primary,
    backgroundAlpha: Float = 0.14f,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(accent.copy(alpha = backgroundAlpha))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppType.button.copy(fontSize = fontSize, fontWeight = fontWeight, color = accent))
    }
}
