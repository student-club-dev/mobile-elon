package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette

/**
 * "Shisha" karta — yarim shaffof fon + nozik chegara.
 *
 * Bu uchlik (`clip` + `background(palette.glass)` + `border(1.dp, palette.border)`)
 * ilovada 40 dan ortiq joyda so'zma-so'z takrorlangan edi.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = AppRadius.lg,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    onClick: (() -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    palette: AppPalette = appPalette,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.glass)
            .border(1.dp, palette.border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

/** Yonma-yon joylashgan kontentli shisha karta — ro'yxat qatorlari uchun. */
@Composable
fun GlassRow(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = AppRadius.lg,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(AppSpacing.md),
    onClick: (() -> Unit)? = null,
    palette: AppPalette = appPalette,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.glass)
            .border(1.dp, palette.border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}

/**
 * Kvadrat ikonka konteyneri — rangli yarim shaffof fon ustida ikonka.
 * Menyu qatorlari, bildirishnoma turlari va harakat tugmalarida ishlatiladi.
 */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = appPalette.primary,
    background: Color = tint.copy(alpha = 0.10f),
    size: Dp = 36.dp,
    iconSize: Dp = 17.dp,
    shape: RoundedCornerShape = AppRadius.sm,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}
