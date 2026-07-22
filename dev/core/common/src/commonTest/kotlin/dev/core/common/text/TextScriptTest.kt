package dev.core.common.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextScriptTest {

    @Test
    fun detectsRussianLetters() {
        assertTrue(TextScript.hasCyrillic("Кафе"))
        assertTrue(TextScript.hasCyrillic("Nur кафе"))
    }

    @Test
    fun detectsUzbekCyrillicExtras() {
        // ў, қ, ғ, ҳ — kirill blokining kengaytmasi; ular ham taqiqlanadi.
        assertTrue(TextScript.hasCyrillic("Ўзбекистон"))
        assertTrue(TextScript.hasCyrillic("Қўқон"))
        assertTrue(TextScript.hasCyrillic("Ғижduvon"))
        assertTrue(TextScript.hasCyrillic("Ҳилол"))
    }

    @Test
    fun allowsLatinWithUzbekApostropheAndDigits() {
        assertFalse(TextScript.hasCyrillic("Qo'qon Kafe 24"))
        assertFalse(TextScript.hasCyrillic("Go'zallik saloni"))
        assertFalse(TextScript.hasCyrillic("Café Nur")) // lotin diakritikasi — ruxsat
    }

    @Test
    fun stripKeepsEverythingExceptCyrillic() {
        assertEquals("Nur  24", TextScript.stripCyrillic("Nur кафе 24"))
        assertEquals("Qo'qon", TextScript.stripCyrillic("Qo'qon"))
        assertEquals("", TextScript.stripCyrillic("Кафе"))
    }
}
