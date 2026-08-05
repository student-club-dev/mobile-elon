package dev.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.HintText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.media.rememberImagePicker
import dev.core.uikit.media.toImageBitmapOrNull
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_account_setup_email_hint
import dev.core.uikit.resources.auth_account_setup_hint
import dev.core.uikit.resources.auth_account_setup_subtitle
import dev.core.uikit.resources.auth_account_setup_title
import dev.core.uikit.resources.auth_continue
import dev.core.uikit.resources.auth_field_email_label
import dev.core.uikit.resources.auth_email_placeholder
import dev.core.uikit.resources.auth_first_name
import dev.core.uikit.resources.auth_last_name
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.profile_avatar_change
import dev.core.uikit.resources.profile_avatar_uploading
import dev.core.uikit.resources.profile_saving
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.profile.presentation.components.ProfileAvatar
import org.jetbrains.compose.resources.stringResource

/**
 * Ro'yxatdan o'tishning **oxirgi** qadami — hisob ma'lumotlari (`PUT /profile/me`).
 *
 * Bu ekran raqam tasdiqlangandan KEYIN ochiladi: shu paytgacha foydalanuvchi faqat raqam va
 * parol bergan, ilova esa uni ismsiz ko'rsata olmaydi (biznes kartalari, e'lonlar va chat
 * hammasi ism bilan ishlaydi). Shuning uchun **ism va familiya majburiy**, rasm va email —
 * ixtiyoriy.
 *
 * Rasm tanlangan zahoti serverга ketadi (`POST /media/upload`) va qaytgan URL profil bilan
 * birga saqlanadi — shu sabab yuklash tugamaguncha "Davom etish" kutadi.
 */
@Composable
fun AccountSetupScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    palette: AppPalette = appPalette,
) {
    // Tanlangan rasm darrov ko'rinadi; server URL'i kelguncha kutib turmaydi.
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }
    val imagePicker = rememberImagePicker { picked ->
        if (picked != null) {
            preview = picked.bytes.toImageBitmapOrNull()
            vm.uploadAvatar(picked.bytes, picked.fileName)
        }
    }

    AppScreenScaffold(scroll = true) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        ScreenTitle(stringResource(Res.string.auth_account_setup_title))
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_account_setup_subtitle))
        Spacer(Modifier.height(AppSpacing.xl))

        val changePhotoLabel = stringResource(Res.string.profile_avatar_change)
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ProfileAvatar(
                    // Ism yozilayotganda bosh harf darrov ko'rinadi — rasm tanlanmasa ham.
                    name = listOf(state.firstName, state.lastName)
                        .filter { it.isNotBlank() }
                        .joinToString(" "),
                    size = 96.dp,
                    fontSize = 36.sp,
                    palette = palette,
                    avatarUrl = state.avatarUrl,
                    localPreview = preview,
                    modifier = Modifier.clickable(enabled = !state.avatarUploading) { imagePicker.pick() },
                )
                Box(
                    Modifier.size(30.dp).clip(AppRadius.pill).background(palette.primary)
                        .clickable(enabled = !state.avatarUploading) { imagePicker.pick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcons.Camera,
                        changePhotoLabel,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            Text(
                if (state.avatarUploading) {
                    stringResource(Res.string.profile_avatar_uploading)
                } else {
                    changePhotoLabel
                },
                style = AppType.fieldLabel.copy(
                    color = if (state.avatarUploading) palette.inkMuted else palette.primary,
                ),
                modifier = Modifier.clickable(enabled = !state.avatarUploading) { imagePicker.pick() },
            )
        }
        Spacer(Modifier.height(AppSpacing.xl))

        FieldLabel(stringResource(Res.string.auth_first_name))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.firstName,
            onValueChange = vm::onFirstNameChange,
            placeholder = stringResource(Res.string.auth_first_name),
            leading = AppIcons.Pencil,
            type = AppFieldType.LatinText,
        )
        Spacer(Modifier.height(13.dp))

        FieldLabel(stringResource(Res.string.auth_last_name))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.lastName,
            onValueChange = vm::onLastNameChange,
            placeholder = stringResource(Res.string.auth_last_name),
            leading = AppIcons.Pencil,
            type = AppFieldType.LatinText,
        )
        Spacer(Modifier.height(13.dp))

        FieldLabel(stringResource(Res.string.auth_field_email_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = stringResource(Res.string.auth_email_placeholder),
            leading = AppIcons.Mail,
            type = AppFieldType.Email,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        HintText(stringResource(Res.string.auth_account_setup_email_hint))

        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(
            text = if (state.isLoading) {
                stringResource(Res.string.profile_saving)
            } else {
                stringResource(Res.string.auth_continue)
            },
            onClick = onContinue,
            enabled = state.accountSetupReady,
            trailingIcon = AppIcons.ArrowRight,
        )

        ErrorText(state.error)

        Spacer(Modifier.height(AppSpacing.md))
        HintText(stringResource(Res.string.auth_account_setup_hint))
        Spacer(Modifier.height(AppSpacing.xl))
    }
}
