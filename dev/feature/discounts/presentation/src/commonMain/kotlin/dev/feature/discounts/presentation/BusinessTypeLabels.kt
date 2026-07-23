package dev.feature.discounts.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.core.uikit.component.AppIcons
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_type_barbershop
import dev.core.uikit.resources.discounts_type_beauty_salon
import dev.core.uikit.resources.discounts_type_clothing
import dev.core.uikit.resources.discounts_type_education_center
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.ListingCatalog
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Biznes turining tarjimasi va ikonkasi.
 *
 * Nima uchun bu yerda, `BusinessType` ichida emas: `BusinessType` — **domain** modeli va u
 * Compose'ga bog'liq emas (bog'lansa domain UI kutubxonasidan qaram bo'lib qolardi). Domain
 * faqat barqaror kalitni saqlaydi, ko'rinish esa shu faylda.
 *
 * ⚠️ Turlar ro'yxati **serverda** (hozircha 27 ta va o'sib boradi), shuning uchun bu yerdagi
 * moslamalar to'liq bo'lishi shart emas: topilmagan tur uchun standart ikonka va serverdan
 * kelgan nom ishlatiladi. Ya'ni adminka yangi tur qo'shsa ilova uni baribir to'g'ri ko'rsatadi.
 */

/**
 * Tur nomining tarjimasi — faqat ilova **maxsus tarjima qilgan** turlar uchun.
 *
 * Backend nomni faqat o'zbekcha beradi (`nameUz`; `nameRu` hozircha `null`), shuning uchun
 * ruscha/inglizcha rejimda qo'lda tarjima qilinganlari shu yerdan olinadi. Qolganlari uchun
 * `null` — chaqiruvchi serverdagi nomga tushadi.
 */
val BusinessType.labelRes: StringResource?
    get() = when (key) {
        "CLOTHING" -> Res.string.discounts_type_clothing
        "EDUCATION_CENTER" -> Res.string.discounts_type_education_center
        "BARBERSHOP" -> Res.string.discounts_type_barbershop
        "BEAUTY_SALON" -> Res.string.discounts_type_beauty_salon
        else -> null
    }

/**
 * Joriy tildagi nomi — ekranlarda shu ishlatiladi.
 *
 * Tartib: tarjima → zaxira katalogdagi o'zbekcha nom → kalitning o'zi. Oxirgisi deyarli
 * uchramaydi (server yangi tur qo'shgan, foydalanuvchi esa oflayn qolgan holat), lekin
 * bo'sh joydan ko'ra kalit ko'ringani yaxshiroq.
 */
@Composable
fun BusinessType.localizedLabel(): String =
    labelRes?.let { stringResource(it) } ?: ListingCatalog.info(this)?.nameUz ?: key

/**
 * Serverdan kelgan tur uchun nom: tarjima bo'lsa u, aks holda javobdagi `nameUz`.
 *
 * Ro'yxatlarda aynan shu ishlatiladi — u yerda javob qo'lda bo'lgani uchun zaxira katalogga
 * murojaat qilish shart emas.
 */
@Composable
fun BusinessTypeInfo.localizedLabel(): String =
    type.labelRes?.let { stringResource(it) } ?: nameUz

/**
 * Biznes turining ikonkasi (dizayn handoff `ic_*.svg` to'plamidan).
 *
 * Nima uchun emoji emas: emoji iOS'da Compose bilan chizilmaydi va `?` kvadratcha bo'lib
 * ko'rinadi. Backend `emoji` maydonini beradi, lekin u faqat ma'lumot uchun.
 *
 * Yaqin turlar bitta ikonkani baham ko'radi (barcha sport maydonlari — koptok), noma'lum
 * tur esa neytral "do'kon" ikonkasini oladi.
 */
val BusinessType.icon: ImageVector
    get() = when (key) {
        // Sport maydonlari va zallari
        "TENNIS", "TABLE_TENNIS", "FOOTBALL_FIELD", "FOOTBALL_TRAINING", "BASKETBALL",
        "VOLLEYBALL", "BOWLING", "BILLIARDS", "SWIMMING_POOL", "FITNESS", "BOXING",
        "WRESTLING_MMA",
        -> AppIcons.Ball

        // O'yin zallari
        "PLAYSTATION" -> AppIcons.Gamepad
        "CYBER_CLUB" -> AppIcons.Laptop

        // Ko'ngilochar
        "CINEMA" -> AppIcons.Camera
        "KARAOKE" -> AppIcons.Mic

        // Ta'lim
        "EDUCATION_CENTER", "LIBRARY" -> AppIcons.Book
        "TUTOR" -> AppIcons.GraduationCap

        // Ovqatlanish
        "NATIONAL_FOOD", "FAST_FOOD", "SOMSA" -> AppIcons.Cafe

        // Xizmatlar va savdo
        "BARBERSHOP" -> AppIcons.Tools
        "BEAUTY_SALON" -> AppIcons.Star
        "PRINTING" -> AppIcons.FileText
        "RENTAL_HOUSE" -> AppIcons.Home
        "CLOTHING" -> AppIcons.Cart

        else -> AppIcons.Store
    }

/**
 * Turning urg'u rangi (ARGB) — zaxira katalogdan.
 *
 * Serverdan kelgan `BusinessTypeInfo.accentColor` qo'lda bo'lganda o'shani ishlating; bu
 * xossa esa faqat tur kaliti ma'lum bo'lgan joylar uchun (masalan saqlangan e'lon kartasi).
 * Noma'lum tur — neytral binafsha.
 */
val BusinessType.catalogAccent: Long
    get() = ListingCatalog.info(this)?.accentColor ?: 0xFF7C5CFF
