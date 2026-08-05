package dev.feature.discounts.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.core.uikit.component.AppBottomSheet
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BottomSheetOption
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_price_sum
import dev.core.uikit.resources.discounts_redeem_already
import dev.core.uikit.resources.discounts_redeem_amount_label
import dev.core.uikit.resources.discounts_redeem_branch
import dev.core.uikit.resources.discounts_redeem_code_hint
import dev.core.uikit.resources.discounts_redeem_code_label
import dev.core.uikit.resources.discounts_redeem_confirm
import dev.core.uikit.resources.discounts_redeem_confirmed
import dev.core.uikit.resources.discounts_redeem_confirming
import dev.core.uikit.resources.discounts_redeem_expired
import dev.core.uikit.resources.discounts_redeem_invalid
import dev.core.uikit.resources.discounts_redeem_invalid_code
import dev.core.uikit.resources.discounts_redeem_limit
import dev.core.uikit.resources.discounts_redeem_title
import dev.core.uikit.resources.discounts_redeem_valid
import dev.core.uikit.resources.discounts_redeem_verify
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.rowShadow
import dev.feature.discounts.domain.model.RedemptionInvalidReason
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.RedeemUiState
import org.jetbrains.compose.resources.stringResource

/**
 * Kassir oynasi — talaba ko'rsatgan kodni tekshirish va chegirmani qo'llash.
 *
 * Oqim ikki qadamli va bu **ataylab**: "Tekshirish" hech narsani o'zgartirmaydi (kassir
 * kodni xavfsiz tekshiradi, talabaning ismi va qo'llanadigan narxni ko'radi), "Tasdiqlash"
 * esa foydalanishni hisobga oladi. Bir tugmaga birlashtirilsa xato terilgan kod ham darrov
 * "ishlatilgan" bo'lib qolardi.
 */
@Composable
fun RedeemSheet(
    state: RedeemUiState,
    palette: AppPalette,
    onCode: (String) -> Unit,
    onBranch: (String) -> Unit,
    onAmount: (String) -> Unit,
    onVerify: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(
        visible = state.listing != null,
        onDismiss = onDismiss,
        title = stringResource(Res.string.discounts_redeem_title),
        palette = palette,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                stringResource(Res.string.discounts_redeem_code_label),
                style = AppType.fieldLabel.copy(color = palette.ink),
            )
            GlassTextField(
                state.code,
                onCode,
                stringResource(Res.string.discounts_redeem_code_hint),
                modifier = Modifier.rowShadow(AppRadius.lg),
                leading = AppIcons.ShieldCheck,
                palette = palette,
            )

            // Tekshiruv natijasi. Yaroqsiz kod — XATO EMAS: server javob berdi, faqat kod
            // yaramaydi. Shuning uchun u aniq sabab bilan ko'rsatiladi, umumiy "xatolik" bilan
            // emas — kassir talabaga nima deyishni bilishi kerak.
            state.check?.let { check ->
                if (check.isValid) {
                    Text(
                        stringResource(Res.string.discounts_redeem_valid),
                        style = AppType.label.copy(color = palette.success),
                    )
                    check.studentName?.takeIf { it.isNotBlank() }?.let { name ->
                        Text(name, style = AppType.body.copy(color = palette.ink))
                    }
                    check.finalPrice?.let { price ->
                        Text(
                            stringResource(Res.string.discounts_price_sum, price.formatSum()),
                            style = AppType.cardTitle.copy(color = palette.ink),
                        )
                    }
                } else {
                    Text(
                        stringResource(check.invalidReason.messageRes()),
                        style = AppType.label.copy(color = palette.danger),
                    )
                }
            }

            state.error?.let { error ->
                Text(error, style = AppType.error.copy(color = palette.danger))
            }

            // Filial va summa faqat kod haqiqiy bo'lgach so'raladi: undan oldin ular
            // to'ldirilsa ham hech qayerga ketmaydi va faqat oynani og'irlashtiradi.
            if (state.check?.isValid == true && !state.confirmed) {
                if (state.branches.size > 1) {
                    Text(
                        stringResource(Res.string.discounts_redeem_branch),
                        style = AppType.fieldLabel.copy(color = palette.ink),
                    )
                    state.branches.forEach { branch ->
                        BottomSheetOption(
                            label = branch.display(),
                            selected = branch.id == state.branchId,
                            onClick = { onBranch(branch.id) },
                            palette = palette,
                        )
                    }
                }

                Text(
                    stringResource(Res.string.discounts_redeem_amount_label),
                    style = AppType.fieldLabel.copy(color = palette.ink),
                )
                GlassTextField(
                    state.amount,
                    onAmount,
                    stringResource(Res.string.discounts_price_sum, "0"),
                    modifier = Modifier.rowShadow(AppRadius.lg),
                    type = AppFieldType.Number,
                    palette = palette,
                )
            }

            Spacer(Modifier.height(AppSpacing.sm))

            when {
                // Yakunlandi — takroriy tasdiqlash yo'q: ikkinchi `confirm` serverда
                // `ALREADY_REDEEMED` bo'lardi va kassirni chalkashtirardi.
                state.confirmed -> Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(Res.string.discounts_redeem_confirmed),
                        style = AppType.cardTitle.copy(color = palette.success),
                    )
                }
                state.check?.isValid == true -> PrimaryButton(
                    stringResource(
                        if (state.confirming) {
                            Res.string.discounts_redeem_confirming
                        } else {
                            Res.string.discounts_redeem_confirm
                        },
                    ),
                    onClick = onConfirm,
                    enabled = state.canConfirm,
                    palette = palette,
                )
                else -> PrimaryButton(
                    stringResource(Res.string.discounts_redeem_verify),
                    onClick = onVerify,
                    // Bo'sh kodni yuborishning ma'nosi yo'q — javob baribir `INVALID_CODE`.
                    enabled = state.code.isNotBlank() && !state.verifying,
                    palette = palette,
                )
            }

            Spacer(Modifier.height(AppSpacing.lg))
        }
    }
}

/**
 * Yaroqsizlik sababi → o'zbekcha matn.
 *
 * Noma'lum sabab (`null`, yoki backend keyinchalik yangi qiymat qo'shsa) umumiy "kod
 * yaroqsiz" matnini oladi — bu ekranni yangi server qiymatidan **sinmaydigan** qiladi.
 */
private fun RedemptionInvalidReason?.messageRes() = when (this) {
    RedemptionInvalidReason.INVALID_CODE -> Res.string.discounts_redeem_invalid_code
    RedemptionInvalidReason.ALREADY_REDEEMED -> Res.string.discounts_redeem_already
    RedemptionInvalidReason.EXPIRED -> Res.string.discounts_redeem_expired
    RedemptionInvalidReason.LIMIT_REACHED -> Res.string.discounts_redeem_limit
    null -> Res.string.discounts_redeem_invalid
}
