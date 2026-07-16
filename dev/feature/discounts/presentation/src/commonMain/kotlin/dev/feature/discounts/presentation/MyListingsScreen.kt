package dev.feature.discounts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.components.IconSquareButton
import dev.feature.discounts.presentation.components.ListingImage
import dev.feature.discounts.presentation.components.StatusPill
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
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                if (onBack != null) IconSquareButton(onBack, AppIcons.ArrowLeft, palette)
                Column(Modifier.weight(1f)) {
                    Text(
                        "Mening chegirmalarim",
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Black, color = palette.ink),
                    )
                    Text(
                        "${listings.size} ta e'lon",
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint),
                    )
                }
                if (showHeaderCreate) IconSquareButton(onCreate, AppIcons.Plus, palette)
            }
            Spacer(Modifier.height(14.dp))
        }

        // Internet yo'q — ustki banner (offline'да Firestore cache'дан ko'rsatiladi).
        if (!online) OfflineBanner(palette)

        when {
            // 1) Yuklanmoqda — spinner.
            state.loading -> LoadingView(palette)
            // 2) Xato — sabab + "Qayta urinish".
            state.error != null -> ErrorView(state.error!!, palette, onRetry = vm::retry)
            // 3) Bo'sh — birinchi e'longa chorlov.
            listings.isEmpty() -> EmptyState(palette, onCreate, filterDiscount)
            // 4) Kontent.
            else -> LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
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
        androidx.compose.material3.CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
    }
}

/** Xato holati — typed sabab matni va "Qayta urinish" tugmasi. */
@Composable
private fun ColumnScope.ErrorView(error: dev.core.common.error.AppException, palette: AppPalette, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().weight(1f).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(palette.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Close, null, tint = palette.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            error.userMessage,
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton("Qayta urinish", onRetry)
    }
}

/** "Internet yo'q" ustki banner. */
@Composable
private fun OfflineBanner(palette: AppPalette) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(AppIcons.Bell, null, tint = Color(0xFFB45309), modifier = Modifier.size(15.dp))
        Text(
            "Internet aloqasi yo'q — oflayn rejim",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB45309)),
        )
    }
}

@Composable
private fun EmptyState(palette: AppPalette, onCreate: () -> Unit, discount: Boolean?) {
    // Tab bo'yicha farqli matn — Chegirma yoki E'lon.
    // `null` (birlashgan ro'yxat) → umumiy "e'lon" matni; `true` → chegirma, `false` → oddiy.
    val isDiscount = discount == true
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Gradient doira ichida ikonka.
        Box(
            Modifier.size(84.dp)
                .shadow(18.dp, RoundedCornerShape(26.dp), spotColor = palette.primary.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(26.dp)).background(palette.primaryBrush),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isDiscount) AppIcons.Tag else AppIcons.FileText,
                null,
                tint = Color.White,
                modifier = Modifier.size(38.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            if (isDiscount) "Hali chegirma yo'q" else "Hali e'lon yo'q",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black, color = palette.ink),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (isDiscount)
                "Birinchi chegirmangizni joylang — talabalar uni Chegirmalar bo'limida ko'radi."
            else
                "Birinchi e'loningizni joylang — mahsulot yoki xizmatingizni talabalarga ko'rsating.",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = palette.inkMuted, lineHeight = 18.sp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(if (isDiscount) "Chegirma qo'shish" else "E'lon qo'shish", onCreate)
    }
}

@Composable
private fun MyListingCard(
    listing: Listing,
    palette: AppPalette,
    onEdit: () -> Unit,
    onTogglePaused: () -> Unit,
    onDelete: () -> Unit,
) {
    val accent = Color(listing.businessType.accent)
    val isDiscount = listing.isDiscount

    Column(
        Modifier.fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = accent.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(22.dp)).background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(22.dp)),
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Muqova rasmi (yo'q bo'lsa — tur emoji'si). Chegirmada burchakda badge.
            Box(Modifier.size(72.dp)) {
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.07f))),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    val cover = listing.images.firstOrNull()
                    if (cover != null) {
                        ListingImage(cover, Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))
                    } else {
                        Text(listing.businessType.emoji, style = TextStyle(fontSize = 28.sp))
                    }
                }
                if (isDiscount) {
                    Box(
                        Modifier.align(Alignment.TopStart).padding(5.dp)
                            .clip(RoundedCornerShape(8.dp)).background(accent)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            listing.discount.badge(),
                            style = TextStyle(fontFamily = AppFontFamily, fontSize = 9.5f.sp, fontWeight = FontWeight.Black, color = Color.White),
                        )
                    }
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    listing.title,
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Black, color = palette.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Kategoriya chipi.
                Box(
                    Modifier.clip(RoundedCornerShape(7.dp)).background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        listing.categoryLabel,
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, fontWeight = FontWeight.Bold, color = accent),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        "${listing.finalPrice.formatSum()} so'm",
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Black, color = palette.ink),
                    )
                    // Chegirmada eski narx — chiziq bilan; oddiy e'londa yo'q.
                    if (isDiscount && listing.originalPrice != listing.finalPrice) {
                        Text(
                            listing.originalPrice.formatSum(),
                            style = TextStyle(
                                fontFamily = AppFontFamily,
                                fontSize = 11.5f.sp,
                                color = palette.inkFaint,
                                textDecoration = TextDecoration.LineThrough,
                            ),
                            modifier = Modifier.padding(bottom = 1.dp),
                        )
                    }
                }
            }

            StatusPill(listing.status.label, listing.status.color(palette))
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val first = listing.branches.firstOrNull()
            if (first != null) {
                val extra = listing.branches.size - 1
                Text(
                    // Bir nechta filial bo'lsa: "📍 Chilonzor filiali — ... +2 filial"
                    "📍 ${first.display()}" + if (extra > 0) "  +$extra filial" else "",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, color = palette.inkFaint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }

            CardAction(AppIcons.Pencil, "Tahrirlash", palette, onEdit)
            if (listing.status == ListingStatus.ACTIVE || listing.status == ListingStatus.PAUSED) {
                CardAction(
                    if (listing.status == ListingStatus.ACTIVE) AppIcons.EyeOff else AppIcons.Eye,
                    if (listing.status == ListingStatus.ACTIVE) "To'xtatish" else "Yoqish",
                    palette,
                    onTogglePaused,
                )
            }
            CardAction(AppIcons.Close, "O'chirish", palette, onDelete)
        }
    }
}

@Composable
private fun CardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    palette: AppPalette,
    onClick: () -> Unit,
) {
    Box(
        Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(palette.fieldBg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, description, tint = palette.inkMuted, modifier = Modifier.size(15.dp)) }
}

private fun ListingStatus.color(palette: AppPalette): Color = when (this) {
    ListingStatus.ACTIVE -> palette.successDeep
    ListingStatus.DRAFT -> palette.inkFaint
    ListingStatus.PENDING_REVIEW, ListingStatus.SCHEDULED -> palette.primary
    ListingStatus.REJECTED, ListingStatus.EXPIRED, ListingStatus.SOLD_OUT -> Color(0xFFEF4444)
    ListingStatus.PAUSED, ListingStatus.ARCHIVED -> palette.inkMuted
}
