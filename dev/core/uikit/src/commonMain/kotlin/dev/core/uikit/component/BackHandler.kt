package dev.core.uikit.component

import androidx.compose.runtime.Composable

/**
 * Tizimning "orqaga" ishorasini ushlab qoladi.
 *
 * Nima uchun kerak: ba'zi ekranlar ichida QATLAM bor — xaritadan joy tanlash, Modal Bottom
 * Sheet, profil ichidagi bo'lim ro'yxati. Ular alohida navigatsiya manzili emas, ekranning
 * o'z holati. Shuning uchun tizim "orqaga" tugmasi ularni yopmasdan BUTUN ekranni yopib
 * yuborardi — foydalanuvchi bir qadam o'rniga ikki qadam orqaga qaytardi.
 *
 * [enabled] `false` bo'lsa ishora odatdagidek navigatsiyaga o'tadi.
 *
 * Compose Multiplatform 1.7 da umumiy `BackHandler` yo'q, shuning uchun platformaga xos.
 */
@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)
