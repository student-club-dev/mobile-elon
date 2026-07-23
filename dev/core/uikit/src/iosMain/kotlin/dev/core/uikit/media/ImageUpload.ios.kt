package dev.core.uikit.media

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
actual fun prepareImageForUpload(bytes: ByteArray, fileName: String): PickedImage {
    val image = bytes.toNSData()?.let { UIImage.imageWithData(it) }
        ?: return PickedImage(bytes, fileName) // format tanilmadi — aslini yuboramiz

    val scaled = image.scaledToFit(ImageUploadLimits.MAX_DIMENSION)
    val png = bytes.isPng()

    val encoded = if (png) scaled.encodePng() else scaled.encodeJpeg()
    // Shaffof PNG siqilgach ham katta bo'lsa — JPEG'ga o'tamiz (shaffoflik yo'qoladi, lekin
    // yuklanmaydigan fayldan ko'ra shu afzal).
    return if (png && encoded != null && encoded.size > ImageUploadLimits.MAX_BYTES) {
        val jpeg = scaled.encodeJpeg() ?: return PickedImage(encoded, fileName.withExtension("png"))
        PickedImage(jpeg, fileName.withExtension("jpg"))
    } else {
        PickedImage(
            bytes = encoded ?: bytes,
            fileName = fileName.withExtension(if (png) "png" else "jpg"),
        )
    }
}

/** Uzun tomoni [max] dan oshsa — nisbatni saqlab qayta chizadi. */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.scaledToFit(max: Int): UIImage {
    val width = size.useContents { width }
    val height = size.useContents { height }
    val factor = scaleFactor(maxOf(width, height).roundToInt(), max)
    if (factor >= 1.0) return this

    val targetWidth = (width * factor).coerceAtLeast(1.0)
    val targetHeight = (height * factor).coerceAtLeast(1.0)

    // `opaque = false` — shaffoflik saqlanadi; `scale = 1.0` — natija aynan shu pikselда bo'ladi
    // (aks holda Retina'da 2–3 barobar katta chiqardi).
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), false, 1.0)
    drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
    val result = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return result ?: this
}

/** Hajm chegarasiga sig'guncha sifatni bosqichma-bosqich tushiradi. */
private fun UIImage.encodeJpeg(): ByteArray? {
    var quality = ImageUploadLimits.INITIAL_QUALITY
    var data = UIImageJPEGRepresentation(this, quality / 100.0)?.toByteArray() ?: return null
    while (data.size > ImageUploadLimits.MAX_BYTES && quality > ImageUploadLimits.MIN_QUALITY) {
        quality -= 10
        data = UIImageJPEGRepresentation(this, quality / 100.0)?.toByteArray() ?: return data
    }
    return data
}

/** PNG yo'qotishsiz — hajmni faqat o'lcham belgilaydi. */
private fun UIImage.encodePng(): ByteArray? = UIImagePNGRepresentation(this)?.toByteArray()

// ---------------------------------------------------------------------------
// NSData ↔ ByteArray
// ---------------------------------------------------------------------------

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}
