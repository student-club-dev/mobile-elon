package dev.core.uikit.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** Har qanday rasm formati — PHPicker shu identifikator bo'yicha ma'lumot beradi. */
private const val UTI_IMAGE = "public.image"

@Composable
actual fun rememberImagePicker(onResult: (PickedImage?) -> Unit): ImagePicker {
    // Bitta rasm — ko'p tanlovli oqimning chegara holati: birinchi natijani olamiz.
    val multi = rememberMultiImagePicker(maxItems = 1) { picked -> onResult(picked.firstOrNull()) }
    return multi
}

@Composable
actual fun rememberMultiImagePicker(
    maxItems: Int,
    onResult: (List<PickedImage>) -> Unit,
): ImagePicker {
    // Delegate Compose qayta chizilishlari orasida saqlanishi kerak — aks holda
    // PHPicker javob qaytarguncha u yig'ib yuboriladi va callback hech qachon kelmaydi.
    val delegate = remember { PhotoPickerDelegate() }
    delegate.onResult = onResult

    val limit = maxItems.coerceAtLeast(1)
    return remember(delegate, limit) {
        ImagePicker {
            val config = PHPickerConfiguration().apply {
                setFilter(PHPickerFilter.imagesFilter())
                setSelectionLimit(limit.toLong())
            }
            val picker = PHPickerViewController(configuration = config)
            picker.delegate = delegate

            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class PhotoPickerDelegate : NSObject(), PHPickerViewControllerDelegateProtocol {

    var onResult: (List<PickedImage>) -> Unit = {}

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            onResult(emptyList()) // bekor qilindi
            return
        }

        // Har bir rasm ALOHIDA va parallel yuklanadi, javoblari esa istalgan tartibda keladi.
        // Shuning uchun natijalar indeks bo'yicha o'z uyachasiga yoziladi — foydalanuvchi
        // tanlagan tartib saqlanadi (birinchi rasm e'lonning muqovasi bo'ladi).
        val slots = MutableList<PickedImage?>(results.size) { null }
        var completed = 0

        results.forEachIndexed { index, result ->
            result.itemProvider.loadDataRepresentationForTypeIdentifier(UTI_IMAGE) { data, _ ->
                // Yuklashdan oldin kichiklashtirib siqamiz (backend chegarasi — 5 MB).
                // Callback fon oqimida keladi, shuning uchun siqish UI'ni bloklamaydi.
                val picked = data?.toByteArray()?.let { prepareImageForUpload(it, "image$index.jpg") }
                // Hisoblagich va uyachalar FAQAT asosiy oqimda o'zgaradi — bu ularni
                // qulfsiz xavfsiz qiladi va UI holatiga ham shu yerdan tegiladi.
                dispatch_async(dispatch_get_main_queue()) {
                    slots[index] = picked
                    completed += 1
                    // O'qib bo'lmagan fayllar tashlab yuboriladi, qolganlari qabul qilinadi.
                    if (completed == results.size) onResult(slots.filterNotNull())
                }
            }
        }
    }
}

actual fun ByteArray.toImageBitmapOrNull(): ImageBitmap? =
    runCatching { Image.makeFromEncoded(this).toComposeImageBitmap() }.getOrNull()
