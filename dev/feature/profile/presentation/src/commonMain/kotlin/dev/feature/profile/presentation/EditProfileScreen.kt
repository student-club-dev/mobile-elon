package dev.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.University
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.InlineErrorText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.media.rememberImagePicker
import dev.core.uikit.media.toImageBitmapOrNull
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_save
import dev.core.uikit.resources.profile_avatar_change
import dev.core.uikit.resources.profile_avatar_uploading
import dev.core.uikit.resources.profile_course_1
import dev.core.uikit.resources.profile_course_2
import dev.core.uikit.resources.profile_course_3
import dev.core.uikit.resources.profile_course_4
import dev.core.uikit.resources.profile_course_master
import dev.core.uikit.resources.profile_field_course
import dev.core.uikit.resources.profile_field_first_name
import dev.core.uikit.resources.profile_field_last_name
import dev.core.uikit.resources.profile_field_phone
import dev.core.uikit.resources.profile_field_phone_placeholder
import dev.core.uikit.resources.profile_field_university
import dev.core.uikit.resources.profile_saving
import dev.core.uikit.resources.profile_university_select
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.cardShadow
import dev.core.uikit.theme.rowShadow
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.presentation.components.ProfileAvatar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.util.UZ_DIALING_CODE
import dev.core.uikit.util.nationalPhoneDigits
import dev.core.uikit.util.fullUzPhoneOrNull

/** Kurs tanlash varianti — yorlig'i resursdan olinadi. */
private data class CourseOption(val value: String, val label: StringResource)

private val courseOptions = listOf(
    CourseOption("1", Res.string.profile_course_1),
    CourseOption("2", Res.string.profile_course_2),
    CourseOption("3", Res.string.profile_course_3),
    CourseOption("4", Res.string.profile_course_4),
    CourseOption("MASTER", Res.string.profile_course_master),
)

/**
 * Profilni tahrirlash ekrani (A2/C2). Local keshdagi profilni prefill qiladi,
 * o'zgarishlarni [ProfileViewModel.saveProfile] orqali backend + local keshga yozadi.
 */
@Composable
fun EditProfileScreen(onBack: () -> Unit, vm: ProfileViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile

    var firstName by remember(profile) { mutableStateOf(profile?.firstName.orEmpty()) }
    var lastName by remember(profile) { mutableStateOf(profile?.lastName.orEmpty()) }
    var phone by remember(profile) { mutableStateOf(nationalPhoneDigits(profile?.phoneNumber)) }
    var universityId by remember(profile) { mutableStateOf(profile?.universityId) }
    var courseYear by remember(profile) { mutableStateOf(profile?.courseYear) }

    var uniExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Avatar: tanlangan rasm darrov ko'rinadi, ayni paytda fon rejimida serverga yuklanadi.
    var avatarPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var avatarUploading by remember { mutableStateOf(false) }
    var avatarError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberImagePicker { picked ->
        if (picked == null) return@rememberImagePicker // bekor qilindi
        avatarError = null
        avatarPreview = picked.bytes.toImageBitmapOrNull()
        avatarUploading = true
        vm.uploadAvatar(picked.bytes, picked.fileName) { err ->
            avatarUploading = false
            if (err != null) {
                avatarError = err
                avatarPreview = null // yuklanmadi — eski rasmga qaytamiz
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            BackButton(
                onClick = onBack,
                contentDescription = stringResource(Res.string.common_back),
                palette = palette,
            )
            Text(
                // Topbarda foydalanuvchining ISMI turadi — "Profilni tahrirlash" degan
                // umumiy sarlavha emas. Nima tahrirlanayotgani maydonlardan aniq.
                state.name,
                style = AppType.topBarTitle.copy(color = palette.ink),
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(AppSpacing.xl))

        // Avatar — bosilganda galereya ochiladi
        val changePhotoLabel = stringResource(Res.string.profile_avatar_change)
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ProfileAvatar(
                    name = state.name,
                    size = 96.dp,
                    fontSize = 36.sp,
                    palette = palette,
                    avatarUrl = profile?.avatarUrl,
                    localPreview = avatarPreview,
                    modifier = Modifier.clickable(enabled = !avatarUploading) { imagePicker.pick() },
                )
                // Kamera nishoni — brend rangli doira ustida, shuning uchun ikonka doim oq.
                Box(
                    Modifier.size(30.dp).clip(AppRadius.pill).background(palette.primary)
                        .clickable(enabled = !avatarUploading) { imagePicker.pick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Camera, changePhotoLabel, tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }

            when {
                avatarUploading -> Text(
                    stringResource(Res.string.profile_avatar_uploading),
                    style = AppType.fieldLabel.copy(fontWeight = AppType.bodyStrong.fontWeight, color = palette.inkMuted),
                )
                avatarError != null -> InlineErrorText(avatarError!!, palette = palette)
                else -> Text(
                    changePhotoLabel,
                    style = AppType.fieldLabel.copy(color = palette.primary),
                    modifier = Modifier.clickable { imagePicker.pick() },
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        Column(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val firstNameLabel = stringResource(Res.string.profile_field_first_name)
            FieldLabel(firstNameLabel, palette = palette)
            GlassTextField(firstName, { firstName = it }, firstNameLabel, leading = AppIcons.Pencil, type = AppFieldType.LatinText)

            val lastNameLabel = stringResource(Res.string.profile_field_last_name)
            FieldLabel(lastNameLabel, palette = palette)
            GlassTextField(lastName, { lastName = it }, lastNameLabel, leading = AppIcons.Pencil, type = AppFieldType.LatinText)

            FieldLabel(stringResource(Res.string.profile_field_phone), palette = palette)
            GlassTextField(
                phone,
                { phone = it },
                stringResource(Res.string.profile_field_phone_placeholder),
                leadingContent = { Text(UZ_DIALING_CODE, style = AppType.bodyStrong.copy(color = palette.ink)) },
                type = AppFieldType.UzPhone,
            )

            // Universitet tanlash
            FieldLabel(stringResource(Res.string.profile_field_university), palette = palette)
            val selectedUni = state.universities.firstOrNull { it.id == universityId }
            Row(
                // Maydon — oq yuza + yumshoq soya, chegara yo'q.
                Modifier.fillMaxWidth().rowShadow(AppRadius.lg).clip(AppRadius.lg)
                    .background(palette.fieldBg)
                    .clickable { uniExpanded = !uniExpanded }
                    .padding(horizontal = AppSpacing.md, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AppIcons.GraduationCap, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(9.dp))
                Text(
                    selectedUni?.name ?: stringResource(Res.string.profile_university_select),
                    style = AppType.bodyStrong.copy(
                        color = if (selectedUni != null) palette.ink else palette.inkFaint,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Icon(AppIcons.ChevronDown, null, tint = palette.inkFaint, modifier = Modifier.size(18.dp))
            }
            if (uniExpanded) {
                Column(
                    Modifier.fillMaxWidth().cardShadow(AppRadius.lg).clip(AppRadius.lg)
                        .background(palette.card),
                ) {
                    state.universities.forEach { uni ->
                        UniversityRow(uni, selected = uni.id == universityId, palette = palette) {
                            universityId = uni.id
                            uniExpanded = false
                        }
                    }
                }
            }

            // Kurs tanlash
            FieldLabel(stringResource(Res.string.profile_field_course), palette = palette)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                courseOptions.forEach { opt ->
                    val active = opt.value == courseYear
                    Box(
                        // Tanlangani ochiq ko'k aksent fonda; chegara o'rniga soya.
                        Modifier.weight(1f).height(42.dp).rowShadow(AppRadius.md).clip(AppRadius.md)
                            .background(if (active) palette.accentBg else palette.card)
                            .clickable { courseYear = opt.value },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(opt.label),
                            style = AppType.hint.copy(
                                fontWeight = AppType.fieldLabel.fontWeight,
                                color = if (active) palette.primary else palette.inkMuted,
                            ),
                        )
                    }
                }
            }

            error?.let { InlineErrorText(it, palette = palette) }

            Spacer(Modifier.height(AppSpacing.xs))
            PrimaryButton(
                text = if (saving) stringResource(Res.string.profile_saving) else stringResource(Res.string.common_save),
                enabled = !saving,
                onClick = {
                    error = null
                    saving = true
                    val updated = (profile ?: UserProfile()).copy(
                        firstName = firstName.trim().ifBlank { null },
                        lastName = lastName.trim().ifBlank { null },
                        phoneNumber = fullUzPhoneOrNull(phone),
                        universityId = universityId,
                        courseYear = courseYear,
                    )
                    vm.saveProfile(updated) { err ->
                        saving = false
                        if (err == null) onBack() else error = err
                    }
                },
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun UniversityRow(uni: University, selected: Boolean, palette: AppPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            uni.monogram,
            style = AppType.fieldLabel.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.primary),
            modifier = Modifier.width(48.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(uni.name, style = AppType.label.copy(color = palette.ink))
            Text(uni.city, style = AppType.caption.copy(color = palette.inkFaint))
        }
        if (selected) Icon(AppIcons.ShieldCheck, null, tint = palette.primary, modifier = Modifier.size(17.dp))
    }
}
