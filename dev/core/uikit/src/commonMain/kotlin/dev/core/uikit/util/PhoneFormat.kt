package dev.core.uikit.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

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
