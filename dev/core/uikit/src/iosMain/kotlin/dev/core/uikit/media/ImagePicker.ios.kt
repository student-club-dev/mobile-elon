package dev.core.uikit.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

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
            val picked = data?.toByteArray()?.let { PickedImage(it, "image.jpg") }
            // Callback fon oqimida keladi — UI holatiga faqat asosiy oqimdan tegamiz.
            dispatch_async(dispatch_get_main_queue()) { onResult(picked) }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}

actual fun ByteArray.toImageBitmapOrNull(): ImageBitmap? =
    runCatching { Image.makeFromEncoded(this).toComposeImageBitmap() }.getOrNull()
