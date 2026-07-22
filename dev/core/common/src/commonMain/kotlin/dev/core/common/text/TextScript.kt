package dev.core.common.text

/**
 * Yozuv (alifbo) qoidalari.
 *
 * Biznes nomi **faqat lotin alifbosida** bo'lishi kerak: bir xil biznes bir joyda "Кафе Nur",
 * boshqa joyda "Kafe Nur" deb yozilsa qidiruv ikkalasini bog'lay olmaydi va ro'yxatda
 * dublikat ko'rinadi. Shuning uchun kirill yozuvi kiritilishiga yo'l qo'yilmaydi.
 */
object TextScript {

    /**
     * Kirill blokidagi belgimi (U+0400–U+04FF)?
     *
     * Bu diapazon ruscha harflar bilan birga o'zbek kirillining qo'shimcha harflarini ham
     * qamraydi (ў U+045E, қ U+049B, ғ U+0493, ҳ U+04B3) — alohida tekshirish shart emas.
     */
    fun isCyrillic(ch: Char): Boolean = ch in 'Ѐ'..'ӿ'

    /** Matnda kamida bitta kirill harfi bormi. */
    fun hasCyrillic(text: String): Boolean = text.any { isCyrillic(it) }

    /** Kirill harflarini olib tashlaydi; qolgan belgilar (bo'shliq, raqam, tinish) saqlanadi. */
    fun stripCyrillic(text: String): String = text.filterNot { isCyrillic(it) }
}
