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
 * Bitta rasm uchun: avatar, biznes logotipi. Bir nechta rasm kerak bo'lganda
 * [rememberMultiImagePicker] ishlatiladi.
 *
 * [onResult] `null` qaytsa — foydalanuvchi bekor qildi yoki rasmni o'qib bo'lmadi.
 */
@Composable
expect fun rememberImagePicker(onResult: (PickedImage?) -> Unit): ImagePicker

/**
 * Galereyadan **bir nechta** rasm tanlagich.
 *
 * Nega kerak: e'longa 5 tagacha rasm qo'yiladi va ilgari ularni bittalab tanlashga to'g'ri
 * kelardi — galereya har safar yopilib, qaytadan ochilardi va oldingi tanlov joyi yo'qolardi.
 * Tizim tanlagichlarining ikkalasi ham ko'p tanlashni qo'llab-quvvatlaydi
 * (Android: `PickMultipleVisualMedia`, iOS: `PHPickerConfiguration.selectionLimit`).
 *
 * [onResult] foydalanuvchi tanlagan **tartibda** qaytaradi (birinchisi — muqova) va bo'sh
 * ro'yxat "bekor qilindi" degani. O'qib bo'lmagan fayllar ro'yxatga tushmaydi.
 */
@Composable
expect fun rememberMultiImagePicker(
    /** Ko'pi bilan nechta rasm tanlash mumkin — odatda qolgan bo'sh joy. */
    maxItems: Int,
    onResult: (List<PickedImage>) -> Unit,
): ImagePicker

/** Tanlangan rasm baytlarini darrov ko'rsatish uchun (format qo'llab-quvvatlanmasa `null`). */
expect fun ByteArray.toImageBitmapOrNull(): ImageBitmap?
