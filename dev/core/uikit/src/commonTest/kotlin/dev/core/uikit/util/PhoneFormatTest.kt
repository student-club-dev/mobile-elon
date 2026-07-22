package dev.core.uikit.util

import dev.core.uikit.component.AppFieldType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Telefon raqamining maydon ↔ saqlash yo'li.
 *
 * Maydon faqat 9 xonali milliy qismni ushlaydi, bazada esa to'liq raqam (`+998…`) turadi.
 * Ikkalasini adashtirsak, saqlangan raqam jimgina buziladi: "+998901234567" dan `take(9)`
 * "998901234" ni beradi va foydalanuvchi buni faqat qo'ng'iroq qilmoqchi bo'lganda sezadi.
 */
class PhoneFormatTest {

    @Test
    fun nationalDigitsStripsCountryCode() {
        assertEquals("901234567", nationalPhoneDigits("+998901234567"))
        assertEquals("901234567", nationalPhoneDigits("998901234567"))
        assertEquals("901234567", nationalPhoneDigits("+998 90 123 45 67"))
        assertEquals("901234567", nationalPhoneDigits("901234567"))
    }

    @Test
    fun nationalDigitsHandlesMissingValue() {
        assertEquals("", nationalPhoneDigits(null))
        assertEquals("", nationalPhoneDigits(""))
    }

    @Test
    fun fullPhoneAddsCountryCode() {
        assertEquals("+998901234567", fullUzPhoneOrNull("901234567"))
        assertNull(fullUzPhoneOrNull(""))
    }

    @Test
    fun storedNumberSurvivesRoundTrip() {
        val stored = "+998901234567"
        assertEquals(stored, fullUzPhoneOrNull(nationalPhoneDigits(stored)))
    }

    @Test
    fun fieldTypeKeepsOnlyNineDigits() {
        val type = AppFieldType.UzPhone
        assertEquals("901234567", type.sanitize("90 123 45 67"))
        assertEquals("901234567", type.sanitize("901234567890")) // ortiqchasi kesiladi
        assertEquals("901234567", type.sanitize("90abc12345c67"))
        assertEquals("", type.sanitize("salom"))
    }

    @Test
    fun displayMaskGroupsDigits() {
        assertEquals("90 123 45 67", formatUzPhone("901234567"))
        assertEquals("90 123", formatUzPhone("90123")) // qisman kiritish
        assertEquals("9", formatUzPhone("9"))
    }

    @Test
    fun latinFieldTypeRejectsCyrillic() {
        assertEquals("Kafe ", AppFieldType.LatinText.sanitize("Kafe Кафе"))
        assertEquals("Qo'qon 24", AppFieldType.LatinText.sanitize("Qo'qon 24"))
    }
}
