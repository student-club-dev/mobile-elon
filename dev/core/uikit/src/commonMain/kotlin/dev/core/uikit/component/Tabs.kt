package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_tab_email
import dev.core.uikit.resources.auth_tab_phone
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.stringResource

/** Kirish usuli — telefon yoki email. */
enum class AuthTab { PHONE, EMAIL }

/** Ikki holatli segment tanlagich (Telefon | Email). */
@Composable
fun SegmentedTabs(
    selected: AuthTab,
    onSelect: (AuthTab) -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(AppRadius.lg)
            .background(palette.accentBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabPill(
            icon = AppIcons.Phone,
            label = stringResource(Res.string.auth_tab_phone),
            active = selected == AuthTab.PHONE,
            onClick = { onSelect(AuthTab.PHONE) },
            modifier = Modifier.weight(1f),
            palette = palette,
        )
        TabPill(
            icon = AppIcons.Mail,
            label = stringResource(Res.string.auth_tab_email),
            active = selected == AuthTab.EMAIL,
            onClick = { onSelect(AuthTab.EMAIL) },
            modifier = Modifier.weight(1f),
            palette = palette,
        )
    }
}

@Composable
private fun TabPill(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    palette: AppPalette,
) {
    val shape = AppRadius.sm
    // Qorong'ida tanlangan tab gradient bilan, yorug'da oq karta + soya bilan ajratiladi.
    val activeBg = when {
        active && palette.dark -> Modifier.background(palette.primaryBrush, shape)
        active -> Modifier
            .shadow(6.dp, shape, spotColor = palette.primary.copy(alpha = 0.4f))
            .background(palette.card, shape)
        else -> Modifier
    }
    val contentColor = when {
        active && palette.dark -> Color.White
        active -> palette.primary
        else -> palette.inkFaint
    }
    Row(
        modifier
            .height(AppSize.tabHeight)
            .clip(shape)
            .then(activeBg)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = contentColor, modifier = Modifier.size(AppSize.iconSm))
        Spacer(Modifier.width(6.dp))
        Text(label, style = AppType.label.copy(color = contentColor))
    }
}
