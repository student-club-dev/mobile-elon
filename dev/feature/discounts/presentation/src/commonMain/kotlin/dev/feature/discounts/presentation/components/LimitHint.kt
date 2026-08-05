package dev.feature.discounts.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_limit_business
import dev.core.uikit.resources.discounts_limit_daily
import dev.core.uikit.resources.discounts_limit_listing
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.stringResource

/**
 * Chegara (`429`) uchun **amaliy maslahat** — server bergan xabar ostida ko'rsatiladi.
 *
 * Nega alohida matn: backend "chegara to'ldi" deb aytadi, lekin foydalanuvchiga endi nima
 * qilishi kerakligini aytmaydi. Chegaralar esa har xil chiqish yo'liga ega
 * (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §4): biznes chegarasida eskisini arxivlash, e'lon
 * chegarasida eski e'lonni to'xtatish, kunlik chegarada esa faqat kutish qoladi.
 *
 * Serverning o'z xabari **almashtirilmaydi** — u yuqorida turadi; bu qator unga qo'shimcha.
 *
 * [code] noma'lum yoki `null` bo'lsa hech narsa chizilmaydi: taxminiy maslahat berish
 * jim turishdan yomonroq bo'lardi.
 */
@Composable
fun LimitHint(
    code: String?,
    palette: AppPalette,
    /** Chegara qaysi ekranда yuz berdi — bir xil `RATE_LIMITED` kodi ikki ma'noni bildiradi. */
    context: LimitContext,
    modifier: Modifier = Modifier,
) {
    val hint = limitHintText(code, context) ?: return

    Text(
        hint,
        style = AppType.caption.copy(color = palette.inkMuted),
        modifier = modifier,
    )
}

/**
 * O'sha maslahat, lekin **matn sifatida** — toast ichiga ikkinchi qator qilib qo'yish uchun
 * ([LimitHint] o'zi Composable chizadi, uni satr o'rniga ishlatib bo'lmaydi).
 */
@Composable
fun limitHintText(code: String?, context: LimitContext): String? {
    val res = when {
        code == null -> null
        code == LISTING_LIMIT_REACHED -> Res.string.discounts_limit_listing
        // `RATE_LIMITED` — umumiy kod: biznes formasida u "5 ta biznes" chegarasi,
        // e'lon formasida esa "kuniga 50 ta yuborish" chegarasi degani.
        code == RATE_LIMITED && context == LimitContext.BUSINESS -> Res.string.discounts_limit_business
        code == RATE_LIMITED && context == LimitContext.LISTING -> Res.string.discounts_limit_daily
        else -> null
    } ?: return null
    return stringResource(res)
}

/** Chegara qaysi oqimda yuz berdi — bir xil kodni to'g'ri o'qish uchun. */
enum class LimitContext { BUSINESS, LISTING }

/** Bir biznesdagi faol e'lonlar chegarasi (`ACTIVE` + `PENDING_REVIEW`). */
private const val LISTING_LIMIT_REACHED = "LISTING_LIMIT_REACHED"

/** Umumiy chegara kodi — ma'nosi kontekstga bog'liq. */
private const val RATE_LIMITED = "RATE_LIMITED"
