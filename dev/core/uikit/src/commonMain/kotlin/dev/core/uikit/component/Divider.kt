package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_or
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.stringResource

/** O'rtasida matn turgan ajratgich chiziq — "yoki". */
@Composable
fun OrDivider(
    modifier: Modifier = Modifier,
    text: String = stringResource(Res.string.common_or),
    palette: AppPalette = appPalette,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(palette.border))
        Text(text, style = AppType.hint.copy(color = palette.inkFaint))
        Box(Modifier.weight(1f).height(1.dp).background(palette.border))
    }
}
