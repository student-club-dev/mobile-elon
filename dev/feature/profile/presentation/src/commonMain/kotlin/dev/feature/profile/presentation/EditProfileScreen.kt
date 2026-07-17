package dev.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.University
import dev.feature.profile.domain.model.UserProfile
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.profile.presentation.components.ProfileAvatar
import dev.core.designsystem.media.rememberImagePicker
import dev.core.designsystem.media.toImageBitmapOrNull
import org.koin.compose.viewmodel.koinViewModel

private data class CourseOption(val value: String, val label: String)

private val courseOptions = listOf(
    CourseOption("1", "1-kurs"),
    CourseOption("2", "2-kurs"),
    CourseOption("3", "3-kurs"),
    CourseOption("4", "4-kurs"),
    CourseOption("MASTER", "Magistr"),
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
    var phone by remember(profile) { mutableStateOf(profile?.phoneNumber.orEmpty()) }
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
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconBoxLocal(AppIcons.ArrowLeft, palette, onBack)
            Text("Profilni tahrirlash", style = TextStyle(fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Black, color = palette.ink))
        }
        Spacer(Modifier.height(18.dp))

        // Avatar — bosilganda galereya ochiladi
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                // Kamera nishoni
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(palette.primary)
                        .clickable(enabled = !avatarUploading) { imagePicker.pick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Camera, "Rasmni o'zgartirish", tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }

            when {
                avatarUploading -> Text(
                    "Rasm yuklanmoqda...",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.inkMuted),
                )
                avatarError != null -> Text(
                    avatarError!!,
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626)),
                )
                else -> Text(
                    "Rasmni o'zgartirish",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.primary),
                    modifier = Modifier.clickable { imagePicker.pick() },
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FieldLabel("Ism", palette)
            GlassTextField(firstName, { firstName = it }, "Ism", leading = AppIcons.Pencil)

            FieldLabel("Familiya", palette)
            GlassTextField(lastName, { lastName = it }, "Familiya", leading = AppIcons.Pencil)

            FieldLabel("Telefon", palette)
            GlassTextField(
                phone, { phone = it }, "+998 90 123 45 67",
                leading = AppIcons.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )

            // Universitet tanlash
            FieldLabel("Universitet", palette)
            val selectedUni = state.universities.firstOrNull { it.id == universityId }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.fieldBg)
                    .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                    .clickable { uniExpanded = !uniExpanded }.padding(horizontal = 12.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AppIcons.GraduationCap, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(9.dp))
                Text(
                    selectedUni?.name ?: "Universitetni tanlang",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (selectedUni != null) palette.ink else palette.inkFaint),
                    modifier = Modifier.weight(1f),
                )
                Icon(AppIcons.ChevronDown, null, tint = palette.inkFaint, modifier = Modifier.size(18.dp))
            }
            if (uniExpanded) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(14.dp)),
                ) {
                    state.universities.forEach { uni ->
                        UniversityRow(uni, selected = uni.id == universityId, palette) {
                            universityId = uni.id
                            uniExpanded = false
                        }
                    }
                }
            }

            // Kurs tanlash
            FieldLabel("Kurs", palette)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                courseOptions.forEach { opt ->
                    val active = opt.value == courseYear
                    Box(
                        Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (active) palette.primary.copy(alpha = 0.14f) else palette.glass)
                            .border(1.dp, if (active) palette.primary else palette.border, RoundedCornerShape(12.dp))
                            .clickable { courseYear = opt.value },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(opt.label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = if (active) palette.primary else palette.inkMuted))
                    }
                }
            }

            if (error != null) {
                Text(error!!, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626)))
            }

            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = if (saving) "Saqlanmoqda..." else "Saqlash",
                enabled = !saving,
                onClick = {
                    error = null
                    saving = true
                    val updated = (profile ?: UserProfile()).copy(
                        firstName = firstName.trim().ifBlank { null },
                        lastName = lastName.trim().ifBlank { null },
                        phoneNumber = phone.trim().ifBlank { null },
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
private fun FieldLabel(text: String, palette: AppPalette) {
    Text(text, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.inkMuted))
}

@Composable
private fun UniversityRow(uni: University, selected: Boolean, palette: AppPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(uni.monogram, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = palette.primary), modifier = Modifier.width(48.dp))
        Column(Modifier.weight(1f)) {
            Text(uni.name, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.ink))
            Text(uni.city, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = palette.inkFaint))
        }
        if (selected) Icon(AppIcons.ShieldCheck, null, tint = palette.primary, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun IconBoxLocal(icon: androidx.compose.ui.graphics.vector.ImageVector, palette: AppPalette, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, "Orqaga", tint = palette.ink, modifier = Modifier.size(18.dp)) }
}
