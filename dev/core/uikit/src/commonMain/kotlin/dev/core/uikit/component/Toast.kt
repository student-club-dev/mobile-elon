package dev.core.uikit.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.cardShadow
import kotlinx.coroutines.delay

/** Toast ekranда qancha turadi. Uzun matnni ham o'qishga yetadi, lekin yo'lni to'smaydi. */
private const val TOAST_MILLIS = 4500L

/**
 * Qalqib chiqadigan qisqa xabar — xato yoki tasdiq.
 *
 * Nega alohida komponent: xato matni ilgari forma **ichida**, tugma yonida kichik qizil
 * yozuv bo'lib turardi. Uzun matn (masalan serverning chegara haqidagi xabari) u yerда
 * ko'zga tashlanmasdi va formaning pastida qolib ketardi — foydalanuvchi tugmani bosib,
 * "hech narsa bo'lmadi" deb o'ylardi. Toast esa kontent ustida, ekranning pastida chiqadi
 * va [TOAST_MILLIS] dan keyin o'zi yo'qoladi (bosilsa — darrov).
 *
 * Ildizdagi `Box` ichida, kontentdan **keyin** chaqiriladi (`BoxScope` shuning uchun kerak).
 *
 * [hint] — ixtiyoriy ikkinchi qator: nima qilish kerakligi ("chegara to'ldi" o'zi yechim
 * aytmaydi).
 */
@Composable
fun BoxScope.AppToast(
    message: String?,
    onDismiss: () -> Unit,
    tone: BannerTone = BannerTone.DANGER,
    hint: String? = null,
    palette: AppPalette = appPalette,
) {
    // Matn o'zgarsa hisob qaytadan boshlanadi — ketma-ket ikki xato bir xil vaqt ko'rinadi.
    LaunchedEffect(message) {
        if (message != null) {
            delay(TOAST_MILLIS)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        val (background, foreground) = when (tone) {
            BannerTone.DANGER -> palette.dangerBg to palette.danger
            BannerTone.WARNING -> palette.warningBg to palette.warning
            BannerTone.SUCCESS -> palette.successBg to palette.success
            BannerTone.INFO -> palette.accentBg to palette.primary
        }
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = 20.dp)
                .cardShadow()
                .clip(AppRadius.md)
                .background(background)
                .clickable(onClick = onDismiss)
                .padding(horizontal = AppSpacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(AppIcons.Bell, null, tint = foreground, modifier = Modifier.size(AppSize.iconSm))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // `message` null bo'lgan kadrda ham chiqish animatsiyasi davom etadi —
                // shuning uchun bo'sh satr (matn sakrab yo'qolmasin).
                Text(message.orEmpty(), style = AppType.fieldLabel.copy(color = foreground))
                if (hint != null) {
                    Text(hint, style = AppType.caption.copy(color = foreground))
                }
            }
        }
    }
}

/** Toast ustiga hech narsa chizilmasa ham `Box` kerak — shu yordamchi kod takrorlanmasin. */
@Composable
fun ToastHost(
    message: String?,
    onDismiss: () -> Unit,
    tone: BannerTone = BannerTone.DANGER,
    hint: String? = null,
    content: @Composable () -> Unit,
) {
    Box {
        content()
        AppToast(message, onDismiss, tone, hint)
    }
}
