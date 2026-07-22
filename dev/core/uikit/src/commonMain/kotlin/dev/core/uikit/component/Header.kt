package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.appPalette

/**
 * Ekran tepasidagi gradientli sarlavha bloki — pastki burchaklari yumaloq.
 *
 * Bu naqsh ProfileScreen, StudentsScreen, BusinessAccountScreen va BusinessWelcomeScreen
 * da bir xil `listOf(Color(0xFF6C47FF), Color(0xFF7C4DFF), Color(0xFF5B34D6))` bilan
 * TAKRORLANGAN edi — brend rangi o'zgarsa to'rt joyni qidirish kerak bo'lardi.
 * Gradient endi [AppPalette.headerBrush] dan keladi va qorong'i rejimda quyuqroq bo'ladi.
 */
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    palette: AppPalette = appPalette,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(
                palette.headerBrush,
                RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius),
            ),
        content = content,
    )
}

/**
 * [GradientHeader] ICHIDAGI kvadrat harakat tugmasi — orqaga, tahrirlash va h.k.
 *
 * Ranglar ataylab palitradan OLINMAYDI: tugma binafsha gradient USTIDA turadi, shuning uchun
 * foni oq shaffof, ikonkasi oq bo'lishi kerak — bu yorug' va qorong'i rejimda bir xil.
 * Palitra rangi ishlatilsa qorong'ida ikonka gradientga singib ketardi.
 */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = AppSize.iconButton,
    iconSize: Dp = AppSize.iconMd,
) {
    Box(
        modifier
            .size(size)
            .clip(AppRadius.md)
            .background(Color.White.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}
