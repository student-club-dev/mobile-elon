package dev.core.uikit.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * iOS'da tizim darajasidagi "orqaga" tugmasi yo'q, ammo [EdgeSwipeBack] chap chetdan
 * surish ishorasini beradi. Shu ishora kimga borishini quyidagi ro'yxat hal qiladi:
 * ekran ichidagi qatlamlar (xarita, tanlash varag'i, bo'lim ro'yxati) o'zini bu yerga
 * yozib qo'yadi.
 *
 * Eng oxirgi yozilgan ishlov beruvchi — eng ustki qatlam, shuning uchun [dispatch]
 * ro'yxatning oxiridan boshlaydi. Androidda `OnBackPressedDispatcher` xuddi shunday
 * ishlaydi, ya'ni ikki platformada xatti-harakat bir xil bo'ladi.
 */
internal object IosBackDispatcher {

    private val handlers = mutableListOf<() -> Unit>()

    fun register(handler: () -> Unit) {
        handlers += handler
    }

    fun unregister(handler: () -> Unit) {
        handlers -= handler
    }

    /** Eng ustki qatlamni yopadi. Qatlam bo'lmasa `false` — demak bir ekran ortga qaytiladi. */
    fun dispatch(): Boolean {
        val handler = handlers.lastOrNull() ?: return false
        handler()
        return true
    }
}

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Lambda har qayta chizishda yangilanishi mumkin, ro'yxatdagi yozuv esa o'zgarmaydi.
    val latest by rememberUpdatedState(onBack)
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }
        val handler: () -> Unit = { latest() }
        IosBackDispatcher.register(handler)
        onDispose { IosBackDispatcher.unregister(handler) }
    }
}
