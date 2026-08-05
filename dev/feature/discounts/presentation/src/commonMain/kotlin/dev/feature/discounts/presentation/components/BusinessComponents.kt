package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GradientTile
import dev.core.uikit.component.rememberKeyboardDismiss
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.common_edit
import dev.core.uikit.resources.discounts_branch_location
import dev.core.uikit.resources.discounts_listings_count
import dev.core.uikit.resources.discounts_business
import dev.core.uikit.resources.discounts_business_rejected_reason
import dev.core.uikit.resources.discounts_business_status
import dev.core.uikit.resources.discounts_business_submit
import dev.core.uikit.resources.discounts_business_submit_hint
import dev.core.uikit.resources.discounts_business_submitting
import dev.core.uikit.resources.discounts_location_hint
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.cardShadow
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessStatus
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.presentation.icon
import dev.feature.discounts.presentation.localizedLabel
import org.jetbrains.compose.resources.stringResource
import dev.core.uikit.theme.rowShadow

/** Biznes turi noma'lum bo'lganda ko'rsatiladigan zaxira ikonka. */
private val BusinessFallbackIcon = AppIcons.Building

/** Handoff: kartadagi tahrirlash/o'chirish tugmalari 40dp, ikonkasi 18dp. */
private val CardActionSize = 40.dp
private val CardActionIconSize = 18.dp

/**
 * Kategoriya aksenti — palitradagi kategoriya tokenlaridan.
 *
 * Serverdan `accentColor` (HEX) ham keladi, lekin u eski to'plamdan qolgan va yangi dizayn
 * tiliga to'g'ri kelmaydi. Ekranlarda faqat palitra ishlatiladi, shuning uchun tur → token
 * moslamasi shu yerda turadi. Turlar ro'yxati serverda o'sib boradi — moslamada yo'q tur
 * neytral `accentGame` tokenini oladi (ro'yxat to'liq bo'lishi shart emas).
 */
private fun BusinessType.accentColor(palette: AppPalette): Color = when (key) {
    "PLAYSTATION", "CYBER_CLUB", "BILLIARDS", "BOWLING" -> palette.accentGame
    "CLOTHING" -> palette.accentClothing
    "NATIONAL_FOOD", "FAST_FOOD", "SOMSA" -> palette.accentFood
    "EDUCATION_CENTER", "TUTOR", "LIBRARY" -> palette.accentStudy
    "CINEMA", "KARAOKE" -> palette.accentCinema
    "BARBERSHOP" -> palette.accentBarber
    "BEAUTY_SALON" -> palette.accentBeauty
    else -> palette.accentGame
}

/**
 * "Bizneslarim" ro'yxatidagi karta — nom, tur, manzil va harakat tugmalari.
 *
 * Moderatsiya holati kartaning **pastki** qismida ko'rsatiladi va faqat kerak bo'lganda:
 * `APPROVED` biznes uchun hech narsa chiqmaydi (bu odatiy holat, uni takrorlash shovqin),
 * `DRAFT`/`REJECTED` da esa "Tekshiruvga yuborish" tugmasi va rad etish sababi turadi.
 */
@Composable
fun BusinessCard(
    biz: Business,
    palette: AppPalette,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    /** `null` — moderatsiya bloki umuman ko'rsatilmaydi (masalan tanlash rejimida). */
    onSubmit: (() -> Unit)? = null,
    /** Shu karta hozir yuborilyaptimi — tugma kutish holatiga o'tadi. */
    submitting: Boolean = false,
) {
    val accent = biz.businessType?.accentColor(palette) ?: palette.primary
    Column(
        Modifier.fillMaxWidth()
            .cardShadow()
            .clip(AppRadius.card).background(palette.card)
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Handoff'da nishon kategoriya rangining ochiq gradienti — ikonka esa to'liq aksentda.
            GradientTile(
                biz.businessType?.icon ?: BusinessFallbackIcon,
                gradient = listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.38f)),
                tint = accent,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    biz.name,
                    style = AppType.cardTitle.copy(color = palette.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    biz.businessType?.localizedLabel() ?: stringResource(Res.string.discounts_business),
                    style = AppType.secondary.copy(fontWeight = AppType.label.fontWeight, color = accent),
                )
                // Manzil faqat filiallar yuklangan bo'lsa (biznes ochilganda) ko'rsatiladi. Ro'yxatда
                // filiallar ataylab so'ralmaydi (har biznes uchun alohida so'rov ketmasin), shuning
                // uchun ro'yxatда bu qator umuman chiqmaydi — e'lonlar soni ko'rsatilmaydi.
                biz.primaryBranch?.address?.takeIf { it.isNotBlank() }?.let { address ->
                    Text(
                        stringResource(Res.string.discounts_branch_location, address),
                        style = AppType.caption.copy(color = palette.inkFaint),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                IconTile(
                    AppIcons.Pencil,
                    contentDescription = stringResource(Res.string.common_edit),
                    tint = palette.primary,
                    background = palette.accentBg,
                    size = CardActionSize,
                    iconSize = CardActionIconSize,
                    shape = AppRadius.sm,
                    onClick = onEdit,
                )
                IconTile(
                    AppIcons.Close,
                    contentDescription = stringResource(Res.string.common_delete),
                    tint = palette.danger,
                    background = palette.dangerBg,
                    size = CardActionSize,
                    iconSize = CardActionIconSize,
                    shape = AppRadius.sm,
                    onClick = onDelete,
                )
            }
        }

        if (onSubmit != null) BusinessModerationRow(biz, palette, onSubmit, submitting)
    }
}

/**
 * Kartaning moderatsiya qatori — holat, rad etish sababi va "Tekshiruvga yuborish".
 *
 * Tugma faqat [dev.feature.discounts.domain.model.BusinessStatus.canSubmit] holatlarda
 * chiqadi: backend boshqasidan `409 INVALID_STATUS_TRANSITION` qaytaradi, ya'ni tugmani
 * ko'rsatish foydalanuvchini albatta xatoga olib borardi.
 *
 * `APPROVED` da bu qator butunlay chiqmaydi — hammasi joyida ekani alohida e'lon qilinmaydi.
 */
@Composable
private fun BusinessModerationRow(
    biz: Business,
    palette: AppPalette,
    onSubmit: () -> Unit,
    submitting: Boolean,
) {
    val status = biz.status ?: return
    if (status == BusinessStatus.APPROVED) return

    Column(
        Modifier.fillMaxWidth().padding(
            start = AppSpacing.lg,
            end = AppSpacing.lg,
            bottom = AppSpacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            stringResource(Res.string.discounts_business_status, status.label),
            style = AppType.caption.copy(color = status.tone(palette)),
        )
        biz.rejectionReason?.let { reason ->
            Text(
                stringResource(Res.string.discounts_business_rejected_reason, reason),
                style = AppType.caption.copy(color = palette.danger),
            )
        }
        if (status.canSubmit) {
            Text(
                stringResource(
                    if (submitting) {
                        Res.string.discounts_business_submitting
                    } else {
                        Res.string.discounts_business_submit
                    },
                ),
                style = AppType.label.copy(color = if (submitting) palette.inkMuted else palette.primary),
                modifier = Modifier
                    .clip(AppRadius.sm)
                    .background(palette.accentBg)
                    // Yuborilayotganda bosish o'chiriladi — takroriy so'rov ikkinchi 409 beradi.
                    .clickable(enabled = !submitting, onClick = onSubmit)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            )
            Text(
                stringResource(Res.string.discounts_business_submit_hint),
                style = AppType.caption.copy(color = palette.inkFaint),
            )
        }
    }
}

/** Holat rangi — palitradan, shuning uchun qorong'i rejimda ham o'qiladi. */
private fun BusinessStatus.tone(palette: AppPalette): Color = when (this) {
    BusinessStatus.APPROVED -> palette.success
    BusinessStatus.PENDING_REVIEW -> palette.primary
    BusinessStatus.REJECTED, BusinessStatus.BLOCKED -> palette.danger
    BusinessStatus.DRAFT, BusinessStatus.ARCHIVED -> palette.inkMuted
}

/** Biznes turi chipi — ikonka + nom, tanlanганда brend fon. */
@Composable
fun BusinessTypeChip(
    type: BusinessTypeInfo,
    active: Boolean,
    onClick: () -> Unit,
    palette: AppPalette,
) {
    // Tur tanlanganda klaviatura yopiladi — nom yozib turib tanlansa u to'sib qolardi.
    val dismissKeyboard = rememberKeyboardDismiss()
    val shape = RoundedCornerShape(13.dp)
    Row(
        Modifier.clip(shape)
            .background(if (active) palette.primary else palette.card)
            .clickable { dismissKeyboard(); onClick() }
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            type.type.icon,
            null,
            // Brend fon USTIDA — kontent rangi `onPrimary` dan olinadi.
            tint = if (active) palette.onPrimary else palette.inkMuted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            // `nameUz` backenddan keladi va faqat o'zbekcha — tarjima uchun enum'dan olamiz.
            type.localizedLabel(),
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
        Modifier.fillMaxWidth().rowShadow(AppRadius.lg).clip(AppRadius.lg).background(palette.card)
            .clickable(onClick = onPick).padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(AppIcons.Pin, null, tint = palette.primary, modifier = Modifier.size(18.dp))
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
    Text(text, style = AppType.fieldLabel.copy(fontSize = 12.5f.sp, color = palette.inkMuted))
}
