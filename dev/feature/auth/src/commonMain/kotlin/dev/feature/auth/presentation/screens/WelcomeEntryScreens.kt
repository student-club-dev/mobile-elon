package dev.feature.auth.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.AuthTab
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.FooterLink
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.HintText
import dev.core.uikit.component.LogoTile
import dev.core.uikit.component.OrDivider
import dev.core.uikit.component.OutlineButton
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.component.SegmentedTabs
import dev.core.uikit.component.SocialRow
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_continue
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.auth_email_placeholder
import dev.core.uikit.resources.auth_face_id_login
import dev.core.uikit.resources.auth_field_email_label
import dev.core.uikit.resources.auth_field_password_label
import dev.core.uikit.resources.auth_field_phone_label
import dev.core.uikit.resources.auth_forgot_password
import dev.core.uikit.resources.auth_get_code
import dev.core.uikit.resources.auth_have_account
import dev.core.uikit.resources.auth_login_subtitle
import dev.core.uikit.resources.auth_login_title
import dev.core.uikit.resources.auth_no_account
import dev.core.uikit.resources.auth_password_placeholder
import dev.core.uikit.resources.auth_phone_only_uz_hint
import dev.core.uikit.resources.auth_phone_placeholder
import dev.core.uikit.resources.auth_phone_subtitle
import dev.core.uikit.resources.auth_phone_title
import dev.core.uikit.resources.auth_remember_me
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.auth_sign_up
import dev.core.uikit.resources.auth_welcome_email_hint
import dev.core.uikit.resources.auth_welcome_phone_hint
import dev.core.uikit.resources.auth_welcome_subtitle
import dev.core.uikit.resources.auth_welcome_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.util.PhoneVisualTransformation
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.CheckBoxSmall
import dev.feature.auth.presentation.screens.components.PhonePrefix
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

// ===========================================================================
// 1a — WELCOME (to'liq forma, tab bilan)
// ===========================================================================

@Composable
fun WelcomeScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    tab: AuthTab,
    onTab: (AuthTab) -> Unit,
    onContinue: () -> Unit,
    onSignUp: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onTelegram: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = true, topPadding = 60.dp) {
        LogoTile()
        Spacer(Modifier.height(18.dp))
        ScreenTitle(stringResource(Res.string.auth_welcome_title), fontSize = 25.sp)
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_welcome_subtitle))
        Spacer(Modifier.height(18.dp))

        SegmentedTabs(tab, onTab)
        Spacer(Modifier.height(14.dp))

        if (tab == AuthTab.PHONE) {
            FieldLabel(stringResource(Res.string.auth_field_phone_label))
            Spacer(Modifier.height(7.dp))
            GlassTextField(
                value = state.phone,
                onValueChange = vm::onPhoneChange,
                placeholder = stringResource(Res.string.auth_phone_placeholder),
                leadingContent = { PhonePrefix(palette) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = PhoneVisualTransformation(),
                textLetterSpacing = 0.5f,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            HintText(stringResource(Res.string.auth_welcome_phone_hint))
        } else {
            FieldLabel(stringResource(Res.string.auth_field_email_label))
            Spacer(Modifier.height(7.dp))
            GlassTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                placeholder = stringResource(Res.string.auth_email_placeholder),
                leading = AppIcons.Mail,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(AppSpacing.sm))
            HintText(stringResource(Res.string.auth_welcome_email_hint))
        }

        Spacer(Modifier.height(AppSpacing.lg))
        PrimaryButton(stringResource(Res.string.auth_continue), onContinue, trailingIcon = AppIcons.ArrowRight)

        state.error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, style = AppType.error.copy(fontSize = 12.sp, color = palette.danger))
        }

        Spacer(Modifier.height(18.dp))
        OrDivider()
        Spacer(Modifier.height(14.dp))
        SocialRow(onGoogle, onApple, onTelegram)

        Spacer(Modifier.height(20.dp))
        FooterLink(
            stringResource(Res.string.auth_no_account),
            stringResource(Res.string.auth_sign_up),
            onSignUp,
        )
    }
}

// ===========================================================================
// 1e — PHONE ENTRY
// ===========================================================================

@Composable
fun PhoneScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSwitchEmail: () -> Unit,
    onGetCode: () -> Unit,
    onSignIn: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onTelegram: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(20.dp))
        ScreenTitle(stringResource(Res.string.auth_phone_title))
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_phone_subtitle))
        Spacer(Modifier.height(18.dp))

        SegmentedTabs(AuthTab.PHONE, { if (it == AuthTab.EMAIL) onSwitchEmail() })
        Spacer(Modifier.height(AppSpacing.lg))

        GlassTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = stringResource(Res.string.auth_phone_placeholder),
            leadingContent = { PhonePrefix(palette) },
            focused = true,
            height = 56.dp,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneVisualTransformation(),
            textLetterSpacing = 0.5f,
        )
        Spacer(Modifier.height(9.dp))
        HintText(stringResource(Res.string.auth_phone_only_uz_hint))

        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            stringResource(Res.string.auth_get_code),
            onGetCode,
            enabled = state.phoneValid && !state.isLoading,
            trailingIcon = AppIcons.ArrowRight,
        )

        ErrorText(state.error)

        Spacer(Modifier.height(18.dp))
        OrDivider()
        Spacer(Modifier.height(14.dp))
        SocialRow(onGoogle, onApple, onTelegram)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(14.dp))
        FooterLink(
            stringResource(Res.string.auth_have_account),
            stringResource(Res.string.auth_sign_in),
            onSignIn,
        )
    }
}

// ===========================================================================
// 1f — EMAIL LOGIN
// ===========================================================================

@Composable
fun EmailLoginScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSwitchPhone: () -> Unit,
    onLogin: () -> Unit,
    onForgot: () -> Unit,
    onBiometric: () -> Unit,
    onSignUp: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(18.dp))
        ScreenTitle(stringResource(Res.string.auth_login_title))
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_login_subtitle))
        Spacer(Modifier.height(AppSpacing.lg))

        SegmentedTabs(AuthTab.EMAIL, { if (it == AuthTab.PHONE) onSwitchPhone() })
        Spacer(Modifier.height(AppSpacing.lg))

        FieldLabel(stringResource(Res.string.auth_field_email_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = stringResource(Res.string.auth_email_placeholder),
            leading = AppIcons.Mail,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(13.dp))
        FieldLabel(stringResource(Res.string.auth_field_password_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = stringResource(Res.string.auth_password_placeholder),
            leading = AppIcons.Lock,
            trailing = {
                Icon(
                    if (state.passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    null,
                    tint = palette.inkFaint,
                    modifier = Modifier.size(AppSize.iconMd).clip(RoundedCornerShape(6.dp))
                        .clickableNoRipple { vm.togglePasswordVisible() },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )

        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.clickableNoRipple { vm.toggleRememberMe() },
            ) {
                CheckBoxSmall(state.rememberMe, palette)
                Text(
                    stringResource(Res.string.auth_remember_me),
                    style = AppType.link.copy(fontWeight = AppType.bodyStrong.fontWeight, color = palette.label),
                )
            }
            Text(
                stringResource(Res.string.auth_forgot_password),
                style = AppType.link.copy(fontWeight = AppType.label.fontWeight, color = palette.primary),
                modifier = Modifier.clickableNoRipple(onForgot),
            )
        }

        Spacer(Modifier.height(18.dp))
        PrimaryButton(stringResource(Res.string.auth_sign_in), onLogin, enabled = !state.isLoading)
        Spacer(Modifier.height(11.dp))
        OutlineButton(
            stringResource(Res.string.auth_face_id_login),
            onBiometric,
            leadingIcon = AppIcons.ScanFace,
        )

        ErrorText(state.error)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(14.dp))
        FooterLink(
            stringResource(Res.string.auth_no_account),
            stringResource(Res.string.auth_sign_up),
            onSignUp,
        )
    }
}
