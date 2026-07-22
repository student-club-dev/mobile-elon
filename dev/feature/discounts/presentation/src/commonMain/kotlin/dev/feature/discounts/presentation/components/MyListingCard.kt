package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.IconTile
import dev.core.uikit.component.SoftPill
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.common_edit
import dev.core.uikit.resources.discounts_action_pause
import dev.core.uikit.resources.discounts_action_resume
import dev.core.uikit.resources.discounts_branch_extra
import dev.core.uikit.resources.discounts_branch_location
import dev.core.uikit.resources.discounts_price_sum
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.icon
import org.jetbrains.compose.resources.stringResource

/**
 * Biznes egasining e'lon kartasi — muqova, sarlavha, kategoriya chipi, narx, status va
 * pastki harakat tugmalari (tahrirlash / to'xtatish / o'chirish).
 *
 * Aksent rangi biznes TURIDAN keladi (`businessType.accent`), shuning uchun u palitra
 * tokeni emas — ma'lumotning bir qismi.
 */
@Composable
fun MyListingCard(
    listing: Listing,
    palette: AppPalette,
    onEdit: () -> Unit,
    onTogglePaused: () -> Unit,
    onDelete: () -> Unit,
) {
    val accent = Color(listing.businessType.accent)
    val isDiscount = listing.isDiscount
    val cardShape = RoundedCornerShape(22.dp)

    Column(
        Modifier.fillMaxWidth()
            .shadow(10.dp, cardShape, spotColor = accent.copy(alpha = 0.25f))
            .clip(cardShape).background(palette.glass)
            .border(1.dp, palette.border, cardShape),
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            ListingCover(listing, accent, isDiscount, palette)

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    listing.title,
                    style = AppType.body.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                SoftPill(
                    listing.categoryLabel,
                    accent = accent,
                    backgroundAlpha = 0.12f,
                    shape = RoundedCornerShape(7.dp),
                    textStyle = AppType.caption.copy(fontSize = 10.5f.sp, fontWeight = AppType.label.fontWeight),
                    contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 3.dp),
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        stringResource(Res.string.discounts_price_sum, listing.finalPrice.formatSum()),
                        style = AppType.cardTitle.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.ink),
                    )
                    // Chegirmada eski narx — chiziq bilan; oddiy e'londa yo'q.
                    if (isDiscount && listing.originalPrice != listing.finalPrice) {
                        Text(
                            listing.originalPrice.formatSum(),
                            style = AppType.hint.copy(
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
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md).padding(bottom = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val first = listing.branches.firstOrNull()
            if (first != null) {
                val extra = listing.branches.size - 1
                // Bir nechta filial bo'lsa: "Chilonzor filiali — ... +2 filial"
                val extraLabel =
                    if (extra > 0) stringResource(Res.string.discounts_branch_extra, "$extra") else ""
                Text(
                    stringResource(Res.string.discounts_branch_location, first.display()) + extraLabel,
                    style = AppType.caption.copy(fontSize = 10.5f.sp, color = palette.inkFaint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }

            CardAction(AppIcons.Pencil, stringResource(Res.string.common_edit), palette, onEdit)
            if (listing.status == ListingStatus.ACTIVE || listing.status == ListingStatus.PAUSED) {
                val active = listing.status == ListingStatus.ACTIVE
                CardAction(
                    if (active) AppIcons.EyeOff else AppIcons.Eye,
                    stringResource(
                        if (active) Res.string.discounts_action_pause else Res.string.discounts_action_resume,
                    ),
                    palette,
                    onTogglePaused,
                )
            }
            CardAction(AppIcons.Close, stringResource(Res.string.common_delete), palette, onDelete)
        }
    }
}

/** Muqova rasmi (yo'q bo'lsa — tur emoji'si). Chegirmada burchakda foiz badge'i. */
@Composable
private fun ListingCover(
    listing: Listing,
    accent: Color,
    isDiscount: Boolean,
    palette: AppPalette,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(Modifier.size(72.dp)) {
        Box(
            Modifier.fillMaxSize().clip(shape)
                .background(
                    Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.07f))),
                ),
            contentAlignment = Alignment.Center,
        ) {
            val cover = listing.images.firstOrNull()
            if (cover != null) {
                ListingImage(cover, Modifier.fillMaxSize().clip(shape))
            } else {
                Icon(listing.businessType.icon, null, tint = accent, modifier = Modifier.size(30.dp))
            }
        }
        if (isDiscount) {
            Box(
                Modifier.align(Alignment.TopStart).padding(5.dp)
                    .clip(RoundedCornerShape(8.dp)).background(accent)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                // Aksent fon USTIDA — kontent rangi palitradan emas, `onPrimary` dan olinadi.
                Text(
                    listing.discount.badge(),
                    style = AppType.caption.copy(
                        fontSize = 9.5f.sp,
                        fontWeight = AppType.screenTitle.fontWeight,
                        color = palette.onPrimary,
                    ),
                )
            }
        }
    }
}

/** Karta ostidagi mayda harakat tugmasi. */
@Composable
private fun CardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    palette: AppPalette,
    onClick: () -> Unit,
) {
    IconTile(
        icon = icon,
        contentDescription = description,
        tint = palette.inkMuted,
        background = palette.fieldBg,
        size = 32.dp,
        iconSize = 15.dp,
        shape = AppRadius.sm,
        onClick = onClick,
    )
}

/** Status rangi — palitradan, shuning uchun qorong'i rejimda ham o'qiladi. */
private fun ListingStatus.color(palette: AppPalette): Color = when (this) {
    ListingStatus.ACTIVE -> palette.successDeep
    ListingStatus.DRAFT -> palette.inkFaint
    ListingStatus.PENDING_REVIEW, ListingStatus.SCHEDULED -> palette.primary
    ListingStatus.REJECTED, ListingStatus.EXPIRED, ListingStatus.SOLD_OUT -> palette.danger
    ListingStatus.PAUSED, ListingStatus.ARCHIVED -> palette.inkMuted
}
