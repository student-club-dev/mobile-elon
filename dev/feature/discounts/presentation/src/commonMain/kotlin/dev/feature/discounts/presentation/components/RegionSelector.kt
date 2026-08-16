package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppBottomSheet
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BottomSheetOption
import dev.core.uikit.component.rememberKeyboardDismiss
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_search
import dev.core.uikit.resources.discounts_search_empty
import dev.core.uikit.resources.discounts_region_select
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.rowShadow
import dev.feature.discounts.domain.model.Region
import dev.feature.discounts.presentation.localizedName
import org.jetbrains.compose.resources.stringResource

/**
 * Tanlov maydoni va viloyat sheet'i (`design_handoff_studentclub_elonuz`, "Biznes qo'shish").
 *
 * Ilgari viloyat ochilib-yopiladigan inline ro'yxat edi: u ochilganda formaning qolgani pastga
 * suriladi va 14 ta viloyat ekranga sig'may qolardi. Handoff naqshi — bosiladigan oq qator,
 * ro'yxat esa modal Bottom Sheet'da.
 */

/**
 * Bosiladigan tanlov qatori — chapda ikonka, o'rtada qiymat, o'ngda strelka.
 *
 * Handoff: oq karta, radius 18, ichki padding 15/16, yumshoq soya. Qiymat tanlanmaganda
 * matn ochroq va yengilroq bo'ladi (placeholder holati).
 */
@Composable
fun SelectorField(
    icon: ImageVector,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    palette: AppPalette,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector = AppIcons.ChevronRight,
) {
    // Sheet ochilganda klaviatura yopiladi — aks holda u variantlarni to'sib qoladi.
    val dismissKeyboard = rememberKeyboardDismiss()
    Row(
        modifier.fillMaxWidth()
            .rowShadow(AppRadius.lg)
            .clip(AppRadius.lg)
            .background(palette.card)
            .clickable { dismissKeyboard(); onClick() }
            .padding(horizontal = AppSpacing.lg, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(AppSize.iconMd))
        Text(
            value ?: placeholder,
            style = if (value != null) {
                AppType.bodyStrong.copy(color = palette.ink)
            } else {
                AppType.body.copy(color = palette.inkFaint)
            },
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(trailingIcon, null, tint = palette.inkMuted, modifier = Modifier.size(16.dp))
    }
}

/**
 * Viloyat tanlash sheet'i.
 *
 * Viloyat xaritadan joy tanlanganda teskari geokodlashdan avtomatik to'ladi, lekin bu har
 * doim ham ishlamaydi: geokoder viloyat nomini id'ga bog'lay olmasa `null` qaytadi va biznes
 * viloyat bo'yicha filtrga tushmay qolardi. Shuning uchun bu maydon ko'rinadigan va tuzatsa
 * bo'ladigan qilindi.
 *
 * [regions] — ViewModel bergan ro'yxat (`GET /v1/regions`, xato bo'lsa klient katalogi):
 * filial id'lari server ro'yxatiga mos bo'lishi shart.
 */
@Composable
fun RegionSheet(
    visible: Boolean,
    regions: List<Region>,
    regionId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    palette: AppPalette,
) {
    // Qidiruv holati sheet ichida yashaydi va yopilganда tozalanadi — keyingi safar
    // ro'yxat to'liq ochiladi, oldingi filtr bilan emas.
    var query by remember { mutableStateOf("") }
    // Ro'yxat TARJIMA QILINGAN nom bilan chiziladi va filtr ham shu nom bo'yicha ishlaydi:
    // ilova ruscha bo'lsa foydalanuvchi "Ташкент" deb yozadi, serverdagi "Toshkent" deb emas.
    // Zaxira sifatida o'zbekcha nom ham tekshiriladi — lotin harflar bilan yozgan ham topsin.
    val localized = regions.map { region -> region to region.localizedName() }
    val visibleRegions = localized.filterByName(query) { (region, label) -> "$label ${region.name}" }

    AppBottomSheet(
        visible = visible,
        onDismiss = { query = ""; onDismiss() },
        title = stringResource(Res.string.discounts_region_select),
        palette = palette,
        searchQuery = query,
        onSearchQueryChange = { query = it },
        searchPlaceholder = stringResource(Res.string.common_search),
    ) {
        visibleRegions.forEach { (region, label) ->
            BottomSheetOption(
                label = label,
                selected = region.id == regionId,
                onClick = { query = ""; onSelect(region.id) },
                palette = palette,
            )
        }
        if (visibleRegions.isEmpty()) SheetEmptyHint(palette)
    }
}

// Tuman tanlash sheet'i BOR EDI — olib tashlandi. Biznes formasi endi faqat viloyatni
// so'raydi: `LocationDto.districtId` backendда majburiy bo'lsa ham, ilovada hech qayerda
// ishlatilmaydi (e'lon filtrlari viloyat darajasida) va uni qiymatini
// `AddBusinessViewModel.fillDistrict` avtomatik qo'yadi.

/**
 * Ro'yxatni nomi bo'yicha filtrlaydi — registrga sezgir emas va so'z ichidan ham topadi.
 *
 * API'da qidiruv yo'q (viloyat/tuman ro'yxati to'liq keladi), shuning uchun filtr mobil
 * tomonda.
 */
private inline fun <T> List<T>.filterByName(query: String, name: (T) -> String): List<T> {
    val q = query.trim()
    if (q.isEmpty()) return this
    return filter { name(it).contains(q, ignoreCase = true) }
}

/** Qidiruv hech narsa topmaganda — bo'sh oyna o'rniga tushuntirish. */
@Composable
internal fun SheetEmptyHint(palette: AppPalette) {
    Text(
        stringResource(Res.string.discounts_search_empty),
        style = AppType.body.copy(color = palette.inkFaint),
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg),
    )
}

