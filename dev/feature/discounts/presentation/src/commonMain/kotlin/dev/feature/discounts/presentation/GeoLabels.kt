package dev.feature.discounts.presentation

import androidx.compose.runtime.Composable
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_day_fri
import dev.core.uikit.resources.discounts_day_mon
import dev.core.uikit.resources.discounts_day_sat
import dev.core.uikit.resources.discounts_day_sun
import dev.core.uikit.resources.discounts_day_thu
import dev.core.uikit.resources.discounts_day_tue
import dev.core.uikit.resources.discounts_day_wed
import dev.core.uikit.resources.discounts_region_andijon
import dev.core.uikit.resources.discounts_region_buxoro
import dev.core.uikit.resources.discounts_region_fargona
import dev.core.uikit.resources.discounts_region_jizzax
import dev.core.uikit.resources.discounts_region_namangan
import dev.core.uikit.resources.discounts_region_navoiy
import dev.core.uikit.resources.discounts_region_qashqadaryo
import dev.core.uikit.resources.discounts_region_qoraqalpogiston
import dev.core.uikit.resources.discounts_region_samarqand
import dev.core.uikit.resources.discounts_region_sirdaryo
import dev.core.uikit.resources.discounts_region_surxondaryo
import dev.core.uikit.resources.discounts_region_toshkent_shahri
import dev.core.uikit.resources.discounts_region_toshkent_viloyati
import dev.core.uikit.resources.discounts_region_xorazm
import dev.feature.discounts.domain.model.Region
import dev.feature.discounts.domain.model.WeekDay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Hafta kunlari va viloyat nomlarining tarjimasi.
 *
 * Nega bu yerda, model ichida emas: `WeekDay.label` — **domain** qiymati va Compose
 * resurslariga bog'lana olmaydi, viloyat nomi esa umuman **backenddan** keladi (`nameUz`).
 * Ikkalasi ham shu sababdan ilova rus yoki ingliz tilida bo'lganda ham o'zbekcha
 * ("Dushanba", "Toshkent shahri") ko'rinardi. `WeekDay.label` endi faqat log/xato matnlari uchun.
 */

/** Filial ish vaqti jadvalidagi kun nomi. */
@Composable
fun WeekDay.localizedLabel(): String = stringResource(
    when (this) {
        WeekDay.MON -> Res.string.discounts_day_mon
        WeekDay.TUE -> Res.string.discounts_day_tue
        WeekDay.WED -> Res.string.discounts_day_wed
        WeekDay.THU -> Res.string.discounts_day_thu
        WeekDay.FRI -> Res.string.discounts_day_fri
        WeekDay.SAT -> Res.string.discounts_day_sat
        WeekDay.SUN -> Res.string.discounts_day_sun
    },
)

/**
 * Viloyat nomi joriy tilda.
 *
 * Ro'yxat serverdan keladi va u faqat `nameUz` ni kafolatlaydi, shuning uchun tarjima
 * **kalit bo'yicha** izlanadi: avval `id`, keyin o'zbekcha nomdan hosil qilingan ASCII
 * "slug" (`GeoCatalog` id formati — "Farg'ona viloyati" → `FARGONA_VILOYATI`).
 *
 * Ikkovi ham topilmasa serverdagi nom qaytariladi: yangi viloyat qo'shilsa yoki backend
 * id formatini o'zgartirsa ekranda bo'sh joy emas, hech bo'lmasa o'zbekcha nom turadi.
 */
@Composable
fun Region.localizedName(): String {
    val key = RegionNames[id] ?: RegionNames[slugOf(name)]
    return if (key == null) name else stringResource(key)
}

private val RegionNames: Map<String, StringResource> = mapOf(
    "TOSHKENT_SHAHRI" to Res.string.discounts_region_toshkent_shahri,
    "TOSHKENT_VILOYATI" to Res.string.discounts_region_toshkent_viloyati,
    "ANDIJON_VILOYATI" to Res.string.discounts_region_andijon,
    "BUXORO_VILOYATI" to Res.string.discounts_region_buxoro,
    "FARGONA_VILOYATI" to Res.string.discounts_region_fargona,
    "JIZZAX_VILOYATI" to Res.string.discounts_region_jizzax,
    "NAMANGAN_VILOYATI" to Res.string.discounts_region_namangan,
    "NAVOIY_VILOYATI" to Res.string.discounts_region_navoiy,
    "QASHQADARYO_VILOYATI" to Res.string.discounts_region_qashqadaryo,
    "QORAQALPOGISTON_RESPUBLIKASI" to Res.string.discounts_region_qoraqalpogiston,
    "SAMARQAND_VILOYATI" to Res.string.discounts_region_samarqand,
    "SIRDARYO_VILOYATI" to Res.string.discounts_region_sirdaryo,
    "SURXONDARYO_VILOYATI" to Res.string.discounts_region_surxondaryo,
    "XORAZM_VILOYATI" to Res.string.discounts_region_xorazm,
)

/**
 * "Farg'ona viloyati" → "FARGONA_VILOYATI" — `GeoCatalog.slug` bilan bir xil qoida.
 *
 * Apostrof tashlab yuboriladi, chunki o'zbek matnida u uch xil belgi bilan yozilishi mumkin
 * (`'`, `ʻ`, `’`) va backend qaysi birini yuborishiga tayanib bo'lmaydi.
 */
private fun slugOf(name: String): String = buildString {
    for (ch in name) {
        when {
            ch.isLetterOrDigit() && ch.code < 128 -> append(ch.uppercaseChar())
            ch == ' ' || ch == '-' -> append('_')
        }
    }
}
