package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.common_edit
import dev.core.uikit.resources.discounts_branch_location
import dev.core.uikit.resources.discounts_business
import dev.core.uikit.resources.discounts_location_hint
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.presentation.localizedLabel
import org.jetbrains.compose.resources.stringResource

/** Biznes turi topilmaganda ko'rsatiladigan zaxira belgisi (matn emas — ikonka o'rnida). */
private const val BUSINESS_FALLBACK_EMOJI = "🏢"

/** "Bizneslarim" ro'yxatidagi karta — nom, tur, manzil va ikkita harakat tugmasi. */
@Composable
fun BusinessCard(
    biz: Business,
    palette: AppPalette,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    // Aksent biznes TURIDAN keladi — bu ma'lumot, palitra tokeni emas.
    val accent = biz.businessType?.let { Color(it.accent) } ?: palette.primary
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth()
            .shadow(10.dp, shape, spotColor = accent.copy(alpha = 0.22f))
            .clip(shape).background(palette.glass)
            .border(1.dp, palette.border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(biz.businessType?.emoji ?: BUSINESS_FALLBACK_EMOJI, style = TextStyle(fontSize = 24.sp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                biz.name,
                style = AppType.cardTitle.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                biz.businessType?.localizedLabel() ?: stringResource(Res.string.discounts_business),
                style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = accent),
            )
            biz.primaryBranch?.address?.takeIf { it.isNotBlank() }?.let {
                Text(
                    stringResource(Res.string.discounts_branch_location, it),
                    style = AppType.caption.copy(fontSize = 11.sp, color = palette.inkFaint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconTile(
            AppIcons.Pencil,
            contentDescription = stringResource(Res.string.common_edit),
            tint = palette.primary,
            background = palette.primary.copy(alpha = 0.12f),
            size = 34.dp,
            iconSize = 16.dp,
            shape = AppRadius.sm,
            onClick = onEdit,
        )
        IconTile(
            AppIcons.Close,
            contentDescription = stringResource(Res.string.common_delete),
            tint = palette.danger,
            background = palette.dangerBg,
            size = 34.dp,
            iconSize = 16.dp,
            shape = AppRadius.sm,
            onClick = onDelete,
        )
    }
}

/** Biznes turi chipi — emoji + nom, tanlanганда brend fon. */
@Composable
fun BusinessTypeChip(
    type: BusinessTypeInfo,
    active: Boolean,
    onClick: () -> Unit,
    palette: AppPalette,
) {
    val shape = RoundedCornerShape(13.dp)
    Row(
        Modifier.clip(shape)
            .background(if (active) palette.primary else palette.glass)
            .then(if (active) Modifier else Modifier.border(1.dp, palette.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(type.emoji, style = TextStyle(fontSize = 15.sp))
        Text(
            // `nameUz` backenddan keladi va faqat o'zbekcha — tarjima uchun enum'dan olamiz.
            type.type.localizedLabel(),
            style = AppType.link.copy(
                fontWeight = AppType.label.fontWeight,
                // Brend fon USTIDA — kontent rangi `onPrimary` dan olinadi.
                color = if (active) palette.onPrimary else palette.inkMuted,
            ),
            maxLines = 1,
        )
    }
}

/** Joylashuv kartasi — bosilganda xarita ochiladi. */
@Composable
fun LocationCard(address: String?, palette: AppPalette, onPick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(AppRadius.lg).background(palette.glass)
            .border(1.dp, palette.border, AppRadius.lg)
            .clickable(onClick = onPick).padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("📍", style = TextStyle(fontSize = 18.sp))
        Text(
            address?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.discounts_location_hint),
            style = AppType.label.copy(
                fontWeight = AppType.bodyStrong.fontWeight,
                color = if (address != null) palette.ink else palette.inkFaint,
            ),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(AppIcons.ChevronRight, null, tint = palette.inkFaint, modifier = Modifier.size(18.dp))
    }
}

/** Maydon ustidagi yorliq — forma bo'ylab bir xil 12.5sp o'lcham. */
@Composable
fun FormFieldLabel(text: String, palette: AppPalette) {
    Text(text, style = AppType.fieldLabel.copy(fontSize = 12.5f.sp, color = palette.label))
}
