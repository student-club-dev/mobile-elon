package dev.feature.discounts.presentation

import androidx.compose.foundation.clickable
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
import dev.core.uikit.component.BannerTone
import dev.core.uikit.component.EmptyState
import dev.core.uikit.component.StatusBanner
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
import dev.core.uikit.resources.discounts_business_submitted
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
private val TopActionSize = 40.dp
private val TopActionIconSize = 20.dp

/**
 * Qo'llab-quvvatlash ikonkasi qolganidan kattaroq: u garnitura + suhbat pufagidan iborat,
 * ya'ni chiziqlari zich. 20dp da u kichik dog'ga aylanib, nima ekani o'qilmasdi.
 */
private val SupportIconSize = 24.dp

/** Hisob tugmasi — urg'uli, lekin sarlavhaga joy qoldirishi kerak. */
private val AccountButtonSize = 46.dp
private val AccountIconSize = 24.dp

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                // Ketma-ketlik: hisob → sarlavha → xabarlar → qo'llab-quvvatlash.
                GradientIconButton(
                    AppIcons.User,
                    onClick = onProfile,
                    contentDescription = stringResource(Res.string.discounts_profile),
                    size = AccountButtonSize,
                    iconSize = AccountIconSize,
                    shape = AppRadius.md,
                    palette = palette,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.discounts_business_hub),
                        style = AppType.microLabel.copy(color = palette.primary),
                    )
                    Text(
                        stringResource(Res.string.discounts_my_businesses),
                        // Sarlavha tugmalar ORASIDA turadi, shuning uchun katta H1 (26-30sp)
                        // sig'maydi — panel sarlavhasi uchun mo'ljallangan token olinadi.
                        // `maxLines` ikki qatorga bo'linishni butunlay taqiqlaydi.
                        style = AppType.topBarTitle.copy(color = palette.ink),
                        maxLines = 1,
                    )
                }
                // Xabarlar va Qo'llab-quvvatlash tugmalari vaqtincha yashirilgan (backend
                // tayyor emas). Marshrutlar (`onMessages`/`onSupport`) va ekranlar joyida —
                // qayta yoqish uchun shu blokning izohini ochish yetarli.
                // Row(
                //     verticalAlignment = Alignment.CenterVertically,
                //     horizontalArrangement = Arrangement.spacedBy(9.dp),
                // ) {
                //     IconActionButton(
                //         AppIcons.MessageSquare,
                //         onClick = onMessages,
                //         contentDescription = stringResource(Res.string.discounts_business_messages),
                //         size = TopActionSize,
                //         iconSize = TopActionIconSize,
                //         palette = palette,
                //     )
                //     IconActionButton(
                //         AppIcons.Support,
                //         onClick = onSupport,
                //         contentDescription = stringResource(Res.string.discounts_business_support),
                //         size = TopActionSize,
                //         iconSize = SupportIconSize,
                //         palette = palette,
                //     )
                // }
            }

            // O'chirib bo'lmadi (masalan biznesda faol e'lon bor yoki tarmoq uzildi) —
            // ro'yxat serverdagi holatga qaytadi, sabab esa shu yerda ko'rinadi.
            state.message?.let { message ->
                StatusBanner(
                    text = message,
                    tone = BannerTone.DANGER,
                    modifier = Modifier
                        .padding(horizontal = AppSpacing.screenHorizontal, vertical = 6.dp)
                        .clickable(onClick = vm::consumeMessage),
                    palette = palette,
                )
            }

            // "Tekshiruvga yuborildi" — qaytgan HOLAT bilan birga. Moderatsiya o'chirilgan
            // bo'lsa biznes darrov tasdiqlanadi, yoqilganda navbatga tushadi; foydalanuvchi
            // qaysi biri bo'lganini shu yerda ko'radi.
            state.successMessage?.let { status ->
                StatusBanner(
                    text = stringResource(Res.string.discounts_business_submitted) + " — " + status,
                    tone = BannerTone.SUCCESS,
                    modifier = Modifier
                        .padding(horizontal = AppSpacing.screenHorizontal, vertical = 6.dp)
                        .clickable(onClick = vm::consumeMessage),
                    palette = palette,
                )
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
                            onSubmit = { vm.submit(biz) },
                            submitting = state.submittingId == biz.id,
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
