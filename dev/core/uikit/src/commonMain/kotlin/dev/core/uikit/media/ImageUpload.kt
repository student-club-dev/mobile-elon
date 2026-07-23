package dev.core.uikit.media

/**
 * Rasmni serverga yuborishdan oldingi chegaralar.
 *
 * Backend `POST /v1/media/upload` da **5 MB** chegarasi bor va `thumbUrl`/`cardUrl` ni
 * qaytarmaydi (v1 da doim `null`) — ya'ni ro'yxatlarда ham aynan shu fayl yuklanadi.
 * Shuning uchun rasm klientда kichiklashtiriladi: zamonaviy telefon kamerasi 12 MP rasmni
 * 3–8 MB qilib beradi, u chegaradan oshib ketardi.
 */
object ImageUploadLimits {
    /** Uzun tomonining eng katta o'lchami (px). Avatar/logo/e'lon rasmi uchun yetarli. */
    const val MAX_DIMENSION = 1600

    /** Maqsadli hajm — backend chegarasidan (5 MB) pastroq, zaxira bilan. */
    const val MAX_BYTES = 4 * 1024 * 1024

    /** JPEG boshlang'ich sifati (%). */
    const val INITIAL_QUALITY = 85

    /** Sifatni bundan pastga tushirmaymiz — rasm ko'zga tashlanadigan darajada buziladi. */
    const val MIN_QUALITY = 50
}

/**
 * Rasmni yuklashga tayyorlaydi: uzun tomonini [ImageUploadLimits.MAX_DIMENSION] gacha
 * kichiklashtiradi va [ImageUploadLimits.MAX_BYTES] ga sig'guncha siqadi.
 *
 * Format tanlash: manba **PNG** bo'lsa natija ham PNG bo'lib qoladi (logotiplarda shaffof fon
 * bo'ladi, JPEG uni qora qilib qo'yardi). PNG siqilgach ham katta bo'lsa — JPEG'ga o'tiladi.
 *
 * Xato bo'lsa (format tanilmadi, xotira yetmadi) **asl baytlar** qaytadi: yuklash imkoniyatini
 * butunlay yo'qotgandan ko'ra, katta fayl bilan urinib ko'rgan ma'qul.
 *
 * Chaqiruvchi buni fon oqimida bajarishi kerak — kattaroq rasmda bir necha yuz millisekund oladi.
 */
expect fun prepareImageForUpload(bytes: ByteArray, fileName: String): PickedImage

/**
 * Baytlar PNG'mi — sarlavhadagi imzo bo'yicha (`89 50 4E 47`).
 * Fayl nomiga ishonmaymiz: galereya `.jpg` nomli PNG ham berishi mumkin.
 */
internal fun ByteArray.isPng(): Boolean =
    size > 8 &&
        this[0] == 0x89.toByte() && this[1] == 0x50.toByte() &&
        this[2] == 0x4E.toByte() && this[3] == 0x47.toByte()

/** Kichiklashtirish koeffitsienti — uzun tomon [max] dan oshmasligi uchun (1.0 dan katta emas). */
internal fun scaleFactor(longestSide: Int, max: Int = ImageUploadLimits.MAX_DIMENSION): Double =
    if (longestSide <= max || longestSide <= 0) 1.0 else max.toDouble() / longestSide

/** Fayl nomining kengaytmasini haqiqiy formatga moslaydi ("photo.heic" → "photo.jpg"). */
internal fun String.withExtension(extension: String): String =
    substringBeforeLast('.', this).ifBlank { "image" } + ".$extension"
