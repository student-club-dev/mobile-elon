package dev.core.uikit.util

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Summani guruhlash — narx maydonining ko'rinishi shu funksiyaga tayanadi, kursor
 * joylashuvi esa [MoneyVisualTransformation] ning moslamasiga.
 */
class MoneyFormatTest {

    @Test
    fun `uch xonagacha guruhlanmaydi`() {
        assertEquals("", formatMoneyDigits(""))
        assertEquals("5", formatMoneyDigits("5"))
        assertEquals("500", formatMoneyDigits("500"))
    }

    @Test
    fun `uch xonadan ortigi bosh joy bilan ajratiladi`() {
        assertEquals("1 000", formatMoneyDigits("1000"))
        assertEquals("50 000", formatMoneyDigits("50000"))
        assertEquals("1 250 000", formatMoneyDigits("1250000"))
        assertEquals("48 484 848 848", formatMoneyDigits("48484848848"))
    }

    @Test
    fun `formatlangan summa xom raqamlarga qaytadi`() {
        assertEquals("1250000", parseMoneyDigits("1 250 000"))
        assertEquals("50000", parseMoneyDigits("50 000 UZS"))
    }

    /** Kursor oxirida turganda u formatlangan matnning ham oxirida bo'lishi kerak. */
    @Test
    fun `kursor oxiri ajratgichlarni hisobga oladi`() {
        val transformed = MoneyVisualTransformation.filter(AnnotatedString("1250000"))
        assertEquals("1 250 000", transformed.text.text)
        assertEquals(9, transformed.offsetMapping.originalToTransformed(7))
        assertEquals(7, transformed.offsetMapping.transformedToOriginal(9))
    }

    /** Kursor o'rtasida turganda ham xom va ko'rinadigan indekslar mos kelishi kerak. */
    @Test
    fun `kursor ortasi togri joylashadi`() {
        val transformed = MoneyVisualTransformation.filter(AnnotatedString("1250000"))
        // "1| 250 000" — birinchi raqamdan keyin ajratgich hali qo'yilmagan.
        assertEquals(1, transformed.offsetMapping.originalToTransformed(1))
        // "1 250| 000" — bitta ajratgichdan keyin.
        assertEquals(5, transformed.offsetMapping.originalToTransformed(4))
        assertEquals(4, transformed.offsetMapping.transformedToOriginal(5))
    }
}
