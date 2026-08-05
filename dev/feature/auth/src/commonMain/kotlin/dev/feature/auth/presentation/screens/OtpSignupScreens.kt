package dev.feature.auth.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_otp_phone_prefix
import dev.core.uikit.resources.auth_otp_sent_suffix
import dev.core.uikit.resources.auth_otp_cancel
import dev.core.uikit.resources.auth_otp_title
import dev.core.uikit.resources.auth_resend_code
import dev.core.uikit.resources.auth_resend_code_timer_prefix
import dev.core.uikit.resources.auth_verify
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.util.formatUzPhone
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.AccentIconTile
import dev.feature.auth.presentation.screens.components.CodeInput
import dev.feature.auth.presentation.screens.components.ResendRow
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

/**
 * Telefon raqamini tasdiqlash (`POST /auth/business/otp/verify`) — ro'yxatdan o'tishning
 * **majburiy** 2-qadami.
 *
 * Hisob bu paytda serverда allaqachon ochilgan (backend `register` da sessiya beradi), lekin
 * bu foydalanuvchi uchun "kirdim" degani emas: tasdiqlanmagan raqam bilan biznes yaratib
 * bo'lmaydi (`403 PHONE_NOT_VERIFIED`) va parolni tiklash ham ishlamaydi. Shuning uchun
 * "keyinroq tasdiqlayman" yo'q — [onBack] esa oqimni butunlay bekor qiladi.
 */
@Composable
fun OtpScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        AccentIconTile(AppIcons.MessageSquare, palette)
        Spacer(Modifier.height(AppSpacing.lg))
        ScreenTitle(stringResource(Res.string.auth_otp_title))
        Spacer(Modifier.height(6.dp))

        val phonePart = stringResource(
            Res.string.auth_otp_phone_prefix,
            formatUzPhone(state.phone.ifEmpty { "901234567" }),
        )
        val suffixPart = stringResource(Res.string.auth_otp_sent_suffix)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = palette.ink, fontWeight = AppType.label.fontWeight)) {
                    append(phonePart)
                }
                withStyle(SpanStyle(color = palette.inkMuted)) { append(suffixPart) }
            },
            style = AppType.subtitle,
        )
        Spacer(Modifier.height(AppSpacing.section))

        CodeInput(state.otp, vm::onOtpChange, palette)

        Spacer(Modifier.height(AppSpacing.section))
        ResendRow(
            seconds = state.resendSeconds,
            timerPrefix = stringResource(Res.string.auth_resend_code_timer_prefix),
            resendLabel = stringResource(Res.string.auth_resend_code),
            onResend = onResend,
            palette = palette,
        )

        Spacer(Modifier.height(AppSpacing.section))
        PrimaryButton(
            stringResource(Res.string.auth_verify),
            onVerify,
            enabled = state.otpValid && !state.isLoading,
            trailingIcon = AppIcons.Check,
        )

        ErrorText(state.error)

        Spacer(Modifier.height(AppSpacing.md))
        // Oqimni tashlab ketish yo'li — "keyinroq tasdiqlayman" emas: raqam tasdiqlanmasa
        // ilovada qiladigan ish yo'q. Chiqib ketgan foydalanuvchi keyin o'sha raqam va parol
        // bilan kiraveradi.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                stringResource(Res.string.auth_otp_cancel),
                style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.inkMuted),
                modifier = Modifier.clickableNoRipple(onBack),
            )
        }
    }
}
