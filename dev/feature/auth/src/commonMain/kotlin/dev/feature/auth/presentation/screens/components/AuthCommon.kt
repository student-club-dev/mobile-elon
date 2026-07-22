package dev.feature.auth.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.rowShadow
import dev.core.uikit.util.UZ_DIALING_CODE

/** Telefon maydonining "🇺🇿 +998 |" prefiksi. */
@Composable
fun PhonePrefix(palette: AppPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Bayroq emojisi olib tashlandi: Compose iOS uni chiza olmaydi va ikkita "?" bo'lib chiqadi.
        Text(UZ_DIALING_CODE, style = AppType.bodyStrong.copy(fontWeight = AppType.label.fontWeight, color = palette.ink))
        Spacer(Modifier.width(9.dp))
        Box(Modifier.width(1.dp).height(22.dp).background(palette.divider))
    }
}

/**
 * Kichik belgilash katakchasi — "Meni eslab qol" va shartlarga rozilik uchun.
 *
 * Belgilanganda brend rangi bilan to'ldiriladi; ichidagi belgi to'ldirilgan fon USTIDA
 * turadi, shuning uchun [AppPalette.onPrimary] (har ikkala rejimda oq) ishlatiladi.
 */
@Composable
fun CheckBoxSmall(checked: Boolean, palette: AppPalette, size: Dp = 20.dp) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        Modifier.size(size).clip(shape)
            .background(if (checked) palette.primary else Color.Transparent)
            .border(if (checked) 0.dp else 1.5.dp, palette.divider, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(AppIcons.Check, null, tint = palette.onPrimary, modifier = Modifier.size(size * 0.6f))
        }
    }
}

/** Mayda interaktiv elementlar uchun bosish yordamchisi. */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

/** Sekundlarni "0:59" ko'rinishida formatlaydi (qayta yuborish taymeri). */
internal fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "0$m:${s.toString().padStart(2, '0')}"
}

/**
 * Sarlavha ustidagi yumshoq aksentli katta ikonka — OTP, email tasdiqlash va parol
 * tiklash ekranlarida bir xil naqsh.
 */
@Composable
fun AccentIconTile(
    icon: ImageVector,
    palette: AppPalette,
    size: Dp = 60.dp,
    shape: RoundedCornerShape = AppRadius.lg,
    iconSize: Dp = 30.dp,
) {
    Box(
        // Yangi dizaynda nishon foni — ochiq ko'k `accentBg`, shaffof qatlam emas.
        Modifier.size(size).background(palette.accentBg, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(iconSize))
    }
}

/** Onboarding sahifa indikatori nuqtasi — faol nuqta cho'ziladi. */
@Composable
internal fun Dot(active: Boolean, palette: AppPalette) {
    Box(
        Modifier.height(7.dp).width(if (active) 22.dp else 7.dp).background(
            if (active) palette.primary else palette.primary.copy(alpha = 0.25f),
            AppRadius.pill,
        ),
    )
}

/** Onboarding illyustratsiyasi ustida suzuvchi ikonka chipi (ilgari emoji edi). */
@Composable
internal fun FloatingChip(icon: ImageVector, label: String?, modifier: Modifier, palette: AppPalette) {
    val shape = AppRadius.sm
    Row(
        // Chegara o'rniga soya — chip fon ustida "suzib" turadi.
        modifier.rowShadow(shape)
            .clip(shape)
            .background(palette.card)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(16.dp))
        if (label != null) {
            Text(label, style = AppType.caption.copy(fontWeight = AppType.label.fontWeight, color = palette.ink))
        }
    }
}
