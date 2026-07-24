package dev.feature.auth.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.AuthTab
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.FooterLink
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.OutlineButton
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.component.SegmentedTabs
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_email_placeholder
import dev.core.uikit.resources.auth_face_id_login
import dev.core.uikit.resources.auth_google_login
import dev.core.uikit.resources.auth_google_unavailable
import dev.core.uikit.resources.auth_telegram_login
import dev.core.uikit.resources.auth_telegram_soon
import dev.feature.auth.oauth.GoogleSignInResult
import dev.feature.auth.oauth.rememberGoogleSignIn
import kotlinx.coroutines.launch
import dev.core.uikit.resources.auth_field_email_label
import dev.core.uikit.resources.auth_field_password_label
import dev.core.uikit.resources.auth_forgot_password
import dev.core.uikit.resources.auth_login_subtitle
import dev.core.uikit.resources.auth_login_title
import dev.core.uikit.resources.auth_no_account
import dev.core.uikit.resources.auth_password_placeholder
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.auth_sign_up
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

/**
 * Email + parol bilan kirish (`POST /auth/business/login`).
 *
 * Asosiy kirish usuli — telefon + parol (`BusinessWelcomeScreen`); bu ekran hisobi emailga
 * bog'langan foydalanuvchilar uchun. Segment tanlagichi telefon ekraniga qaytaradi.
 */
/**
 * Ijtimoiy kirish tugmalari — Google (backend tayyor: `/auth/business/oauth/google`) va
 * Telegram. Telegram backend endpointи yo'q, shuning uchun hozircha "tez orada" xabarini beradi.
 */
@Composable
private fun OAuthLoginButtons(vm: AuthFlowViewModel) {
    val googleSignIn = rememberGoogleSignIn()
    val scope = rememberCoroutineScope()
    val googleUnavailable = stringResource(Res.string.auth_google_unavailable)
    val telegramSoon = stringResource(Res.string.auth_telegram_soon)

    OutlineButton(
        stringResource(Res.string.auth_google_login),
        onClick = {
            scope.launch {
                when (val result = googleSignIn.signIn()) {
                    is GoogleSignInResult.Success -> vm.signInWithGoogle(result.idToken)
                    is GoogleSignInResult.Failed -> vm.showAuthError(result.message)
                    GoogleSignInResult.Unavailable -> vm.showAuthError(googleUnavailable)
                    GoogleSignInResult.Cancelled -> Unit
                }
            }
        },
    )
    Spacer(Modifier.height(11.dp))
    OutlineButton(
        stringResource(Res.string.auth_telegram_login),
        onClick = { vm.showAuthError(telegramSoon) },
    )
}

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
        Spacer(Modifier.height(AppSpacing.xl))
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
                    modifier = Modifier.size(AppSize.iconMd).clip(AppRadius.sm)
                        .clickableNoRipple { vm.togglePasswordVisible() },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                stringResource(Res.string.auth_forgot_password),
                style = AppType.link.copy(fontWeight = AppType.label.fontWeight, color = palette.primary),
                modifier = Modifier.clickableNoRipple(onForgot),
            )
        }

        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(stringResource(Res.string.auth_sign_in), onLogin, enabled = state.emailLoginReady)
        Spacer(Modifier.height(11.dp))
        OutlineButton(
            stringResource(Res.string.auth_face_id_login),
            onBiometric,
            leadingIcon = AppIcons.ScanFace,
        )
        Spacer(Modifier.height(11.dp))
        OAuthLoginButtons(vm)

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
