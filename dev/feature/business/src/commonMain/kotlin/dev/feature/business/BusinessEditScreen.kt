package dev.feature.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.InlineErrorText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.business_edit_title
import dev.core.uikit.resources.business_field_email
import dev.core.uikit.resources.business_field_email_hint
import dev.core.uikit.resources.business_field_name
import dev.core.uikit.resources.business_field_name_hint
import dev.core.uikit.resources.business_field_phone
import dev.core.uikit.resources.business_field_phone_hint
import dev.core.uikit.resources.business_info_type
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_save
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.presentation.ProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.util.UZ_DIALING_CODE
import dev.core.uikit.util.nationalPhoneDigits
import dev.core.uikit.util.fullUzPhoneOrNull
import androidx.compose.material3.Text
import dev.core.uikit.theme.AppType

/**
 * Biznes profilini tahrirlash — biznes nomi, telefon, email (gmail), biznes turi.
 * Saqlangач local DB + backend yangilanadi (ProfileViewModel.saveProfile).
 */
@Composable
fun BusinessEditScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile

    var name by remember(profile) { mutableStateOf(profile?.businessName ?: state.name) }
    var phone by remember(profile) { mutableStateOf(nationalPhoneDigits(profile?.phoneNumber)) }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var type by remember(profile) { mutableStateOf(profile?.businessType ?: "") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AppScreenScaffold(scroll = true, horizontalPadding = 20.dp, topPadding = 54.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
            ScreenTitle(stringResource(Res.string.business_edit_title), fontSize = 21.sp)
        }

        Spacer(Modifier.height(20.dp))
        FieldLabel(stringResource(Res.string.business_field_name))
        Spacer(Modifier.height(AppSpacing.sm))
        GlassTextField(name, { name = it }, stringResource(Res.string.business_field_name_hint), type = AppFieldType.LatinText)

        Spacer(Modifier.height(AppSpacing.lg))
        FieldLabel(stringResource(Res.string.business_field_phone))
        Spacer(Modifier.height(AppSpacing.sm))
        GlassTextField(
            phone, { phone = it }, stringResource(Res.string.business_field_phone_hint),
            leadingContent = { Text(UZ_DIALING_CODE, style = AppType.bodyStrong.copy(color = appPalette.ink)) },
            type = AppFieldType.UzPhone,
        )

        Spacer(Modifier.height(AppSpacing.lg))
        FieldLabel(stringResource(Res.string.business_field_email))
        Spacer(Modifier.height(AppSpacing.sm))
        GlassTextField(
            email, { email = it }, stringResource(Res.string.business_field_email_hint),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        Spacer(Modifier.height(AppSpacing.lg))
        FieldLabel(stringResource(Res.string.business_info_type))
        Spacer(Modifier.height(AppSpacing.sm))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(businessTypes) { t ->
                TypeChip(t, type == t.id, { type = t.id }, palette)
            }
        }

        error?.let { message ->
            Spacer(Modifier.height(AppSpacing.sm))
            InlineErrorText(message, palette = palette)
        }

        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(
            stringResource(Res.string.common_save),
            onClick = {
                saving = true
                error = null
                val updated = (profile ?: UserProfile(role = "BUSINESS")).copy(
                    businessName = name.ifBlank { null },
                    phoneNumber = fullUzPhoneOrNull(phone),
                    email = email.ifBlank { null },
                    businessType = type.ifBlank { null },
                    role = "BUSINESS",
                )
                vm.saveProfile(updated) { err ->
                    saving = false
                    if (err == null) onBack() else error = err
                }
            },
            enabled = name.isNotBlank() && !saving,
        )
    }
}
