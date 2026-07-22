package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.ctaShadow
import dev.core.uikit.theme.rowShadow

/** Asosiy harakat tugmasi — ko'k gradient va brend rangli yorug' soya. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    leadingIcon: ImageVector? = null,
    shape: RoundedCornerShape = AppRadius.button,
    palette: AppPalette = appPalette,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(AppSize.buttonHeight)
            .then(if (enabled) Modifier.ctaShadow(shape) else Modifier)
            .clip(shape)
            .background(
                if (enabled) palette.primaryBrush
                else SolidColor(palette.primary.copy(alpha = 0.35f)),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = palette.onPrimary, modifier = Modifier.size(18.dp))
            }
            Text(text, style = AppType.button.copy(color = palette.onPrimary))
            if (trailingIcon != null) {
                Icon(trailingIcon, null, tint = palette.onPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Ikkilamchi tugma — oq karta yuzasi, chegarasiz. */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    palette: AppPalette = appPalette,
) {
    val shape = AppRadius.lg
    val alpha = if (enabled) 1f else 0.4f
    Row(
        modifier
            .fillMaxWidth()
            .height(AppSize.buttonSecondaryHeight)
            .rowShadow(shape)
            .clip(shape)
            .background(palette.card)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                null,
                tint = palette.primary.copy(alpha = alpha),
                modifier = Modifier.size(AppSize.iconMd),
            )
            Spacer(Modifier.width(9.dp))
        }
        Text(text, style = AppType.buttonSecondary.copy(color = palette.ink.copy(alpha = alpha)))
    }
}

/**
 * Orqaga tugmasi.
 *
 * Handoff qoidasi: orqaga **doim `<`**, hech qachon `✕`. `✕` faqat modal/dialog yopish uchun.
 */
@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.ArrowLeft,
    contentDescription: String? = null,
    iconSize: Dp = AppSize.iconMd,
    palette: AppPalette = appPalette,
) {
    IconActionButton(
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        contentDescription = contentDescription,
        iconSize = iconSize,
        tint = palette.ink,
        palette = palette,
    )
}

/** Oq ikonka tugmasi — topbar harakatlari (chat, qo'ng'iroq, sozlama). */
@Composable
fun IconActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = AppSize.iconButton,
    iconSize: Dp = AppSize.iconLg,
    tint: Color = appPalette.primary,
    shape: RoundedCornerShape = AppRadius.md,
    palette: AppPalette = appPalette,
) {
    Box(
        modifier
            .size(size)
            .rowShadow(shape)
            .clip(shape)
            .background(palette.card)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Gradientli ikonka tugmasi — urg'uli harakat (masalan profilga o'tish). */
@Composable
fun GradientIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = AppSize.iconButtonLarge,
    iconSize: Dp = AppSize.iconFab,
    shape: RoundedCornerShape = AppRadius.lg,
    palette: AppPalette = appPalette,
) {
    Box(
        modifier
            .size(size)
            .ctaShadow(shape)
            .clip(shape)
            .background(palette.primaryBrush)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = palette.onPrimary, modifier = Modifier.size(iconSize))
    }
}

/** Kengligi kontentga mos CTA — "+ Biznes" kabi suzuvchi tugma. */
@Composable
fun CompactPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = AppIcons.Plus,
    palette: AppPalette = appPalette,
) {
    val shape = AppRadius.button
    Row(
        modifier
            .ctaShadow(shape)
            .clip(shape)
            .background(palette.primaryBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = palette.onPrimary, modifier = Modifier.size(18.dp))
        }
        Text(text, style = AppType.button.copy(color = palette.onPrimary))
    }
}
