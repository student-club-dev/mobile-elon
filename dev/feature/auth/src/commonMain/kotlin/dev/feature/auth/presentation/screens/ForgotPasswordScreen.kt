package dev.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.BannerTone
import dev.core.uikit.component.ToastEffect
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_back_to_login
import dev.core.uikit.resources.auth_field_phone_label
import dev.core.uikit.resources.auth_forgot_phone_subtitle
import dev.core.uikit.resources.auth_forgot_send
import dev.core.uikit.resources.auth_forgot_title
import dev.core.uikit.resources.auth_phone_placeholder
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.PhonePrefix
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

/**
 * Parolni tiklash, 1-qadam — telefon raqami (`POST /auth/business/password/forgot`).
 *
 * Backend tiklash kodini **faqat SMS orqali** yuboradi, shuning uchun bu yerda email emas,
 * telefon so'raladi. Kod ketgach [ResetPasswordScreen] ochiladi.
 */
@Composable
fun ForgotPasswordScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSend: () -> Unit,
    onBackToLogin: () -> Unit,
    palette: AppPalette = appPalette,
) {
    // Xato va "kod yuborildi" xabari — ekran ichidagi yozuv emas, yuqori o'ng burchakdagi toast.
    ToastEffect(state.error, onConsumed = vm::clearError)
    ToastEffect(state.info, tone = BannerTone.SUCCESS, onConsumed = vm::clearError)

    AppScreenScaffold(scroll = false) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(40.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(96.dp).background(palette.accentBg, AppRadius.card),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(64.dp).background(palette.primaryBrush, AppRadius.row),
                    contentAlignment = Alignment.Center,
                ) {
                    // Gradient USTIDAGI ikonka — har ikkala rejimda oq bo'lib qoladi.
                    Icon(AppIcons.Lock, null, tint = palette.onPrimary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(AppSpacing.section))
            ScreenTitle(stringResource(Res.string.auth_forgot_title), fontSize = 23.sp)
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                stringResource(Res.string.auth_forgot_phone_subtitle),
                style = AppType.subtitle.copy(color = palette.inkMuted, textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }

        Spacer(Modifier.height(26.dp))
        FieldLabel(stringResource(Res.string.auth_field_phone_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = stringResource(Res.string.auth_phone_placeholder),
            leadingContent = { PhonePrefix(palette) },
            focused = true,
            type = AppFieldType.UzPhone,
        )
        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(
            stringResource(Res.string.auth_forgot_send),
            onSend,
            enabled = state.phoneValid && !state.isLoading,
            loading = state.isLoading,
        )

        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth().clickableNoRipple(onBackToLogin),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppIcons.ArrowLeft, null, tint = palette.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                stringResource(Res.string.auth_back_to_login),
                style = AppType.subtitle.copy(fontWeight = AppType.label.fontWeight, color = palette.primary),
            )
        }
    }
}
