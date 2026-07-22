package dev.feature.discounts.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.core.uikit.component.AppIcons
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_type_barbershop
import dev.core.uikit.resources.discounts_type_beauty_salon
import dev.core.uikit.resources.discounts_type_cafe_restaurant
import dev.core.uikit.resources.discounts_type_clothing
import dev.core.uikit.resources.discounts_type_education_center
import dev.core.uikit.resources.discounts_type_entertainment
import dev.core.uikit.resources.discounts_type_game_club
import dev.feature.discounts.domain.model.BusinessType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Biznes turining tarjima qilingan nomi.
 *
 * Nima uchun bu yerda, `BusinessType` enumida emas: `BusinessType` — **domain** modeli va u
 * Compose'ga bog'liq emas (bog'lansa domain UI kutubxonasidan qaram bo'lib qolardi). Shuning
 * uchun enum faqat barqaror `id` va o'zbekcha zaxira nomni saqlaydi, tarjima esa shu yerda.
 *
 * Eslatma: `BusinessTypeInfo.nameUz` backenddan keladi va nomidan ko'rinib turibdiki faqat
 * o'zbekcha. Ruscha/inglizcha rejimda undan foydalanib bo'lmaydi, shuning uchun ekranlarda
 * aynan shu funksiya ishlatiladi.
 */
val BusinessType.labelRes: StringResource
    get() = when (this) {
        BusinessType.GAME_CLUB -> Res.string.discounts_type_game_club
        BusinessType.CLOTHING -> Res.string.discounts_type_clothing
        BusinessType.CAFE_RESTAURANT -> Res.string.discounts_type_cafe_restaurant
        BusinessType.EDUCATION_CENTER -> Res.string.discounts_type_education_center
        BusinessType.ENTERTAINMENT -> Res.string.discounts_type_entertainment
        BusinessType.BARBERSHOP -> Res.string.discounts_type_barbershop
        BusinessType.BEAUTY_SALON -> Res.string.discounts_type_beauty_salon
    }

/** Joriy tildagi nomi — ekranlarda shu ishlatiladi. */
@Composable
fun BusinessType.localizedLabel(): String = stringResource(labelRes)

/**
 * Biznes turining ikonkasi (dizayn handoff `ic_*.svg` to'plamidan).
 *
 * Nima uchun bu yerda, `BusinessType.emoji` o'rniga: emoji iOS'da Compose bilan
 * chizilmaydi va `?` kvadratcha bo'lib ko'rinadi. `BusinessType` esa **domain** modeli —
 * unga `ImageVector` qo'shib bo'lmaydi (domain Compose'dan qaram bo'lib qolardi), shuning
 * uchun ikonka moslamasi [labelRes] kabi shu presentation faylida turadi. Enumdagi `emoji`
 * maydoni backend/ma'lumot uchun qoladi, lekin ekranlarda ISHLATILMAYDI.
 */
val BusinessType.icon: ImageVector
    get() = when (this) {
        BusinessType.GAME_CLUB -> AppIcons.Gamepad
        BusinessType.CLOTHING -> AppIcons.Cart
        BusinessType.CAFE_RESTAURANT -> AppIcons.Cafe
        BusinessType.EDUCATION_CENTER -> AppIcons.Book
        BusinessType.ENTERTAINMENT -> AppIcons.Camera
        BusinessType.BARBERSHOP -> AppIcons.Tools
        BusinessType.BEAUTY_SALON -> AppIcons.Star
    }
