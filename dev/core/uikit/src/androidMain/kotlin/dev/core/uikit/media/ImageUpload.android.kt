package dev.core.uikit.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual fun prepareImageForUpload(bytes: ByteArray, fileName: String): PickedImage =
    runCatching {
        // 1-qadam: faqat o'lchamlarni o'qiymiz — butun rasmni xotiraga yuklamasdan.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        if (longest <= 0) return@runCatching null // format tanilmadi

        // 2-qadam: `inSampleSize` bilan darrov kichraytirib yuklaymiz — 12 MP rasmni to'liq
        // ochish 48 MB xotira talab qilardi va zaif qurilmada OutOfMemory beradi.
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(longest, ImageUploadLimits.MAX_DIMENSION)
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: return@runCatching null

        // 3-qadam: `inSampleSize` faqat 2 ning darajalari bilan ishlaydi, shuning uchun
        // aniq o'lchamga qo'shimcha masshtablash kerak bo'lishi mumkin.
        val scaled = decoded.scaledToFit(ImageUploadLimits.MAX_DIMENSION)

        val png = bytes.isPng()
        val output = if (png) scaled.encodePng() else scaled.encodeJpeg()
        // Shaffof PNG siqilgach ham katta bo'lsa — JPEG'ga o'tamiz (shaffoflik yo'qoladi,
        // lekin yuklanmaydigan fayldan ko'ra shu afzal).
        val result = if (png && output.size > ImageUploadLimits.MAX_BYTES) {
            scaled.encodeJpeg() to "jpg"
        } else {
            output to (if (png) "png" else "jpg")
        }

        if (scaled !== decoded) scaled.recycle()
        decoded.recycle()

        PickedImage(bytes = result.first, fileName = fileName.withExtension(result.second))
    }.getOrNull() ?: PickedImage(bytes, fileName)

/** Uzun tomon [max] dan kichik bo'lguncha 2 ga bo'lib boradi (BitmapFactory talabi). */
private fun sampleSizeFor(longest: Int, max: Int): Int {
    var sample = 1
    while (longest / (sample * 2) >= max) sample *= 2
    return sample
}

/** Uzun tomoni [max] dan oshsa — nisbatni saqlab kichiklashtiradi. */
private fun Bitmap.scaledToFit(max: Int): Bitmap {
    val factor = scaleFactor(maxOf(width, height), max)
    if (factor >= 1.0) return this
    return Bitmap.createScaledBitmap(
        this,
        (width * factor).roundToInt().coerceAtLeast(1),
        (height * factor).roundToInt().coerceAtLeast(1),
        true,
    )
}

/** Hajm chegarasiga sig'guncha sifatni bosqichma-bosqich tushiradi. */
private fun Bitmap.encodeJpeg(): ByteArray {
    var quality = ImageUploadLimits.INITIAL_QUALITY
    var encoded = compressTo(Bitmap.CompressFormat.JPEG, quality)
    while (encoded.size > ImageUploadLimits.MAX_BYTES && quality > ImageUploadLimits.MIN_QUALITY) {
        quality -= 10
        encoded = compressTo(Bitmap.CompressFormat.JPEG, quality)
    }
    return encoded
}

/** PNG yo'qotishsiz — `quality` e'tiborsiz qoldiriladi, hajmni faqat o'lcham belgilaydi. */
private fun Bitmap.encodePng(): ByteArray = compressTo(Bitmap.CompressFormat.PNG, 100)

private fun Bitmap.compressTo(format: Bitmap.CompressFormat, quality: Int): ByteArray =
    ByteArrayOutputStream().use { stream ->
        compress(format, quality, stream)
        stream.toByteArray()
    }
