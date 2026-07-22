package dev.feature.discounts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.CompactPrimaryButton
import dev.core.uikit.component.EmptyState
import dev.core.uikit.component.GradientIconButton
import dev.core.uikit.component.IconActionButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_cancel
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.discounts_business
import dev.core.uikit.resources.discounts_business_add
import dev.core.uikit.resources.discounts_business_delete_confirm
import dev.core.uikit.resources.discounts_business_delete_title
import dev.core.uikit.resources.discounts_business_empty_message
import dev.core.uikit.resources.discounts_business_empty_title
import dev.core.uikit.resources.discounts_business_hub
import dev.core.uikit.resources.discounts_business_messages
import dev.core.uikit.resources.discounts_business_support
import dev.core.uikit.resources.discounts_my_businesses
import dev.core.uikit.resources.discounts_profile
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.presentation.components.BusinessCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.unit.sp

/** Handoff: yuqoridagi oq harakat tugmalari 44dp, ichidagi ikonka 20dp. */
private val TopActionSize = 44.dp
private val TopActionIconSize = 20.dp

/**
 * "Bizneslarim" — biznes egasining barcha bizneslari. Har biriga alohida chegirma va
 * e'lonlar joylanadi, shuning uchun bu ekran butun biznes oqimining kirish nuqtasi.
 *
 * Tuzilishi handoff'dagi "SCREEN 1: Bizneslarim" bilan bir xil: mikro-yorliq + katta
 * sarlavha, o'ngda uchta tugma (xabarlar, qo'llab-quvvatlash, profil), ro'yxat va pastda
 * o'ngda suzuvchi "+ Biznes" CTA.
 */
@Composable
fun MyBusinessesScreen(
    onOpenBusiness: (Business) -> Unit,
    onEditBusiness: (Business) -> Unit,
    onAddBusiness: () -> Unit,
    onProfile: () -> Unit = {},
    // Xabarlar va qo'llab-quvvatlash chatlari alohida oqim — karkas (`BusinessShell`)
    // ularni ulaguncha tugmalar ko'rinadi, lekin harakat bermaydi.
    onMessages: () -> Unit = {},
    onSupport: () -> Unit = {},
    vm: MyBusinessesViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<Business?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(top = 54.dp, bottom = AppSpacing.sm),
                // Tugmalar sarlavha blokining YUQORISIGA tekislanadi.
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.discounts_business_hub),
                        style = AppType.microLabel.copy(color = palette.primary),
                    )
                    Text(
                        stringResource(Res.string.discounts_my_businesses),
                        // 26sp — handoff H1 oralig'ining (26-30) pastki chegarasi. 30sp da
                        // "Bizneslarim" tor ekranlarda (360dp) uchta tugma yonida sig'maydi va
                        // ikki qatorga bo'linib ketardi; `maxLines` buni butunlay taqiqlaydi.
                        style = AppType.screenTitle.copy(fontSize = 26.sp, color = palette.ink),
                        maxLines = 1,
                    )
                }
                Row(
                    Modifier.padding(top = AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    IconActionButton(
                        AppIcons.MessageSquare,
                        onClick = onMessages,
                        contentDescription = stringResource(Res.string.discounts_business_messages),
                        size = TopActionSize,
                        iconSize = TopActionIconSize,
                        palette = palette,
                    )
                    IconActionButton(
                        AppIcons.Support,
                        onClick = onSupport,
                        contentDescription = stringResource(Res.string.discounts_business_support),
                        size = TopActionSize,
                        iconSize = TopActionIconSize,
                        palette = palette,
                    )
                    // Uchinchisi urg'uli — kattaroq va ko'k gradient (handoff: 54dp, radius 20).
                    GradientIconButton(
                        AppIcons.Users,
                        onClick = onProfile,
                        contentDescription = stringResource(Res.string.discounts_profile),
                        shape = AppRadius.row,
                        palette = palette,
                    )
                }
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
                }
                state.businesses.isEmpty() -> EmptyState(
                    icon = AppIcons.Store,
                    title = stringResource(Res.string.discounts_business_empty_title),
                    message = stringResource(Res.string.discounts_business_empty_message),
                    modifier = Modifier.fillMaxSize(),
                    actionText = stringResource(Res.string.discounts_business_add),
                    actionIcon = AppIcons.Plus,
                    onAction = onAddBusiness,
                    palette = palette,
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = AppSpacing.screenHorizontal,
                        end = AppSpacing.screenHorizontal,
                        top = 14.dp,
                        bottom = 110.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    items(state.businesses, key = { it.id }) { biz ->
                        BusinessCard(
                            biz, palette,
                            onClick = { onOpenBusiness(biz) },
                            onEdit = { onEditBusiness(biz) },
                            onDelete = { toDelete = biz },
                        )
                    }
                }
            }
        }

        // Pastdagi "+ Biznes" CTA — yangi biznes qo'shish.
        CompactPrimaryButton(
            text = stringResource(Res.string.discounts_business),
            onClick = onAddBusiness,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = AppSpacing.screenHorizontal, bottom = AppSpacing.screenBottom),
            palette = palette,
        )

        // O'chirishни tasdiqlash dialogи.
        toDelete?.let { biz ->
            AlertDialog(
                onDismissRequest = { toDelete = null },
                title = {
                    Text(
                        stringResource(Res.string.discounts_business_delete_title),
                        style = AppType.sectionTitle.copy(color = palette.ink),
                    )
                },
                text = {
                    Text(
                        stringResource(Res.string.discounts_business_delete_confirm, biz.name),
                        style = AppType.body.copy(color = palette.inkMuted),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { vm.delete(biz.id); toDelete = null }) {
                        Text(
                            stringResource(Res.string.common_delete),
                            style = AppType.label.copy(color = palette.danger),
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { toDelete = null }) {
                        Text(
                            stringResource(Res.string.common_cancel),
                            style = AppType.body.copy(color = palette.inkMuted),
                        )
                    }
                },
            )
        }
    }
}
