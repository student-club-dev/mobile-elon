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
    // Delegate Compose qayta chizilishlari orasida saqlanishi kerak — aks holda
    // PHPicker javob qaytarguncha u yig'ib yuboriladi va callback hech qachon kelmaydi.
    val delegate = remember { PhotoPickerDelegate() }
    delegate.onResult = onResult

    return remember(delegate) {
        ImagePicker {
            val config = PHPickerConfiguration().apply {
                setFilter(PHPickerFilter.imagesFilter())
                setSelectionLimit(1)
            }
            val picker = PHPickerViewController(configuration = config)
            picker.delegate = delegate

            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class PhotoPickerDelegate : NSObject(), PHPickerViewControllerDelegateProtocol {

    var onResult: (PickedImage?) -> Unit = {}

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val provider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider
        if (provider == null) {
            onResult(null) // bekor qilindi
            return
        }

        provider.loadDataRepresentationForTypeIdentifier(UTI_IMAGE) { data, _ ->
            // Yuklashdan oldin kichiklashtirib siqamiz (backend chegarasi — 5 MB).
            // Callback fon oqimida keladi, shuning uchun siqish UI'ni bloklamaydi.
            val picked = data?.toByteArray()?.let { prepareImageForUpload(it, "image.jpg") }
            // Callback fon oqimida keladi — UI holatiga faqat asosiy oqimdan tegamiz.
            dispatch_async(dispatch_get_main_queue()) { onResult(picked) }
        }
    }
}

actual fun ByteArray.toImageBitmapOrNull(): ImageBitmap? =
    runCatching { Image.makeFromEncoded(this).toComposeImageBitmap() }.getOrNull()
