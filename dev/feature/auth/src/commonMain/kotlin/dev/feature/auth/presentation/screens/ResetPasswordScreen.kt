package dev.feature.auth.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_confirm_password_label
import dev.core.uikit.resources.auth_confirm_password_placeholder
import dev.core.uikit.resources.auth_field_password_label
import dev.core.uikit.resources.auth_password_min_placeholder
import dev.core.uikit.resources.auth_resend_code
import dev.core.uikit.resources.auth_resend_code_timer_prefix
import dev.core.uikit.resources.auth_reset_submit
import dev.core.uikit.resources.auth_reset_subtitle
import dev.core.uikit.resources.auth_reset_title
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.AccentIconTile
import dev.feature.auth.presentation.screens.components.CodeInput
import dev.feature.auth.presentation.screens.components.ResendRow
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

/**
 * Parolni tiklash, 2-qadam — SMS kod + yangi parol (`POST /auth/business/password/reset`).
 *
 * Kod va parol bitta ekranda: backend ikkalasini bitta so'rovda kutadi, shuning uchun ularni
 * ikki ekranga ajratish faqat ortiqcha qadam bo'lardi.
 */
@Composable
fun ResetPasswordScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
    palette: AppPalette = appPalette,
) {
    val pwVisual = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    AppScreenScaffold(scroll = true) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        AccentIconTile(AppIcons.Lock, palette)
        Spacer(Modifier.height(AppSpacing.lg))
        ScreenTitle(stringResource(Res.string.auth_reset_title), fontSize = 23.sp)
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_reset_subtitle))

        Spacer(Modifier.height(AppSpacing.section))
        CodeInput(state.otp, vm::onOtpChange, palette)

        Spacer(Modifier.height(AppSpacing.md))
        ResendRow(
            seconds = state.resendSeconds,
            timerPrefix = stringResource(Res.string.auth_resend_code_timer_prefix),
            resendLabel = stringResource(Res.string.auth_resend_code),
            onResend = onResend,
            palette = palette,
        )

        Spacer(Modifier.height(AppSpacing.lg))
        FieldLabel(stringResource(Res.string.auth_field_password_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = stringResource(Res.string.auth_password_min_placeholder),
            leading = AppIcons.Lock,
            trailing = {
                Icon(
                    if (state.passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    null,
                    tint = palette.inkFaint,
                    modifier = Modifier.size(AppSize.iconMd).clip(AppRadius.sm)
                        .clickableNoRipple { vm.togglePasswordVisible() },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = pwVisual,
        )

        Spacer(Modifier.height(13.dp))
        FieldLabel(stringResource(Res.string.auth_confirm_password_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.confirmPassword,
            onValueChange = vm::onConfirmPasswordChange,
            placeholder = stringResource(Res.string.auth_confirm_password_placeholder),
            leading = AppIcons.Lock,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = pwVisual,
        )

        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(
            stringResource(Res.string.auth_reset_submit),
            onSubmit,
            enabled = state.resetReady,
            trailingIcon = AppIcons.Check,
        )

        ErrorText(state.error)
        Spacer(Modifier.height(AppSpacing.lg))
    }
}
