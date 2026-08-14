package dev.feature.discounts.presentation

import androidx.compose.runtime.Composable
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_status_active
import dev.core.uikit.resources.discounts_status_approved
import dev.core.uikit.resources.discounts_status_archived
import dev.core.uikit.resources.discounts_status_blocked
import dev.core.uikit.resources.discounts_status_draft
import dev.core.uikit.resources.discounts_status_expired
import dev.core.uikit.resources.discounts_status_paused
import dev.core.uikit.resources.discounts_status_pending
import dev.core.uikit.resources.discounts_status_rejected
import dev.core.uikit.resources.discounts_status_scheduled
import dev.core.uikit.resources.discounts_status_sold_out
import dev.feature.discounts.domain.model.BusinessStatus
import dev.feature.discounts.domain.model.ListingStatus
import org.jetbrains.compose.resources.stringResource

/**
 * Holat nomlarining tarjimasi.
 *
 * Nega bu yerda, enum ichida emas: `BusinessStatus.label` va `ListingStatus.label` —
 * **domain** qiymatlari va ular Compose resurslariga bog'lana olmaydi. Shu sabab u yerдаgi
 * matn qattiq o'zbekcha edi va ilova ingliz yoki rus tilida bo'lganda ham "Tasdiqlangan",
 * "Faol", "Qoralama" deb ko'rinardi. Enum'dagi `label` endi faqat log/xato matnlari uchun.
 */

@Composable
fun BusinessStatus.localizedLabel(): String = stringResource(
    when (this) {
        BusinessStatus.DRAFT -> Res.string.discounts_status_draft
        BusinessStatus.PENDING_REVIEW -> Res.string.discounts_status_pending
        BusinessStatus.APPROVED -> Res.string.discounts_status_approved
        BusinessStatus.REJECTED -> Res.string.discounts_status_rejected
        BusinessStatus.BLOCKED -> Res.string.discounts_status_blocked
        BusinessStatus.ARCHIVED -> Res.string.discounts_status_archived
    },
)

@Composable
fun ListingStatus.localizedLabel(): String = stringResource(
    when (this) {
        ListingStatus.DRAFT -> Res.string.discounts_status_draft
        ListingStatus.PENDING_REVIEW -> Res.string.discounts_status_pending
        ListingStatus.REJECTED -> Res.string.discounts_status_rejected
        ListingStatus.SCHEDULED -> Res.string.discounts_status_scheduled
        ListingStatus.ACTIVE -> Res.string.discounts_status_active
        ListingStatus.PAUSED -> Res.string.discounts_status_paused
        ListingStatus.EXPIRED -> Res.string.discounts_status_expired
        ListingStatus.SOLD_OUT -> Res.string.discounts_status_sold_out
        ListingStatus.ARCHIVED -> Res.string.discounts_status_archived
    },
)
