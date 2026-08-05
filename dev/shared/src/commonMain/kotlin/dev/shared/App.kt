package dev.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import dev.core.uikit.locale.applyAppLanguage
import dev.core.uikit.theme.AppTheme
import dev.core.domain.model.AppLanguage
import dev.core.domain.model.ThemeMode
import dev.core.domain.repository.SettingsRepository
import dev.feature.auth.presentation.flow.AuthNavHost
import dev.core.uikit.theme.appPalette
import org.koin.compose.koinInject

/**
 * Android kirish nuqtasi (ElonUzActivity) — to'g'ridan-to'g'ri biznes login oqimi + BusinessShell.
 * ElonUz butunlay biznes ilovasi, shuning uchun rol tanlash yo'q.
 */
@Composable
fun BusinessApp(onExit: () -> Unit) {
    AppScaffold { AuthNavHost(onExit = onExit) }
}

/**
 * iOS kirish nuqtasi (MainViewController) — Android bilan BIR XIL biznes oqimini ochadi.
 * `onExit` yo'q (iOS'da tashqi router yo'q) — logout ichki navigatsiya bilan biznes login'ga qaytadi.
 */
@Composable
fun App() {
    AppScaffold { AuthNavHost() }
}

/** Umumiy ildiz sozlamasi (rasm yuklovchi, seed, mavzu, inset) — hamma kirish nuqtalari ishlatadi. */
@Composable
private fun AppScaffold(content: @Composable () -> Unit) {
    // Rasmlar (e'lon rasmlari, avatarlar) — Coil ularni URL'dan TO'G'RIDAN-TO'G'RI oladi.
    // `KtorNetworkFetcherFactory()` (klientsiz) — Coil O'ZINING ichki `HttpClient`idan
    // foydalanadi: ilova klientiga umuman bog'lanmaydi, shuning uchun Bearer token, `/v1/`
    // baza va Chucker YO'Q. (iym-native-business ham shunday — rasm backend API orqali o'tmaydi.)
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    // Foydalanuvchi tanlagan mavzu (Sozlamalar). SYSTEM bo'lsa qurilma rejimiga ergashadi.
    val settings = koinInject<SettingsRepository>()
    val themeMode by settings.observeThemeMode().collectAsState(initial = ThemeMode.Default)
    val isDark = themeMode == ThemeMode.DARK

    // Foydalanuvchi tanlagan til. Tanlov platformaning joriy tiliga yoziladi, chunki
    // `stringResource` aynan shundan o'qiydi (qarang `applyAppLanguage` izohi).
    val language by settings.observeLanguage().collectAsState(initial = AppLanguage.Default)

    // MUHIM — tartib: tilni `content()` YARATILISHIDAN OLDIN qo'llash shart.
    // `LaunchedEffect` kompozitsiyadan KEYIN ishga tushadi, `key(language)` esa subtree'ni
    // kompozitsiya vaqtida darhol qayta yaratadi. Shu sabab `LaunchedEffect` bilan platforma
    // tili doim bir qadam orqada qolib, `stringResource` oldingi tilni o'qirdi — ru/en
    // tanlovlari almashib ko'rinardi. `remember(language)` esa kompozitsiya oqimida, aynan
    // shu nuqtada (quyidagi `content()` dan oldin) sinxron bajariladi va idempotent — til
    // subtree yaratilishidan oldin yoziladi. Qaytgan qiymat faqat remember kalitini ushlaydi.
    remember(language) { applyAppLanguage(language.tag); language }

    AppTheme(darkTheme = isDark) {
        // Butun ilova pastki tizim navigatsiya paneli (3 tugma) / iOS home indikatori
        // ortida qolmasligi uchun global inset. Fon gradienti panel ostida ham to'liq chiziladi.
        Box(Modifier.fillMaxSize().background(appPalette.bgBrush)) {
            Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
                // `key(language)` — til o'zgarganda butun daraxt qaytadan yaratiladi.
                // `stringResource` tanlangan tilni `remember` ichida keshlaydi, shuning uchun
                // faqat platforma tilini yozish yetarli emas: kesh bekor qilinishi kerak.
                key(language) { content() }
            }
        }
    }
}
