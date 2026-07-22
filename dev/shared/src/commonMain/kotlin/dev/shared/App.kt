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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import dev.core.data.seed.LocalDataSeeder
import dev.core.uikit.theme.AppTheme
import dev.core.domain.model.ThemeMode
import dev.core.domain.repository.SettingsRepository
import dev.feature.auth.presentation.flow.AuthNavHost
import dev.feature.auth.presentation.flow.AuthUserFlow
import dev.core.uikit.theme.appPalette
import io.ktor.client.HttpClient
import org.koin.compose.koinInject

/**
 * Android kirish nuqtasi (ElonUzActivity) — to'g'ridan-to'g'ri biznes login oqimi + BusinessShell.
 * ElonUz butunlay biznes ilovasi, shuning uchun rol tanlash yo'q.
 */
@Composable
fun BusinessApp(onExit: () -> Unit) {
    AppScaffold { AuthNavHost(flow = AuthUserFlow.BUSINESS, onExit = onExit) }
}

/**
 * iOS kirish nuqtasi (MainViewController) — Android bilan BIR XIL biznes oqimini ochadi.
 * `onExit` yo'q (iOS'da tashqi router yo'q) — logout ichki navigatsiya bilan biznes login'ga qaytadi.
 */
@Composable
fun App() {
    AppScaffold { AuthNavHost(flow = AuthUserFlow.BUSINESS) }
}

/** Umumiy ildiz sozlamasi (rasm yuklovchi, seed, mavzu, inset) — hamma kirish nuqtalari ishlatadi. */
@Composable
private fun AppScaffold(content: @Composable () -> Unit) {
    // Tarmoqdan rasm yuklash (avatar) — Coil ilovaning o'z Ktor klientidan foydalanadi,
    // shunda so'rovlarga Firebase ID token ham qo'shiladi (himoyalangan rasm URL'lari uchun).
    val httpClient = koinInject<HttpClient>()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory(httpClient)) }
            .build()
    }

    // Local bazani dizayndagi namuna ma'lumot bilan to'ldiramiz (bo'sh bo'lsagina).
    val seeder = koinInject<LocalDataSeeder>()
    LaunchedEffect(Unit) { seeder.seedIfEmpty() }

    // Foydalanuvchi tanlagan mavzu (Sozlamalar). SYSTEM bo'lsa qurilma rejimiga ergashadi.
    val settings = koinInject<SettingsRepository>()
    val themeMode by settings.observeThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    AppTheme(darkTheme = isDark) {
        // Butun ilova pastki tizim navigatsiya paneli (3 tugma) / iOS home indikatori
        // ortida qolmasligi uchun global inset. Fon gradienti panel ostida ham to'liq chiziladi.
        Box(Modifier.fillMaxSize().background(appPalette.bgBrush)) {
            Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
                content()
            }
        }
    }
}
