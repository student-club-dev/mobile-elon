package dev.core.uikit.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Guruhlar orasidagi ajratgich — nuqta yoki vergul emas, **bo'sh joy**. */
const val MONEY_GROUP_SEPARATOR = ' '

/** Uch xonadan guruhlanadi: `50 000`, `1 250 000`. */
private const val GROUP_SIZE = 3

/**
 * Raqamlar qatorini o'qiladigan summaga aylantiradi: `"1250000"` → `"1 250 000"`.
 *
 * Kirish faqat raqamlardan iborat deb kutiladi (maydon o'zi shunday tozalaydi).
 */
fun formatMoneyDigits(digits: String): String {
    if (digits.length <= GROUP_SIZE) return digits
    val builder = StringBuilder(digits.length + digits.length / GROUP_SIZE)
    val firstGroup = digits.length % GROUP_SIZE
    if (firstGroup > 0) builder.append(digits, 0, firstGroup)
    var index = firstGroup
    while (index < digits.length) {
        if (builder.isNotEmpty()) builder.append(MONEY_GROUP_SEPARATOR)
        builder.append(digits, index, index + GROUP_SIZE)
        index += GROUP_SIZE
    }
    return builder.toString()
}

/** Formatlangan summani xom raqamlarga qaytaradi: `"1 250 000"` → `"1250000"`. */
fun parseMoneyDigits(text: String): String = text.filter { it.isDigit() }

/**
 * Summani kiritish paytining O'ZIDA formatlab ko'rsatadi.
 *
 * Nega niqob (visual transformation), forma holatini formatlash emas: holatda **xom
 * raqamlar** qoladi (`"48484848848"`) va uni `toLongOrNull()` bilan o'qiydigan hamma joy
 * o'zgarishsiz ishlayveradi. Formatlash faqat ko'rinishda bo'lgani uchun u har bir belgi
 * kiritilganда darhol qo'llanadi — `blur` ni kutmaydi. Ilgari uzun summa (`48484848848`)
 * bir uzluksiz raqam bo'lib chiqib, foydalanuvchi nol sonini sanashga majbur bo'lardi.
 */
object MoneyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = formatMoneyDigits(digits)

        // Ajratgichlar chapdan qo'shiladi, shuning uchun kursor siljishi pozitsiyaga qarab
        // o'zgaradi — uni "shu nuqtagacha nechta ajratgich bor" deb hisoblaymiz.
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (digits.isEmpty()) return 0
                val safe = offset.coerceIn(0, digits.length)
                return safe + separatorsBefore(digits.length, safe)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safe = offset.coerceIn(0, formatted.length)
                // Formatlangan matndagi `safe` gacha nechta RAQAM borligi — xom indeks shu.
                return formatted.take(safe).count { it.isDigit() }
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }

    /**
     * `digitCount` uzunlikdagi sonda birinchi `offset` ta raqamdan oldin nechta ajratgich
     * turishini qaytaradi.
     */
    private fun separatorsBefore(digitCount: Int, offset: Int): Int {
        if (digitCount <= GROUP_SIZE || offset == 0) return 0
        val firstGroup = digitCount % GROUP_SIZE
        val head = if (firstGroup == 0) GROUP_SIZE else firstGroup
        if (offset <= head) return 0
        return 1 + (offset - head - 1) / GROUP_SIZE
    }
}
