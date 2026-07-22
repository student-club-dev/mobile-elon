package dev.feature.auth.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.FooterLink
import dev.core.uikit.component.LogoTile
import dev.core.uikit.component.OutlineButton
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_email_verify_fallback
import dev.core.uikit.resources.auth_email_verify_spam_hint
import dev.core.uikit.resources.auth_email_verify_suffix
import dev.core.uikit.resources.auth_email_verify_title
import dev.core.uikit.resources.auth_have_account
import dev.core.uikit.resources.auth_register_choice_hint
import dev.core.uikit.resources.auth_register_choice_subtitle
import dev.core.uikit.resources.auth_register_choice_title
import dev.core.uikit.resources.auth_register_with_email
import dev.core.uikit.resources.auth_register_with_phone
import dev.core.uikit.resources.auth_resend_code
import dev.core.uikit.resources.auth_resend_short_timer_prefix
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.auth_verify
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.AccentIconTile
import dev.feature.auth.presentation.screens.components.CodeInput
import dev.feature.auth.presentation.screens.components.ResendRow
import org.jetbrains.compose.resources.stringResource

// ===========================================================================
// Ro'yxatdan o'tish — usul tanlash (Telefon / Email)
// ===========================================================================

@Composable
fun RegisterChoiceScreen(
    onBack: () -> Unit,
    onPhone: () -> Unit,
    onEmail: () -> Unit,
    onSignIn: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        LogoTile(size = 56.dp, radius = 17.dp, iconSize = 30.dp)
        Spacer(Modifier.height(AppSpacing.xl))
        ScreenTitle(stringResource(Res.string.auth_register_choice_title))
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_register_choice_subtitle))

        Spacer(Modifier.weight(1f))

        PrimaryButton(stringResource(Res.string.auth_register_with_phone), onPhone, trailingIcon = AppIcons.Phone)
        Spacer(Modifier.height(AppSpacing.md))
        OutlineButton(stringResource(Res.string.auth_register_with_email), onEmail, leadingIcon = AppIcons.Mail)

        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(Res.string.auth_register_choice_hint),
            style = AppType.hint.copy(lineHeight = 16.sp, color = palette.inkFaint, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm),
        )

        Spacer(Modifier.weight(1f))
        FooterLink(
            stringResource(Res.string.auth_have_account),
            stringResource(Res.string.auth_sign_in),
            onSignIn,
        )
    }
}

// ===========================================================================
// Email tasdiqlash — 6 xonali kod
// ===========================================================================

@Composable
fun EmailVerifyScreen(
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
        AccentIconTile(AppIcons.Mail, palette)
        Spacer(Modifier.height(AppSpacing.lg))
        ScreenTitle(stringResource(Res.string.auth_email_verify_title))
        Spacer(Modifier.height(AppSpacing.sm))

        val address = state.email.ifBlank { stringResource(Res.string.auth_email_verify_fallback) }
        val suffix = stringResource(Res.string.auth_email_verify_suffix)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = palette.ink, fontWeight = AppType.label.fontWeight)) { append(address) }
                withStyle(SpanStyle(color = palette.inkMuted)) { append(suffix) }
            },
            style = AppType.subtitle,
        )

        Spacer(Modifier.height(AppSpacing.section))
        CodeInput(state.emailCode, vm::onEmailCodeChange, palette)

        Spacer(Modifier.height(20.dp))
        ResendRow(
            seconds = state.resendSeconds,
            timerPrefix = stringResource(Res.string.auth_resend_short_timer_prefix),
            resendLabel = stringResource(Res.string.auth_resend_code),
            onResend = onResend,
            palette = palette,
        )

        Spacer(Modifier.height(AppSpacing.section))
        PrimaryButton(
            stringResource(Res.string.auth_verify),
            onVerify,
            enabled = state.emailCodeValid && !state.isLoading,
            trailingIcon = AppIcons.Check,
        )

        state.info?.let {
            Spacer(Modifier.height(AppSpacing.md))
            Text(it, style = AppType.error.copy(fontWeight = AppType.bodyStrong.fontWeight, color = palette.success))
        }
        ErrorText(state.error)

        Spacer(Modifier.weight(1f))
        Text(
            stringResource(Res.string.auth_email_verify_spam_hint),
            style = AppType.hint.copy(color = palette.inkFaint),
        )
    }
}
