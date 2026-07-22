package dev.core.uikit.component

import androidx.compose.runtime.Composable

/**
 * iOS'da tizim darajasidagi "orqaga" tugmasi yo'q, ilova esa `UINavigationController`
 * ishlatmaydi — demak ushlab qoladigan ishora ham yo'q. Ekrandagi "orqaga" tugmasi
 * bevosita chaqiriladi, shuning uchun bu yerda hech narsa qilinmaydi.
 */
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
