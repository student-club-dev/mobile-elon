package dev.feature.discounts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.common.error.AppException
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.BannerTone
import dev.core.uikit.component.EmptyState
import dev.core.uikit.component.ErrorState
import dev.core.uikit.component.ScreenTopBar
import dev.core.uikit.component.StatusBanner
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_retry
import dev.core.uikit.resources.discounts_add_discount
import dev.core.uikit.resources.discounts_add_listing
import dev.core.uikit.resources.discounts_create_cd
import dev.core.uikit.resources.discounts_empty_discount_message
import dev.core.uikit.resources.discounts_empty_discount_title
import dev.core.uikit.resources.discounts_empty_listing_message
import dev.core.uikit.resources.discounts_empty_listing_title
import dev.core.uikit.resources.discounts_listings_count
import dev.core.uikit.resources.discounts_my_listings_title
import dev.core.uikit.resources.discounts_offline
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.presentation.components.MyListingCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Biznes egasining chegirma e'lonlari — status, to'xtatish, tahrirlash, o'chirish. */
@Composable
fun MyListingsScreen(
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    // Biznes shell'ida header "+" yashiriladi — u yerda o'ng-past burchakda aylana FAB bor.
    showHeaderCreate: Boolean = true,
    // Sarlavha (biznes shell'da segment selektor bilan almashtiriladi).
    showHeader: Boolean = true,
    // `true` — faqat chegirma, `false` — faqat oddiy e'lon, `null` — hammasi.
    filterDiscount: Boolean? = null,
    // Faqat shu biznesning e'lonlari (`null` — barcha e'lonlar).
    businessId: String? = null,
    vm: MyListingsViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    val online by vm.online.collectAsStateWithLifecycle()
    val listings = state.listings
        .filter { filterDiscount == null || it.isDiscount == filterDiscount }
        .filter { businessId == null || it.businessId == businessId }

    Column(Modifier.fillMaxSize()) {
        if (showHeader) {
            ScreenTopBar(
                title = stringResource(Res.string.discounts_my_listings_title),
                subtitle = stringResource(Res.string.discounts_listings_count, "${listings.size}"),
                onBack = onBack,
                backContentDescription = stringResource(Res.string.common_back),
                modifier = Modifier.padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
                palette = palette,
                trailing = if (!showHeaderCreate) null else {
                    {
                        BackButton(
                            onClick = onCreate,
                            icon = AppIcons.Plus,
                            contentDescription = stringResource(Res.string.discounts_create_cd),
                            iconSize = 18.dp,
                            palette = palette,
                        )
                    }
                },
            )
            Spacer(Modifier.height(14.dp))
        }

        // Internet yo'q — ustki banner (offline'да e'lonlar local bazadan ko'rsatiladi).
        // Ranglar palitradan: ilgari qattiq `0xFFFEF3C7` fon qorong'i rejimda o'qilmasdi.
        if (!online) {
            StatusBanner(
                text = stringResource(Res.string.discounts_offline),
                tone = BannerTone.WARNING,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = 6.dp),
                palette = palette,
            )
        }

        when {
            // 1) Yuklanmoqda — spinner.
            state.loading -> LoadingView(palette)
            // 2) Xato — sabab + "Qayta urinish".
            state.error != null -> ErrorView(state.error!!, palette, onRetry = vm::retry)
            // 3) Bo'sh — birinchi e'longa chorlov.
            listings.isEmpty() -> ListingsEmptyState(palette, onCreate, filterDiscount)
            // 4) Kontent.
            else -> LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(
                    start = AppSpacing.lg,
                    end = AppSpacing.lg,
                    top = AppSpacing.sm,
                    bottom = 110.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                items(listings, key = { it.id }) { listing ->
                    MyListingCard(
                        listing = listing,
                        palette = palette,
                        onEdit = { onEdit(listing.id) },
                        onTogglePaused = { vm.togglePaused(listing) },
                        onDelete = { vm.delete(listing) },
                    )
                }
            }
        }
    }
}

/** Markazda aylanuvchi yuklash indikatori. */
@Composable
private fun ColumnScope.LoadingView(palette: AppPalette) {
    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
    }
}

/** Xato holati — typed sabab matni va "Qayta urinish" tugmasi. */
@Composable
private fun ColumnScope.ErrorView(error: AppException, palette: AppPalette, onRetry: () -> Unit) {
    ErrorState(
        message = error.userMessage,
        modifier = Modifier.weight(1f),
        actionText = stringResource(Res.string.common_retry),
        onAction = onRetry,
        palette = palette,
    )
}

/**
 * Bo'sh holat. Tab bo'yicha farqli matn — Chegirma yoki E'lon.
 * `null` (birlashgan ro'yxat) → umumiy "e'lon" matni; `true` → chegirma, `false` → oddiy.
 */
@Composable
private fun ListingsEmptyState(palette: AppPalette, onCreate: () -> Unit, discount: Boolean?) {
    val isDiscount = discount == true
    EmptyState(
        icon = if (isDiscount) AppIcons.Tag else AppIcons.FileText,
        title = stringResource(
            if (isDiscount) Res.string.discounts_empty_discount_title else Res.string.discounts_empty_listing_title,
        ),
        message = stringResource(
            if (isDiscount) Res.string.discounts_empty_discount_message else Res.string.discounts_empty_listing_message,
        ),
        modifier = Modifier.fillMaxSize(),
        actionText = stringResource(
            if (isDiscount) Res.string.discounts_add_discount else Res.string.discounts_add_listing,
        ),
        onAction = onCreate,
        palette = palette,
    )
}
