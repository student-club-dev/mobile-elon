package dev.core.uikit.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import dev.core.common.text.TextScript
import dev.core.uikit.util.MoneyVisualTransformation
import dev.core.uikit.util.PhoneVisualTransformation

/**
 * Matn maydonining **turi** — klaviatura, ko'rinish niqobi va kiritishni tozalash qoidasi
 * bitta joyda.
 *
 * Ilgari har bir ekran buni o'zi yig'ardi: biri telefonga `KeyboardType.Phone` qo'yardi,
 * ikkinchisi niqobni unutardi, uchinchisi raqamdan boshqasini filtrlamasdi. Natijada bir
 * xil maydon ekranga qarab boshqacha ishlardi. Endi tur beriladi — qolgani o'zi to'g'ri
 * bo'ladi.
 */
sealed class AppFieldType {

    open val keyboardOptions: KeyboardOptions = KeyboardOptions.Default
    open val visualTransformation: VisualTransformation = VisualTransformation.None

    /** Kiritilgan matnni maydon qoidasiga moslaydi (ruxsatsiz belgilar tushib qoladi). */
    open fun sanitize(input: String): String = input

    /** Oddiy matn — cheklovsiz. */
    data object Text : AppFieldType()

    /**
     * Lotin alifbosidagi matn — nom va tavsiflar uchun.
     *
     * Kirill harflari kiritilmaydi: bir xil narsa bir joyda "Кафе", boshqa joyda "Kafe" deb
     * yozilsa qidiruv ikkalasini bog'lay olmaydi.
     */
    data object LatinText : AppFieldType() {
        override fun sanitize(input: String): String = TextScript.stripCyrillic(input)
    }

    /**
     * O'zbekiston telefon raqami — `+998` prefiksidan KEYINGI 9 raqam.
     *
     * Holatda faqat raqamlar saqlanadi ("901234567"), ekranda esa `90 123 45 67` ko'rinishida
     * chiziladi. Maydon oldiga `+998` yorlig'i qo'yilsa, foydalanuvchi to'liq
     * `+998 90 123 45 67` ni ko'radi.
     */
    data object UzPhone : AppFieldType() {
        const val DIGITS = 9

        override val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        override val visualTransformation = PhoneVisualTransformation()

        override fun sanitize(input: String): String =
            input.filter { it.isDigit() }.take(DIGITS)
    }

    /** Faqat butun son — muddat, miqdor, foiz. */
    data object Number : AppFieldType() {
        override val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

        override fun sanitize(input: String): String = input.filter { it.isDigit() }
    }

    /**
     * Pul summasi — holatда xom raqamlar, ekranда `50 000` ko'rinishida.
     *
     * [Number] dan farqi faqat niqobда: summani o'qish uchun guruhlash SHART, chunki
     * `48484848848` kabi uzun raqamni foydalanuvchi tekshira olmaydi. Formatlash kiritish
     * paytining o'zida ishlaydi (`blur` kutilmaydi).
     */
    data object Money : AppFieldType() {
        /** Amaliy chegara — bundan uzun summa kiritilmaydi (xato yozuvdan himoya). */
        const val MAX_DIGITS = 12

        override val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        override val visualTransformation = MoneyVisualTransformation

        override fun sanitize(input: String): String =
            input.filter { it.isDigit() }.take(MAX_DIGITS)
    }

    /** Email — klaviatura mos, matn o'zgarmaydi. */
    data object Email : AppFieldType() {
        override val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    }
}
