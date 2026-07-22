package dev.feature.discounts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.EmptyState
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
import dev.core.uikit.resources.discounts_my_businesses
import dev.core.uikit.resources.discounts_profile
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.presentation.components.BusinessCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Bizneslarim" — biznes egasining barcha bizneslari. Har biriga alohida chegirma va
 * e'lonlar joylanadi, shuning uchun bu ekran butun biznes oqimining kirish nuqtasi.
 */
@Composable
fun MyBusinessesScreen(
    onOpenBusiness: (Business) -> Unit,
    onEditBusiness: (Business) -> Unit,
    onAddBusiness: () -> Unit,
    onProfile: () -> Unit = {},
    vm: MyBusinessesViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<Business?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg)
                    .padding(top = 54.dp, bottom = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.discounts_business_hub),
                        style = AppType.caption.copy(
                            fontWeight = AppType.buttonSecondary.fontWeight,
                            color = palette.primary,
                        ),
                    )
                    Text(
                        stringResource(Res.string.discounts_my_businesses),
                        style = AppType.screenTitle.copy(fontSize = 26.sp, color = palette.ink),
                    )
                }
                // Gradient profil tugmasi — E'lonlarim ekranidagi bilan bir xil joylashuv.
                Box(
                    Modifier.size(46.dp)
                        .shadow(10.dp, CircleShape, spotColor = palette.primary.copy(alpha = 0.5f))
                        .clip(CircleShape).background(palette.primaryBrush)
                        .clickable(onClick = onProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    // Gradient FONI USTIDA — kontent rangi `onPrimary`.
                    Icon(
                        AppIcons.Store,
                        stringResource(Res.string.discounts_profile),
                        tint = palette.onPrimary,
                        modifier = Modifier.size(21.dp),
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
                        start = AppSpacing.lg,
                        end = AppSpacing.lg,
                        top = AppSpacing.sm,
                        bottom = 110.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
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

        // Pastdagi "+" tugma — yangi biznes qo'shish.
        Row(
            Modifier.align(Alignment.BottomEnd).padding(20.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = palette.primary.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(18.dp)).background(palette.primaryBrush)
                .clickable(onClick = onAddBusiness)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(AppIcons.Plus, null, tint = palette.onPrimary, modifier = Modifier.size(20.dp))
            Text(
                stringResource(Res.string.discounts_business),
                style = AppType.button.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.onPrimary),
            )
        }

        // O'chirishни tasdiqlash dialogи.
        toDelete?.let { biz ->
            AlertDialog(
                onDismissRequest = { toDelete = null },
                title = {
                    Text(
                        stringResource(Res.string.discounts_business_delete_title),
                        style = AppType.sectionTitle.copy(
                            fontWeight = AppType.screenTitle.fontWeight,
                            color = palette.ink,
                        ),
                    )
                },
                text = {
                    Text(
                        stringResource(Res.string.discounts_business_delete_confirm, biz.name),
                        style = AppType.body.copy(fontWeight = AppType.subtitle.fontWeight, color = palette.inkMuted),
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
                            style = AppType.body.copy(fontWeight = AppType.subtitle.fontWeight, color = palette.inkMuted),
                        )
                    }
                },
            )
        }
    }
}
