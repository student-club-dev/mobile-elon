package dev.feature.auth.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import dev.core.domain.model.DiscountCategory
import dev.core.domain.model.DiscountOffer
import dev.core.domain.model.DiscountTag
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Job
import dev.core.domain.model.Student
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.MonogramTile
import dev.core.uikit.component.SoftPill
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_bookmark
import dev.core.uikit.resources.auth_discount_tag_promo
import dev.core.uikit.resources.auth_discount_tag_student_id
import dev.core.uikit.resources.auth_discounts_title
import dev.core.uikit.resources.auth_friend_add_short
import dev.core.uikit.resources.auth_friend_pending
import dev.core.uikit.resources.auth_friend_single
import dev.core.uikit.resources.auth_home_job_meta
import dev.core.uikit.resources.auth_home_jobs_subtitle
import dev.core.uikit.resources.auth_home_offer_title
import dev.core.uikit.resources.auth_home_students_action
import dev.core.uikit.resources.auth_home_students_subtitle
import dev.core.uikit.resources.auth_jobs_title
import dev.core.uikit.resources.auth_more
import dev.core.uikit.resources.auth_see_all
import dev.core.uikit.resources.auth_students_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.stringResource

// ---------------------------------------------------------------------------
// Umumiy bo'lim sarlavhasi
// ---------------------------------------------------------------------------

/** "Chegirmalar … Barchasi >" ko'rinishidagi bo'lim sarlavhasi. */
@Composable
internal fun SectionHeader(
    title: String,
    palette: AppPalette,
    action: String? = null,
    subtitle: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppType.screenTitle.copy(fontSize = 17.sp, letterSpacing = 0.sp, color = palette.ink))
            if (subtitle != null) {
                Text(subtitle, style = AppType.hint.copy(color = palette.inkFaint))
            }
        }
        if (action != null) {
            Row(
                Modifier.then(
                    if (onAction != null) {
                        Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onAction)
                            .padding(horizontal = AppSpacing.xs, vertical = 2.dp)
                    } else {
                        Modifier
                    },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(action, style = AppType.fieldLabel.copy(color = palette.primary))
                Icon(AppIcons.ChevronRight, null, tint = palette.primary, modifier = Modifier.size(15.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Chegirmalar
// ---------------------------------------------------------------------------

@Composable
internal fun DiscountsSection(
    categories: List<DiscountCategory>,
    featured: DiscountOffer?,
    palette: AppPalette,
    onSeeAll: () -> Unit,
) {
    SectionHeader(
        stringResource(Res.string.auth_discounts_title),
        palette = palette,
        action = stringResource(Res.string.auth_see_all),
        onAction = onSeeAll,
    )
    Spacer(Modifier.height(AppSpacing.md))
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        categories.take(4).forEach { CategoryChip(it, palette, onSeeAll) }
        SoftPill(
            text = stringResource(Res.string.auth_more),
            accent = palette.primary,
            backgroundAlpha = 0.10f,
            shape = AppRadius.pill,
            textStyle = AppType.fieldLabel,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = AppSpacing.sm),
            onClick = onSeeAll,
        )
    }
    if (featured != null) {
        Spacer(Modifier.height(14.dp))
        FeaturedOfferCard(featured, palette, onSeeAll)
    }
}

@Composable
private fun CategoryChip(category: DiscountCategory, palette: AppPalette, onClick: () -> Unit) {
    val shape = AppRadius.pill
    Row(
        Modifier.clip(shape).background(palette.glass).border(1.dp, palette.border, shape)
            .clickable(onClick = onClick).padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            AppIcons.forCatalog(category.id),
            null,
            tint = Color(category.accent),
            modifier = Modifier.size(15.dp),
        )
        Text(category.name, style = AppType.fieldLabel.copy(color = palette.ink))
    }
}

@Composable
private fun FeaturedOfferCard(offer: DiscountOffer, palette: AppPalette, onClick: () -> Unit) {
    // Aksent domendan keladi (har bir taklifning o'z banneri) — palitra tokeni emas.
    val accent = Color(offer.bannerAccent)
    val shape = AppRadius.card
    Row(
        Modifier.fillMaxWidth().clip(shape).background(palette.glassStrong)
            .border(1.dp, palette.border, shape).clickable(onClick = onClick).padding(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(52.dp).clip(AppRadius.lg).background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Icon(AppIcons.forCatalog(offer.categoryId), null, tint = accent, modifier = Modifier.size(26.dp)) }
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(Res.string.auth_home_offer_title, offer.merchant, "${offer.discountPercent}%"),
                style = AppType.label.copy(fontSize = 13.5f.sp, fontWeight = AppType.button.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            val tagText = if (offer.tag == DiscountTag.STUDENT_ID) {
                stringResource(Res.string.auth_discount_tag_student_id)
            } else {
                stringResource(Res.string.auth_discount_tag_promo)
            }
            val meta = listOfNotNull(tagText, offer.expiry).joinToString(" · ")
            Text(meta, style = AppType.caption.copy(color = palette.inkFaint))
        }
        Box(
            Modifier.clip(AppRadius.sm).background(accent).padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            // To'ldirilgan aksent USTIDAGI matn — palitradan emas, doim oq.
            Text("−${offer.discountPercent}%", style = AppType.screenTitle.copy(fontSize = 13.sp, letterSpacing = 0.sp, color = palette.onPrimary))
        }
    }
}

// ---------------------------------------------------------------------------
// Ishlar
// ---------------------------------------------------------------------------

@Composable
internal fun JobsSection(jobs: List<Job>, palette: AppPalette, onSeeAll: () -> Unit, onBookmark: (Job) -> Unit) {
    SectionHeader(
        stringResource(Res.string.auth_jobs_title),
        palette = palette,
        action = stringResource(Res.string.auth_see_all),
        subtitle = stringResource(Res.string.auth_home_jobs_subtitle),
        onAction = onSeeAll,
    )
    Spacer(Modifier.height(AppSpacing.md))
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        jobs.take(3).forEach { JobRow(it, palette, onBookmark) }
    }
}

@Composable
private fun JobRow(job: Job, palette: AppPalette, onBookmark: (Job) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(palette.glass)
            .border(1.dp, palette.border, shape).padding(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        MonogramTile(job.companyMonogram, size = 42.dp, shape = AppRadius.md, fontSize = 16.sp, backgroundAlpha = 0.12f)
        Column(Modifier.weight(1f)) {
            Text(
                job.title,
                style = AppType.label.copy(fontSize = 13.5f.sp, fontWeight = AppType.button.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(Res.string.auth_home_job_meta, job.company, job.location, job.category),
                style = AppType.caption.copy(color = palette.inkFaint),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(AppSpacing.xs))
            Text(job.salary, style = AppType.link.copy(fontWeight = AppType.label.fontWeight, color = palette.successDeep))
        }
        Icon(
            AppIcons.Bookmark,
            stringResource(Res.string.auth_bookmark),
            tint = if (job.bookmarked) palette.primary else palette.inkFaint,
            modifier = Modifier.size(20.dp).clickable { onBookmark(job) },
        )
    }
}

// ---------------------------------------------------------------------------
// Studentlar
// ---------------------------------------------------------------------------

@Composable
internal fun StudentsSection(
    students: List<Student>,
    palette: AppPalette,
    onSeeAll: () -> Unit,
    onFriend: (Student) -> Unit,
) {
    SectionHeader(
        stringResource(Res.string.auth_students_title),
        palette = palette,
        action = stringResource(Res.string.auth_home_students_action),
        subtitle = stringResource(Res.string.auth_home_students_subtitle),
        onAction = onSeeAll,
    )
    Spacer(Modifier.height(AppSpacing.md))
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        students.take(6).forEach { StudentConnectCard(it, palette, onFriend) }
    }
}

@Composable
private fun StudentConnectCard(student: Student, palette: AppPalette, onFriend: (Student) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.width(128.dp).clip(shape).background(palette.glass)
            .border(1.dp, palette.border, shape).padding(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        MonogramTile(student.initial, size = 48.dp)
        Text(
            student.firstName,
            style = AppType.label.copy(color = palette.ink),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(student.universityMonogram, style = AppType.caption.copy(fontSize = 10.5f.sp, color = palette.inkFaint))
        FriendButton(student, palette, onFriend)
    }
}

@Composable
private fun FriendButton(student: Student, palette: AppPalette, onFriend: (Student) -> Unit) {
    val pending = student.friendStatus == FriendStatus.PENDING
    val friends = student.friendStatus == FriendStatus.FRIENDS
    val label = when {
        friends -> stringResource(Res.string.auth_friend_single)
        pending -> stringResource(Res.string.auth_friend_pending)
        else -> stringResource(Res.string.auth_friend_add_short)
    }
    val filled = !pending && !friends
    Box(
        Modifier.fillMaxWidth().clip(AppRadius.sm)
            .background(if (filled) palette.primary else palette.primary.copy(alpha = 0.12f))
            .clickable { onFriend(student) }.padding(vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = AppType.caption.copy(
                fontWeight = AppType.button.fontWeight,
                color = if (filled) palette.onPrimary else palette.primary,
            ),
            maxLines = 1,
        )
    }
}
