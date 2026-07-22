package dev.core.uikit.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Galereyadan tanlangan rasm. */
class PickedImage(
    val bytes: ByteArray,
    /** Kengaytmani aniqlash uchun fayl nomi, masalan "avatar.jpg". */
    val fileName: String,
)

/** Rasm tanlashni ishga tushiruvchi. */
fun interface ImagePicker {
    fun pick()
}

/**
 * Galereyadan rasm tanlagich (Android: `PickVisualMedia`, iOS: `PHPickerViewController`).
 * Ikkala platformada ham **ruxsat so'ramaydi** — tizim tanlagichi faqat tanlangan rasmni beradi.
 *
 * [onResult] `null` qaytsa — foydalanuvchi bekor qildi yoki rasmni o'qib bo'lmadi.
 */
@Composable
expect fun rememberImagePicker(onResult: (PickedImage?) -> Unit): ImagePicker

/** Tanlangan rasm baytlarini darrov ko'rsatish uchun (format qo'llab-quvvatlanmasa `null`). */
expect fun ByteArray.toImageBitmapOrNull(): ImageBitmap?
