package dev.feature.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.components.BackButton
import dev.core.designsystem.components.FieldLabel
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.components.ScreenTitle
import dev.core.designsystem.theme.appPalette
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.presentation.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

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
    var phone by remember(profile) { mutableStateOf(profile?.phoneNumber ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var type by remember(profile) { mutableStateOf(profile?.businessType ?: "") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AppScreenScaffold(scroll = true, horizontalPadding = 20, topPadding = 54) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(onBack)
            ScreenTitle("Profilni tahrirlash", size = 21)
        }

        Spacer(Modifier.height(20.dp))
        FieldLabel("Biznes nomi")
        Spacer(Modifier.height(8.dp))
        GlassTextField(name, { name = it }, "Masalan: Kafe Aurora", height = 50)

        Spacer(Modifier.height(16.dp))
        FieldLabel("Telefon")
        Spacer(Modifier.height(8.dp))
        GlassTextField(
            phone, { phone = it }, "+998 90 123 45 67",
            height = 50,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        Spacer(Modifier.height(16.dp))
        FieldLabel("Email (gmail)")
        Spacer(Modifier.height(8.dp))
        GlassTextField(
            email, { email = it }, "biznes@gmail.com",
            height = 50,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        Spacer(Modifier.height(16.dp))
        FieldLabel("Biznes turi")
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(businessTypes) { t ->
                TypeChip(t, type == t, { type = t }, palette)
            }
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFFE5484D)))
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            "Saqlash",
            onClick = {
                saving = true
                error = null
                val updated = (profile ?: UserProfile(role = "BUSINESS")).copy(
                    businessName = name.ifBlank { null },
                    phoneNumber = phone.ifBlank { null },
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
