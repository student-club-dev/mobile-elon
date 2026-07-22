package dev.core.uikit.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** O'zbekiston xalqaro kodi — saqlangan raqamlar shu prefiks bilan yoziladi. */
const val UZ_DIALING_CODE = "+998"

/**
 * Saqlangan to'liq raqamdan ("+998901234567") maydonga mos 9 xonali milliy qismni ajratadi.
 *
 * Maydon faqat milliy qismni saqlaydi, `+998` esa yorliq sifatida yonida turadi. To'liq raqamni
 * maydonga shundoq berib bo'lmaydi: `998901234` bo'lib kesilib qolardi.
 */
fun nationalPhoneDigits(stored: String?): String =
    stored.orEmpty().removePrefix(UZ_DIALING_CODE).filter { it.isDigit() }.takeLast(9)

/** Maydondagi milliy qismni saqlash uchun to'liq raqamga aylantiradi. Bo'sh bo'lsa `null`. */
fun fullUzPhoneOrNull(nationalDigits: String): String? =
    nationalDigits.filter { it.isDigit() }.takeIf { it.isNotBlank() }?.let { "$UZ_DIALING_CODE$it" }

/** 9 xonali milliy raqamni "90 123 45 67" ko'rinishida formatlaydi. */
fun formatUzPhone(digits: String): String {
    val d = digits.filter { it.isDigit() }.take(9)
    val sb = StringBuilder()
    for (i in d.indices) {
        sb.append(d[i])
        if ((i == 1 || i == 4 || i == 6) && i != d.lastIndex) sb.append(' ')
    }
    return sb.toString()
}

/** Telefon maydonida raqamni "## ### ## ##" bo'yicha ko'rsatib, kursor mosligini saqlaydi. */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val out = formatUzPhone(text.text)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var t = offset
                if (offset > 2) t++
                if (offset > 5) t++
                if (offset > 7) t++
                return t.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var o = offset
                if (offset > 2) o--
                if (offset > 6) o--
                if (offset > 9) o--
                return o.coerceIn(0, out.filter { it.isDigit() }.length)
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}
