package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/** Brend logotipi — gradient kvadrat ichida bitiruv shapkasi ikonkasi. */
@Composable
fun LogoTile(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    radius: Dp = 16.dp,
    iconSize: Dp = 28.dp,
    palette: AppPalette = appPalette,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .size(size)
            .shadow(18.dp, shape, spotColor = palette.primary, ambientColor = palette.primary)
            .background(palette.primaryBrush, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(AppIcons.GraduationCap, null, tint = palette.onPrimary, modifier = Modifier.size(iconSize))
    }
}

/** Ekran ostidagi havola — "Hisobingiz yo'qmi? Ro'yxatdan o'tish". */
@Composable
fun FooterLink(
    prefix: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    Row(
        modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("$prefix ", style = AppType.link.copy(color = palette.inkMuted))
        Text(action, style = AppType.linkAction.copy(color = palette.primary))
    }
}
