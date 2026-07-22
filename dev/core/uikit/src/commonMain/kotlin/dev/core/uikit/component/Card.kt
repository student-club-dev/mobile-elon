package dev.core.uikit.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.cardShadow
import dev.core.uikit.theme.rowShadow

/**
 * Asosiy karta — **oq yuza och ko'k-kulrang fon ustida**, yumshoq soya bilan.
 *
 * Bu yangi dizayn tilining asosiy qurilish bloki (`design_handoff_studentclub_elonuz`).
 * Chegara YO'Q: kartani fondan faqat rang va soya ajratadi.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = AppRadius.card,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    onClick: (() -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    palette: AppPalette = appPalette,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .cardShadow(shape)
            .clip(shape)
            .background(palette.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

/** Yonma-yon kontentli karta — ro'yxat qatorlari uchun (yengilroq soya). */
@Composable
fun GlassRow(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = AppRadius.row,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.lg, vertical = 15.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(13.dp),
    onClick: (() -> Unit)? = null,
    palette: AppPalette = appPalette,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .rowShadow(shape)
            .clip(shape)
            .background(palette.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}

/**
 * Kvadrat ikonka nishoni — ochiq ko'k fon ustida brend rangli ikonka.
 *
 * Handoff'da bu naqsh har bir sozlama qatorida va karta boshida takrorlanadi.
 */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = appPalette.primary,
    background: Color = appPalette.accentBg,
    size: Dp = AppSize.iconTile,
    iconSize: Dp = AppSize.iconMd,
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

/**
 * Gradientli nishon — biznes kategoriyasi kabi urg'uli elementlar uchun
 * (handoff'da kafe kartasi pushti-sariq gradient bilan).
 */
@Composable
fun GradientTile(
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.White,
    size: Dp = AppSize.categoryTile,
    iconSize: Dp = AppSize.iconFab,
    shape: RoundedCornerShape = AppRadius.lg,
) {
    Box(
        modifier.size(size).clip(shape).background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}
