package dev.core.uikit.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.appPalette

/** Ijtimoiy tarmoq orqali kirish qatori — Google / Apple / Telegram. */
@Composable
fun SocialRow(
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onTelegram: () -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        SocialButton(Modifier.weight(1f), onGoogle, palette) {
            Image(AppIcons.Google, null, modifier = Modifier.size(AppSize.iconLg))
        }
        SocialButton(Modifier.weight(1f), onApple, palette) {
            Icon(AppIcons.Apple, null, tint = palette.ink, modifier = Modifier.size(19.dp))
        }
        SocialButton(Modifier.weight(1f), onTelegram, palette) {
            Image(AppIcons.Telegram, null, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SocialButton(
    modifier: Modifier,
    onClick: () -> Unit,
    palette: AppPalette,
    content: @Composable () -> Unit,
) {
    val shape = AppRadius.lg
    Box(
        modifier
            .height(AppSize.buttonSecondaryHeight)
            .clip(shape)
            .background(palette.glass)
            .border(1.dp, palette.border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
