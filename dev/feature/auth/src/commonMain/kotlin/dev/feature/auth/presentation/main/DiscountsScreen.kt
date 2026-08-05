package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.DiscountCategory
import dev.core.domain.model.DiscountOffer
import dev.core.domain.model.DiscountTag
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.SoftPill
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_bookmark
import dev.core.uikit.resources.auth_copied
import dev.core.uikit.resources.auth_copy
import dev.core.uikit.resources.auth_discount_tag_promo
import dev.core.uikit.resources.auth_discount_tag_student_id_short
import dev.core.uikit.resources.auth_discounts_category_title
import dev.core.uikit.resources.auth_discounts_offer_count
import dev.core.uikit.resources.auth_discounts_search_placeholder
import dev.core.uikit.resources.auth_discounts_subtitle
import dev.core.uikit.resources.auth_discounts_title
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.cardShadow
import dev.core.uikit.theme.rowShadow
import dev.feature.discounts.presentation.NearbyDiscountsSection
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DiscountsScreen(vm: DiscountsViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.selected == null) {
        DiscountsGrid(state.categories, palette, onOpen = vm::open)
    } else {
        CategoryOffers(
            category = state.selected!!,
            offers = state.offers,
            query = state.query,
            savedIds = state.savedIds,
            palette = palette,
            onBack = vm::close,
            onQuery = vm::onQuery,
            onToggleSaved = { offer, saved -> vm.toggleSaved(offer, saved) },
        )
    }
}

// ---------------------------------------------------------------------------
// 1q — kategoriyalar grid
// ---------------------------------------------------------------------------
@Composable
private fun DiscountsGrid(
    categories: List<DiscountCategory>,
    palette: AppPalette,
    onOpen: (DiscountCategory) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
    ) {
        // Talaba ko'rinishi — faqat chegirmalarni ko'rish. E'lon qo'yish biznesmen shell'ida.
        Column(Modifier.fillMaxWidth()) {
            Text(
                stringResource(Res.string.auth_discounts_title),
                style = AppType.screenTitle.copy(color = palette.ink),
            )
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                stringResource(Res.string.auth_discounts_subtitle),
                style = AppType.link.copy(color = palette.inkMuted),
            )
        }
        Spacer(Modifier.height(AppSpacing.xl))

        // Biznes egalari yuklagan haqiqiy e'lonlar — eng yaqin filiali va masofasi bilan
        // (feature:discounts). E'lon bo'lmasa bo'lim ko'rinmaydi.
        NearbyDiscountsSection()
        Spacer(Modifier.height(AppSpacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            categories.chunked(2).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    rowItems.forEach { CategoryCard(it, palette, Modifier.weight(1f), onOpen) }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun CategoryCard(
    category: DiscountCategory,
    palette: AppPalette,
    modifier: Modifier,
    onOpen: (DiscountCategory) -> Unit,
) {
    // Aksent domendan keladi (har bir kategoriyaning o'z rangi) — palitra tokeni emas.
    val accent = Color(category.accent)
    val shape = AppRadius.md
    Column(
        // Oq karta — chegara emas, soya bilan ajratiladi.
        modifier.rowShadow(shape).clip(shape).background(palette.card)
            .clickable { onOpen(category) }.padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(46.dp).clip(AppRadius.sm).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) { Icon(AppIcons.forCatalog(category.id), null, tint = accent, modifier = Modifier.size(23.dp)) }
        Text(
            category.name,
            style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(Res.string.auth_discounts_offer_count, "${category.offerCount}"),
            style = AppType.caption.copy(color = palette.inkFaint),
        )
    }
}

// ---------------------------------------------------------------------------
// 1w — kategoriya ichidagi takliflar
// ---------------------------------------------------------------------------
@Composable
private fun CategoryOffers(
    category: DiscountCategory,
    offers: List<DiscountOffer>,
    query: String,
    savedIds: Set<String>,
    palette: AppPalette,
    onBack: () -> Unit,
    onQuery: (String) -> Unit,
    onToggleSaved: (DiscountOffer, Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                BackButton(onBack, contentDescription = stringResource(Res.string.common_back), iconSize = 18.dp)
                // Ilgari sarlavha oldida emoji turardi — endi kategoriya ikonkasi.
                Icon(
                    AppIcons.forCatalog(category.id),
                    null,
                    tint = Color(category.accent),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    stringResource(Res.string.auth_discounts_category_title, category.name),
                    style = AppType.cardTitle.copy(color = palette.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(14.dp))
            GlassTextField(
                query,
                onQuery,
                stringResource(Res.string.auth_discounts_search_placeholder),
                leading = AppIcons.Search,
                height = 46.dp,
            )
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            items(offers, key = { it.id }) { offer ->
                OfferCard(offer, saved = savedIds.contains(offer.id), palette = palette, onToggleSaved = onToggleSaved)
            }
        }
    }
}

@Composable
private fun OfferCard(
    offer: DiscountOffer,
    saved: Boolean,
    palette: AppPalette,
    onToggleSaved: (DiscountOffer, Boolean) -> Unit,
) {
    val accent = Color(offer.bannerAccent)
    val clipboard = LocalClipboardManager.current
    var copied by remember(offer.id) { mutableStateOf(false) }
    LaunchedEffect(copied) { if (copied) { delay(1500); copied = false } }
    val shape = AppRadius.card
    Column(
        Modifier.fillMaxWidth().cardShadow(shape).clip(shape).background(palette.card),
    ) {
        // Rangli banner
        Box(
            Modifier.fillMaxWidth().height(64.dp).background(accent.copy(alpha = 0.16f))
                .padding(horizontal = AppSpacing.lg),
            contentAlignment = Alignment.CenterStart,
        ) {
            Icon(AppIcons.forCatalog(offer.categoryId), null, tint = accent, modifier = Modifier.size(30.dp))
            Box(
                Modifier.align(Alignment.CenterEnd).clip(AppRadius.sm).background(accent)
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                // To'ldirilgan aksent USTIDAGI matn — doim oq.
                Text(
                    "−${offer.discountPercent}%",
                    style = AppType.button.copy(color = palette.onPrimary),
                )
            }
        }
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(
                "${offer.merchant} — ${offer.title}",
                style = AppType.body.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val tagText = if (offer.tag == DiscountTag.STUDENT_ID) {
                    stringResource(Res.string.auth_discount_tag_student_id_short)
                } else {
                    stringResource(Res.string.auth_discount_tag_promo)
                }
                SoftPill(tagText, backgroundAlpha = 0.10f)
                val promo = offer.promoCode
                if (promo != null) {
                    Spacer(Modifier.size(6.dp))
                    Row(
                        Modifier.clip(AppRadius.sm)
                            .background(palette.accentBg)
                            .clickable { clipboard.setText(AnnotatedString(promo)); copied = true }
                            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Text(
                            if (copied) stringResource(Res.string.auth_copied) else promo,
                            style = AppType.caption.copy(
                                fontWeight = AppType.button.fontWeight,
                                color = palette.primary,
                            ),
                        )
                        if (!copied) {
                            Icon(
                                AppIcons.FileText,
                                stringResource(Res.string.auth_copy),
                                tint = palette.primary,
                                modifier = Modifier.size(11.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    AppIcons.Bookmark,
                    stringResource(Res.string.auth_bookmark),
                    tint = if (saved) palette.primary else palette.inkFaint,
                    modifier = Modifier.size(20.dp).clickable { onToggleSaved(offer, saved) },
                )
            }
            // Manzil oldidagi belgi emoji emas — `ic_pin` ikonkasi (iOS'da emoji chizilmaydi).
            val meta = listOfNotNull(offer.location, offer.expiry).joinToString(" · ")
            if (meta.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    if (offer.location != null) {
                        Icon(AppIcons.Pin, null, tint = palette.inkFaint, modifier = Modifier.size(12.dp))
                    }
                    Text(meta, style = AppType.caption.copy(color = palette.inkFaint))
                }
            }
        }
    }
}
