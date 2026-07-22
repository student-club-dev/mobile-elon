package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/** Banner ohangi — fon va matn rangini belgilaydi. */
enum class BannerTone { WARNING, DANGER, SUCCESS, INFO }

/**
 * Qisqa holat xabari — "E'loningiz moderatsiyada", "Muddati tugadi" kabi.
 *
 * Ilgari har bir ekran buni `Color(0xFFFEF3C7)` / `Color(0xFFB45309)` bilan qo'lda
 * chizardi va qorong'i rejimda sariq fon oq matn bilan qo'shilib o'qib bo'lmas edi.
 * Endi ranglar palitradan olinadi va ikkala rejimda ham to'g'ri ishlaydi.
 */
@Composable
fun StatusBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.WARNING,
    icon: ImageVector? = AppIcons.Bell,
    palette: AppPalette = appPalette,
) {
    val (background: Color, foreground: Color) = when (tone) {
        BannerTone.WARNING -> palette.warningBg to palette.warning
        BannerTone.DANGER -> palette.dangerBg to palette.danger
        BannerTone.SUCCESS -> palette.successBg to palette.successDeep
        BannerTone.INFO -> palette.tabTrack to palette.primary
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(AppRadius.md)
            .background(background)
            .padding(horizontal = AppSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        if (icon != null) {
            Icon(icon, null, tint = foreground, modifier = Modifier.size(AppSize.iconSm))
        }
        Text(text, style = AppType.fieldLabel.copy(color = foreground))
    }
}
