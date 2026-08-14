package dev.feature.profile.presentation.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.core.domain.model.Ad
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.screenTopInset
import dev.core.uikit.component.GlassRow
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_cancel
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.common_edit
import dev.core.uikit.resources.profile_ad_delete_confirm
import dev.core.uikit.resources.profile_ad_delete_title
import dev.core.uikit.resources.profile_application_status_interview
import dev.core.uikit.resources.profile_application_status_rejected
import dev.core.uikit.resources.profile_application_status_sent
import dev.core.uikit.resources.profile_application_status_viewed
import dev.core.uikit.resources.profile_section_applications
import dev.core.uikit.resources.profile_section_my_ads
import dev.core.uikit.resources.profile_section_saved
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.rowShadow
import dev.feature.profile.presentation.ProfileUiState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Profil ichidagi ro'yxat bo'limlari. Sarlavha matni resursdan olinadi. */
enum class ProfileSection(val title: StringResource) {
    MY_ADS(Res.string.profile_section_my_ads),
    SAVED(Res.string.profile_section_saved),
    APPLICATIONS(Res.string.profile_section_applications),
}

/** Tanlangan bo'lim ro'yxati — "Mening e'lonlarim", "Saqlanganlar", "Arizalarim". */
@Composable
fun SectionList(
    section: ProfileSection,
    state: ProfileUiState,
    palette: AppPalette,
    onBack: () -> Unit,
    onDeleteAd: (String) -> Unit,
    onEditAd: (String) -> Unit,
) {
    var adToDelete by remember { mutableStateOf<Ad?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().screenTopInset(AppSpacing.md).padding(horizontal = AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            // Orqaga tugmasi — oq karta, uni chegara emas soya ajratadi.
            Box(
                Modifier.size(AppSize.iconButton).rowShadow(AppRadius.md).clip(AppRadius.md)
                    .background(palette.card)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.ArrowLeft,
                    stringResource(Res.string.common_back),
                    tint = palette.ink,
                    modifier = Modifier.size(AppSize.iconSm),
                )
            }
            Text(stringResource(section.title), style = AppType.sheetTitle.copy(color = palette.ink))
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            when (section) {
                ProfileSection.MY_ADS -> items(state.myAds, key = { it.id }) { ad ->
                    DeletableAdRow(
                        title = ad.title,
                        subtitle = "${ad.category} · ${ad.price}",
                        trailing = ad.createdAgo,
                        palette = palette,
                        onEdit = { onEditAd(ad.id) },
                        onDelete = { adToDelete = ad },
                    )
                }
                ProfileSection.SAVED -> items(state.savedDiscounts, key = { it.id }) {
                    SimpleRow("${it.merchant} — ${it.title}", "−${it.discountPercent}%", it.expiry ?: "", palette)
                }
                ProfileSection.APPLICATIONS -> items(state.applications, key = { it.id }) {
                    SimpleRow(it.jobTitle, it.company, statusLabel(it.status.name), palette)
                }
            }
        }
    }

    val target = adToDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { adToDelete = null },
            title = {
                Text(stringResource(Res.string.profile_ad_delete_title), style = AppType.sectionTitle.copy(color = palette.ink))
            },
            text = {
                Text(
                    stringResource(Res.string.profile_ad_delete_confirm, target.title),
                    style = AppType.body.copy(color = palette.inkMuted),
                )
            },
            confirmButton = {
                TextButton(onClick = { onDeleteAd(target.id); adToDelete = null }) {
                    Text(stringResource(Res.string.common_delete), style = AppType.buttonSecondary.copy(color = palette.danger))
                }
            },
            dismissButton = {
                TextButton(onClick = { adToDelete = null }) {
                    Text(stringResource(Res.string.common_cancel), style = AppType.label.copy(color = palette.inkMuted))
                }
            },
        )
    }
}

/** E'lon qatori — tahrirlash va o'chirish tugmalari bilan. */
@Composable
private fun DeletableAdRow(
    title: String,
    subtitle: String,
    trailing: String,
    palette: AppPalette,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), palette = palette) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink))
            Text(subtitle, style = AppType.hint.copy(color = palette.inkFaint))
            if (trailing.isNotBlank()) {
                Text(trailing, style = AppType.caption.copy(fontWeight = AppType.fieldLabel.fontWeight, color = palette.primary))
            }
        }
        IconTile(
            AppIcons.Pencil,
            contentDescription = stringResource(Res.string.common_edit),
            tint = palette.primary,
            size = 34.dp,
            iconSize = 15.dp,
            onClick = onEdit,
        )
        IconTile(
            AppIcons.Close,
            contentDescription = stringResource(Res.string.common_delete),
            tint = palette.danger,
            background = palette.dangerBg,
            size = 34.dp,
            iconSize = 16.dp,
            onClick = onDelete,
        )
    }
}

/** Oddiy ro'yxat qatori — sarlavha, izoh va o'ngda kichik meta. */
@Composable
private fun SimpleRow(title: String, subtitle: String, trailing: String, palette: AppPalette) {
    GlassRow(palette = palette) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink))
            Text(subtitle, style = AppType.hint.copy(color = palette.inkFaint))
        }
        if (trailing.isNotBlank()) {
            Text(trailing, style = AppType.caption.copy(fontWeight = AppType.fieldLabel.fontWeight, color = palette.primary))
        }
    }
}

/** Ariza holati yorlig'i. */
@Composable
private fun statusLabel(status: String): String = when (status) {
    "SENT" -> stringResource(Res.string.profile_application_status_sent)
    "VIEWED" -> stringResource(Res.string.profile_application_status_viewed)
    "INTERVIEW" -> stringResource(Res.string.profile_application_status_interview)
    "REJECTED" -> stringResource(Res.string.profile_application_status_rejected)
    else -> status
}
