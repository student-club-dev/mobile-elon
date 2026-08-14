package dev.core.uikit.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.cardShadow
import kotlinx.coroutines.delay

/**
 * Tasdiq/ma'lumot toasti qancha turadi — o'qishga yetadi, lekin ishni to'xtatmaydi.
 * Xato uzunroq turadi: unda odatda nima qilish kerakligi ham yozilgan bo'ladi.
 */
private const val TOAST_MILLIS = 2500L
private const val TOAST_ERROR_MILLIS = 4000L

/** Ekranда turgan bitta xabar. [id] — ketma-ket bir xil matn qayta ko'rsatilsin uchun. */
data class ToastMessage(
    val text: String,
    val tone: BannerTone = BannerTone.INFO,
    val hint: String? = null,
    val id: Long = 0,
)

/**
 * Butun ilova uchun bitta toast navbati.
 *
 * Nega global: xato matni ilgari har bir ekranда forma **ichida**, tugma tagida kichik qizil
 * yozuv bo'lardi. Uzun matn (serverning "hisobingiz bloklangan" kabi javobi) u yerда ko'zga
 * tashlanmasdi, ba'zan esa ekrandan chiqib ketardi — foydalanuvchi tugmani bosib, "hech narsa
 * bo'lmadi" deb o'ylardi. Endi xabar kontent USTIDA, ekranning yuqori o'ng burchagidan suzib
 * chiqadi va o'zi yo'qoladi.
 */
@Stable
class ToastController {
    var current: ToastMessage? by mutableStateOf(null)
        private set

    private var sequence = 0L

    fun show(text: String, tone: BannerTone = BannerTone.INFO, hint: String? = null) {
        if (text.isBlank()) return
        sequence += 1
        current = ToastMessage(text, tone, hint, sequence)
    }

    fun success(text: String, hint: String? = null) = show(text, BannerTone.SUCCESS, hint)

    fun error(text: String, hint: String? = null) = show(text, BannerTone.DANGER, hint)

    fun dismiss() {
        current = null
    }
}

val LocalToastController = staticCompositionLocalOf { ToastController() }

/** Joriy toast navbati — istalgan ekrandan `toasts.error("...")` deb chaqiriladi. */
val toastController: ToastController
    @Composable get() = LocalToastController.current

/**
 * Ilova ildizidagi toast qatlami — [content] ni o'rab, uning USTIGA xabarni chizadi.
 *
 * Ildizda bir marta chaqiriladi (`AppScaffold`), shundan keyin har qanday ekran
 * [LocalToastController] orqali xabar ko'rsata oladi.
 */
@Composable
fun AppToastHost(
    controller: ToastController = remember { ToastController() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalToastController provides controller) {
        Box(Modifier.fillMaxSize()) {
            content()
            ToastOverlay(controller)
        }
    }
}

/**
 * Holatdagi bitta martalik xabarni toastga uzatadi va darhol "iste'mol qilingan" deb
 * belgilaydi — aks holда ekran har qayta chizilganда xabar qaytadan chiqaverardi.
 */
@Composable
fun ToastEffect(
    message: String?,
    tone: BannerTone = BannerTone.DANGER,
    hint: String? = null,
    onConsumed: () -> Unit,
) {
    val controller = LocalToastController.current
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            controller.show(message, tone, hint)
            onConsumed()
        }
    }
}

/** Yuqori o'ng burchakdagi xabar qatlami. */
@Composable
private fun BoxScope.ToastOverlay(controller: ToastController, palette: AppPalette = appPalette) {
    val message = controller.current

    // Matn o'zgarsa hisob qaytadan boshlanadi — ketma-ket ikki xabar bir xil vaqt ko'rinadi.
    LaunchedEffect(message?.id) {
        if (message != null) {
            delay(if (message.tone == BannerTone.DANGER) TOAST_ERROR_MILLIS else TOAST_MILLIS)
            controller.dismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        // O'ngdan suzib kiradi — bildirishnoma ohangi, kontentni turtib yubormaydi.
        enter = fadeIn() + slideInHorizontally { it },
        exit = fadeOut() + slideOutHorizontally { it },
        modifier = Modifier.align(Alignment.TopEnd)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        val (background, foreground) = when (message?.tone ?: BannerTone.INFO) {
            BannerTone.DANGER -> palette.dangerBg to palette.danger
            BannerTone.WARNING -> palette.warningBg to palette.warning
            BannerTone.SUCCESS -> palette.successBg to palette.success
            BannerTone.INFO -> palette.accentBg to palette.primary
        }
        val icon: ImageVector = when (message?.tone ?: BannerTone.INFO) {
            BannerTone.DANGER, BannerTone.WARNING -> AppIcons.Bell
            BannerTone.SUCCESS -> AppIcons.Check
            BannerTone.INFO -> AppIcons.Bell
        }
        Row(
            Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                // Kengligi kontentga mos, lekin tor ekranда ham chetdan chiqmaydi.
                .widthIn(max = 320.dp)
                .cardShadow(AppRadius.md)
                .clip(AppRadius.md)
                .background(background)
                .clickable(onClick = controller::dismiss)
                .padding(horizontal = AppSpacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(icon, null, tint = foreground, modifier = Modifier.size(AppSize.iconSm))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // `message` null bo'lgan kadrda ham chiqish animatsiyasi davom etadi —
                // shuning uchun bo'sh satr (matn sakrab yo'qolmasin).
                Text(message?.text.orEmpty(), style = AppType.fieldLabel.copy(color = foreground))
                message?.hint?.let {
                    Text(it, style = AppType.caption.copy(color = foreground))
                }
            }
        }
    }
}
