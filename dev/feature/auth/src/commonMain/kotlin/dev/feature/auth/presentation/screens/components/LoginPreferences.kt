package dev.feature.auth.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.AppLanguage
import dev.core.domain.model.ThemeMode
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.IconActionButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_theme_dark
import dev.core.uikit.resources.auth_theme_light
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.rowShadow
import dev.feature.auth.presentation.main.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Kirish ekranining tepasidagi til va mavzu tanlagichi.
 *
 * Nega kirish ekranida: standart til — inglizcha (`AppLanguage.Default`), sozlamalar esa
 * faqat hisobga kirgandan keyin ochiladi. Ya'ni ilovani birinchi marta ochgan foydalanuvchi
 * butun kirish oqimini (ro'yxat, SMS kod, parolni tiklash) tushunmaydigan tilda o'tishga
 * majbur edi va tilni almashtirishning yo'li yo'q edi.
 *
 * Tanlov [SettingsViewModel] orqali local bazaga yoziladi — ya'ni sozlamalar ekranidagi
 * bilan ayni bir joyga, va hisobga kirgandan keyin ham saqlanib qoladi.
 *
 * Til modal oynada emas, uchta kalta chipда: variant atigi uchta va ular shu yerдаyoq
 * ko'rinib turadi — bir bosishда almashadi, oyna ochib-yopish shart emas.
 */
@Composable
fun LoginPreferences(
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
    vm: SettingsViewModel = koinViewModel(),
) {
    val settings by vm.state.collectAsStateWithLifecycle()
    val dark = settings.themeMode == ThemeMode.DARK

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.rowShadow(AppRadius.pill)
                .clip(AppRadius.pill)
                .background(palette.card)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLanguage.entries.forEach { language ->
                LanguageChip(
                    language = language,
                    selected = language == settings.language,
                    palette = palette,
                    onClick = { vm.setLanguage(language) },
                )
            }
        }

        // Bitta tugma ikkala rejim uchun: ikonka QARAMA-QARSHI rejimni ko'rsatadi
        // (tungida quyosh) — u holatni emas, bosilganda nima bo'lishini bildiradi.
        IconActionButton(
            icon = if (dark) AppIcons.Sun else AppIcons.Moon,
            onClick = { vm.setThemeMode(if (dark) ThemeMode.LIGHT else ThemeMode.DARK) },
            contentDescription = stringResource(
                if (dark) Res.string.auth_theme_light else Res.string.auth_theme_dark,
            ),
            size = ChipRowHeight,
            iconSize = 18.dp,
            shape = AppRadius.pill,
            tint = palette.primary,
            palette = palette,
        )
    }
}

/**
 * Bitta til chipi — "UZ" / "RU" / "EN".
 *
 * Yorliq sifatida `AppLanguage` nomining o'zi ishlatiladi va u TARJIMA QILINMAYDI: "RU" har
 * doim "RU" bo'lib qoladi, shu sababdan tushunmaydigan tilda qolib ketgan foydalanuvchi ham
 * o'z tilini topa oladi (sozlamalar ekranidagi qoida bilan bir xil).
 */
@Composable
private fun LanguageChip(
    language: AppLanguage,
    selected: Boolean,
    palette: AppPalette,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .clip(AppRadius.pill)
            .background(if (selected) palette.accentBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            language.name,
            style = AppType.segment.copy(
                fontSize = 12.sp,
                color = if (selected) palette.primary else palette.inkFaint,
            ),
        )
    }
}

/** Til qatorining balandligi — mavzu tugmasi u bilan bir xil bo'lishi kerak. */
private val ChipRowHeight = 38.dp
