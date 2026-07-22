package dev.core.uikit.component

import androidx.compose.runtime.Composable

/**
 * Androidda tizimning o'z "orqaga" ishorasi bor — uni [AppBackHandler] ushlaydi.
 * Qo'shimcha surish ishorasi ikki xil xatti-harakatga olib kelardi, shuning uchun
 * bu yerda hech narsa qilinmaydi.
 */
@Composable
actual fun EdgeSwipeBack(enabled: Boolean, onBack: () -> Unit, content: @Composable () -> Unit) {
    content()
}
