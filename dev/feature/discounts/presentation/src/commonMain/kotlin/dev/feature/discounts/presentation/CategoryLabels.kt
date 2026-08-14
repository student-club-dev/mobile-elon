package dev.feature.discounts.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_cat_all
import dev.core.uikit.resources.discounts_cat_other
import dev.core.uikit.resources.discounts_cat_kids
import dev.core.uikit.resources.discounts_cat_teen
import dev.core.uikit.resources.discounts_cat_adult
import dev.core.uikit.resources.discounts_cat_vip
import dev.core.uikit.resources.discounts_cat_standard
import dev.core.uikit.resources.discounts_cat_training
import dev.core.uikit.resources.discounts_cat_personal
import dev.core.uikit.resources.discounts_cat_group
import dev.core.uikit.resources.discounts_cat_indoor
import dev.core.uikit.resources.discounts_cat_outdoor
import dev.core.uikit.resources.discounts_cat_mini
import dev.core.uikit.resources.discounts_cat_goalkeeper
import dev.core.uikit.resources.discounts_cat_beach
import dev.core.uikit.resources.discounts_cat_rent
import dev.core.uikit.resources.discounts_cat_free_swim
import dev.core.uikit.resources.discounts_cat_aqua_aerobics
import dev.core.uikit.resources.discounts_cat_gym
import dev.core.uikit.resources.discounts_cat_crossfit
import dev.core.uikit.resources.discounts_cat_yoga
import dev.core.uikit.resources.discounts_cat_boxing
import dev.core.uikit.resources.discounts_cat_kickboxing
import dev.core.uikit.resources.discounts_cat_wrestling
import dev.core.uikit.resources.discounts_cat_judo
import dev.core.uikit.resources.discounts_cat_mma
import dev.core.uikit.resources.discounts_cat_pool
import dev.core.uikit.resources.discounts_cat_russian
import dev.core.uikit.resources.discounts_cat_snooker
import dev.core.uikit.resources.discounts_cat_ps4
import dev.core.uikit.resources.discounts_cat_ps5
import dev.core.uikit.resources.discounts_cat_pro
import dev.core.uikit.resources.discounts_cat_studio
import dev.core.uikit.resources.discounts_cat_room
import dev.core.uikit.resources.discounts_cat_apartment
import dev.core.uikit.resources.discounts_cat_hostel
import dev.core.uikit.resources.discounts_cat_coworking
import dev.core.uikit.resources.discounts_cat_reading_hall
import dev.core.uikit.resources.discounts_cat_book_rent
import dev.core.uikit.resources.discounts_cat_book_binding
import dev.core.uikit.resources.discounts_cat_document_print
import dev.core.uikit.resources.discounts_cat_photo
import dev.core.uikit.resources.discounts_cat_copy
import dev.core.uikit.resources.discounts_cat_banner
import dev.core.uikit.resources.discounts_cat_design
import dev.core.uikit.resources.discounts_cat_english
import dev.core.uikit.resources.discounts_cat_foreign_languages
import dev.core.uikit.resources.discounts_cat_native_lang
import dev.core.uikit.resources.discounts_cat_math
import dev.core.uikit.resources.discounts_cat_math_science
import dev.core.uikit.resources.discounts_cat_physics
import dev.core.uikit.resources.discounts_cat_it
import dev.core.uikit.resources.discounts_cat_it_programming
import dev.core.uikit.resources.discounts_cat_business_marketing
import dev.core.uikit.resources.discounts_cat_ielts_cefr
import dev.core.uikit.resources.discounts_cat_exam_prep
import dev.core.uikit.resources.discounts_cat_university_prep
import dev.core.uikit.resources.discounts_cat_master_class
import dev.core.uikit.resources.discounts_cat_haircut_men
import dev.core.uikit.resources.discounts_cat_beard
import dev.core.uikit.resources.discounts_cat_hair
import dev.core.uikit.resources.discounts_cat_hair_color
import dev.core.uikit.resources.discounts_cat_hair_care
import dev.core.uikit.resources.discounts_cat_styling
import dev.core.uikit.resources.discounts_cat_makeup
import dev.core.uikit.resources.discounts_cat_manicure
import dev.core.uikit.resources.discounts_cat_pedicure
import dev.core.uikit.resources.discounts_cat_eyebrows_lashes
import dev.core.uikit.resources.discounts_cat_epilation
import dev.core.uikit.resources.discounts_cat_cosmetology
import dev.core.uikit.resources.discounts_cat_spa_massage
import dev.core.uikit.resources.discounts_cat_men
import dev.core.uikit.resources.discounts_cat_women
import dev.core.uikit.resources.discounts_cat_outerwear
import dev.core.uikit.resources.discounts_cat_shirts
import dev.core.uikit.resources.discounts_cat_blouses
import dev.core.uikit.resources.discounts_cat_dresses
import dev.core.uikit.resources.discounts_cat_skirts
import dev.core.uikit.resources.discounts_cat_pants
import dev.core.uikit.resources.discounts_cat_suits
import dev.core.uikit.resources.discounts_cat_sportswear
import dev.core.uikit.resources.discounts_cat_shoes
import dev.core.uikit.resources.discounts_cat_bags
import dev.core.uikit.resources.discounts_cat_accessories
import dev.core.uikit.resources.discounts_cat_burger
import dev.core.uikit.resources.discounts_cat_pizza
import dev.core.uikit.resources.discounts_cat_hotdog
import dev.core.uikit.resources.discounts_cat_lavash_shawarma
import dev.core.uikit.resources.discounts_cat_fries
import dev.core.uikit.resources.discounts_cat_combo
import dev.core.uikit.resources.discounts_cat_drinks
import dev.core.uikit.resources.discounts_cat_salad
import dev.core.uikit.resources.discounts_cat_palov
import dev.core.uikit.resources.discounts_cat_shorva
import dev.core.uikit.resources.discounts_cat_lagmon
import dev.core.uikit.resources.discounts_cat_manti_chuchvara
import dev.core.uikit.resources.discounts_cat_kabob
import dev.core.uikit.resources.discounts_cat_meat_somsa
import dev.core.uikit.resources.discounts_cat_potato_somsa
import dev.core.uikit.resources.discounts_cat_greens_somsa
import dev.core.uikit.resources.discounts_cat_tandir_non
import dev.core.uikit.resources.discounts_cat_patir
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * E'lon kategoriyasining tarjimasi.
 *
 * Backend kategoriya nomini **faqat o'zbekcha** beradi (`ListingCategory.nameUz`; ruscha/inglizcha
 * maydonlar yo'q) — shu sabab ingliz yoki rus tilidagi ilovada e'lon formasidagi chiplar
 * o'zbekcha bo'lib turardi ("Barcha seanslar", "Butun menyu"). Bu yerдаgi moslama kalit
 * bo'yicha tarjimani beradi.
 *
 * MUHIM — o'zbek tilida SERVERNING nomi qoladi: u turga moslashgan va aniqroq ("Barcha kortlar",
 * "Barcha stollar"), moslamadagi umumiy "Barchasi" esa uni yo'qotib yuborardi. Boshqa tillarda
 * umumiy tarjima aniq bo'lmagan o'zbekchadan baribir yaxshiroq.
 *
 * Noma'lum kalit — serverdagi nom (moslama to'liq bo'lishi shart emas: kategoriyalar
 * adminkadan qo'shiladi).
 */
@Composable
fun localizedCategoryLabel(key: String, nameUz: String): String {
    if (Locale.current.language.lowercase() == UZBEK_LANGUAGE) return nameUz
    val res = categoryLabelRes(key) ?: return nameUz
    return stringResource(res)
}

/** `applyAppLanguage` yozgan joriy til kodi. */
private const val UZBEK_LANGUAGE = "uz"

private fun categoryLabelRes(key: String): StringResource? = when (key) {
    "ALL" -> Res.string.discounts_cat_all
    "OTHER" -> Res.string.discounts_cat_other
    "KIDS" -> Res.string.discounts_cat_kids
    "TEEN" -> Res.string.discounts_cat_teen
    "ADULT" -> Res.string.discounts_cat_adult
    "VIP" -> Res.string.discounts_cat_vip
    "STANDARD" -> Res.string.discounts_cat_standard
    "TRAINING" -> Res.string.discounts_cat_training
    "PERSONAL" -> Res.string.discounts_cat_personal
    "GROUP" -> Res.string.discounts_cat_group
    "INDOOR" -> Res.string.discounts_cat_indoor
    "OUTDOOR" -> Res.string.discounts_cat_outdoor
    "MINI" -> Res.string.discounts_cat_mini
    "GOALKEEPER" -> Res.string.discounts_cat_goalkeeper
    "BEACH" -> Res.string.discounts_cat_beach
    "RENT" -> Res.string.discounts_cat_rent
    "FREE_SWIM" -> Res.string.discounts_cat_free_swim
    "AQUA_AEROBICS" -> Res.string.discounts_cat_aqua_aerobics
    "GYM" -> Res.string.discounts_cat_gym
    "CROSSFIT" -> Res.string.discounts_cat_crossfit
    "YOGA" -> Res.string.discounts_cat_yoga
    "BOXING" -> Res.string.discounts_cat_boxing
    "KICKBOXING" -> Res.string.discounts_cat_kickboxing
    "WRESTLING" -> Res.string.discounts_cat_wrestling
    "JUDO" -> Res.string.discounts_cat_judo
    "MMA" -> Res.string.discounts_cat_mma
    "POOL" -> Res.string.discounts_cat_pool
    "RUSSIAN" -> Res.string.discounts_cat_russian
    "SNOOKER" -> Res.string.discounts_cat_snooker
    "PS4" -> Res.string.discounts_cat_ps4
    "PS5" -> Res.string.discounts_cat_ps5
    "PRO" -> Res.string.discounts_cat_pro
    "STUDIO" -> Res.string.discounts_cat_studio
    "ROOM" -> Res.string.discounts_cat_room
    "APARTMENT" -> Res.string.discounts_cat_apartment
    "HOSTEL" -> Res.string.discounts_cat_hostel
    "COWORKING" -> Res.string.discounts_cat_coworking
    "READING_HALL" -> Res.string.discounts_cat_reading_hall
    "BOOK_RENT" -> Res.string.discounts_cat_book_rent
    "BOOK_BINDING" -> Res.string.discounts_cat_book_binding
    "DOCUMENT_PRINT" -> Res.string.discounts_cat_document_print
    "PHOTO" -> Res.string.discounts_cat_photo
    "COPY" -> Res.string.discounts_cat_copy
    "BANNER" -> Res.string.discounts_cat_banner
    "DESIGN" -> Res.string.discounts_cat_design
    "ENGLISH" -> Res.string.discounts_cat_english
    "FOREIGN_LANGUAGES" -> Res.string.discounts_cat_foreign_languages
    "NATIVE_LANG" -> Res.string.discounts_cat_native_lang
    "MATH" -> Res.string.discounts_cat_math
    "MATH_SCIENCE" -> Res.string.discounts_cat_math_science
    "PHYSICS" -> Res.string.discounts_cat_physics
    "IT" -> Res.string.discounts_cat_it
    "IT_PROGRAMMING" -> Res.string.discounts_cat_it_programming
    "BUSINESS_MARKETING" -> Res.string.discounts_cat_business_marketing
    "IELTS_CEFR" -> Res.string.discounts_cat_ielts_cefr
    "EXAM_PREP" -> Res.string.discounts_cat_exam_prep
    "UNIVERSITY_PREP" -> Res.string.discounts_cat_university_prep
    "MASTER_CLASS" -> Res.string.discounts_cat_master_class
    "HAIRCUT_MEN" -> Res.string.discounts_cat_haircut_men
    "BEARD" -> Res.string.discounts_cat_beard
    "HAIR" -> Res.string.discounts_cat_hair
    "HAIR_COLOR" -> Res.string.discounts_cat_hair_color
    "HAIR_CARE" -> Res.string.discounts_cat_hair_care
    "STYLING" -> Res.string.discounts_cat_styling
    "MAKEUP" -> Res.string.discounts_cat_makeup
    "MANICURE" -> Res.string.discounts_cat_manicure
    "PEDICURE" -> Res.string.discounts_cat_pedicure
    "EYEBROWS_LASHES" -> Res.string.discounts_cat_eyebrows_lashes
    "EPILATION" -> Res.string.discounts_cat_epilation
    "COSMETOLOGY" -> Res.string.discounts_cat_cosmetology
    "SPA_MASSAGE" -> Res.string.discounts_cat_spa_massage
    "MEN" -> Res.string.discounts_cat_men
    "WOMEN" -> Res.string.discounts_cat_women
    "OUTERWEAR" -> Res.string.discounts_cat_outerwear
    "SHIRTS" -> Res.string.discounts_cat_shirts
    "BLOUSES" -> Res.string.discounts_cat_blouses
    "DRESSES" -> Res.string.discounts_cat_dresses
    "SKIRTS" -> Res.string.discounts_cat_skirts
    "PANTS" -> Res.string.discounts_cat_pants
    "SUITS" -> Res.string.discounts_cat_suits
    "SPORTSWEAR" -> Res.string.discounts_cat_sportswear
    "SHOES" -> Res.string.discounts_cat_shoes
    "BAGS" -> Res.string.discounts_cat_bags
    "ACCESSORIES" -> Res.string.discounts_cat_accessories
    "BURGER" -> Res.string.discounts_cat_burger
    "PIZZA" -> Res.string.discounts_cat_pizza
    "HOTDOG" -> Res.string.discounts_cat_hotdog
    "LAVASH_SHAWARMA" -> Res.string.discounts_cat_lavash_shawarma
    "FRIES" -> Res.string.discounts_cat_fries
    "COMBO" -> Res.string.discounts_cat_combo
    "DRINKS" -> Res.string.discounts_cat_drinks
    "SALAD" -> Res.string.discounts_cat_salad
    "PALOV" -> Res.string.discounts_cat_palov
    "SHORVA" -> Res.string.discounts_cat_shorva
    "LAGMON" -> Res.string.discounts_cat_lagmon
    "MANTI_CHUCHVARA" -> Res.string.discounts_cat_manti_chuchvara
    "KABOB" -> Res.string.discounts_cat_kabob
    "MEAT_SOMSA" -> Res.string.discounts_cat_meat_somsa
    "POTATO_SOMSA" -> Res.string.discounts_cat_potato_somsa
    "GREENS_SOMSA" -> Res.string.discounts_cat_greens_somsa
    "TANDIR_NON" -> Res.string.discounts_cat_tandir_non
    "PATIR" -> Res.string.discounts_cat_patir
    else -> null
}
