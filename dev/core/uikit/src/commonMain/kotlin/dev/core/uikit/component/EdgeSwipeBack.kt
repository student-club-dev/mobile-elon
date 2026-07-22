package dev.core.uikit.component

import androidx.compose.runtime.Composable

/**
 * Chap chetdan o'ngga surib "orqaga" qaytish ishorasi — **faqat iOS uchun**.
 *
 * Nima uchun kerak: Androidda tizimning o'z "orqaga" ishorasi bor va uni
 * [AppBackHandler] ushlab qoladi. iOS'da esa ilova `UINavigationController`
 * ishlatmaydi, shuning uchun tizim darajasidagi surish ishorasi umuman yo'q —
 * foydalanuvchi faqat ekrandagi `‹` tugmasiga bosishi mumkin edi. Bu iOS
 * odatlariga zid, shuning uchun ishorani o'zimiz qo'shamiz.
 *
 * Ishora [AppBackHandler] bilan bir xil tartibda ishlaydi: avval ekran ichidagi
 * eng ustki qatlam (xarita, tanlash varag'i, bo'lim ro'yxati) yopiladi; agar
 * bunday qatlam bo'lmasa, [onBack] chaqiriladi — ya'ni bir qadam ortga.
 *
 * Androidda hech narsa qilmaydi: [content] o'zgarishsiz ko'rsatiladi.
 */
@Composable
expect fun EdgeSwipeBack(
    enabled: Boolean = true,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
)
