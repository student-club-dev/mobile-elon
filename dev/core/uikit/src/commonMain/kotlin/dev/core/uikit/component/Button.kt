package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

/** Asosiy harakat tugmasi — gradient fon va yumshoq soya. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    palette: AppPalette = appPalette,
) {
    val shape = AppRadius.button
    Box(
        modifier
            .fillMaxWidth()
            .height(AppSize.buttonHeight)
            .shadow(if (enabled) 20.dp else 0.dp, shape, spotColor = palette.primary, ambientColor = palette.primary)
            .clip(shape)
            .background(if (enabled) palette.primaryBrush else SolidColor(palette.primary.copy(alpha = 0.4f)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(text, style = AppType.button.copy(color = palette.onPrimary))
            if (trailingIcon != null) {
                Icon(trailingIcon, null, tint = palette.onPrimary, modifier = Modifier.size(17.dp))
            }
        }
    }
}

/** Ikkilamchi tugma — shisha fon va nozik chegara. */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    palette: AppPalette = appPalette,
) {
    val shape = AppRadius.button
    val contentAlpha = if (enabled) 1f else 0.4f
    Row(
        modifier
            .fillMaxWidth()
            .height(AppSize.buttonSecondaryHeight)
            .clip(shape)
            .background(palette.glass)
            .border(1.dp, palette.border, shape)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                null,
                tint = palette.primary.copy(alpha = contentAlpha),
                modifier = Modifier.size(AppSize.iconMd),
            )
            Spacer(Modifier.width(9.dp))
        }
        Text(text, style = AppType.buttonSecondary.copy(color = palette.onGlass.copy(alpha = contentAlpha)))
    }
}

/** Kvadrat ikonka tugmasi — odatda orqaga qaytish uchun. */
@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.ArrowLeft,
    contentDescription: String? = null,
    /** Ikonka o'lchami — ba'zi ekranlarda 18.dp, auth oqimida 19.dp ishlatiladi. */
    iconSize: Dp = 19.dp,
    palette: AppPalette = appPalette,
) {
    Box(
        modifier
            .size(AppSize.iconButton)
            .clip(AppRadius.md)
            .background(palette.glass)
            .border(1.dp, palette.border, AppRadius.md)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = palette.ink, modifier = Modifier.size(iconSize))
    }
}
