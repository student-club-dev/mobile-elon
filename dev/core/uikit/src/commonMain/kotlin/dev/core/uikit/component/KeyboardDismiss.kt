package dev.core.uikit.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Klaviaturani yopib, matn maydonidan fokusni oladi.
 *
 * Tanlash elementlari (viloyat, biznes turi, kategoriya) uchun kerak: foydalanuvchi nom
 * yozayotib ro'yxatni ochsa, klaviatura ekranning yarmini egallab turadi va tanlanadigan
 * variantlar uning ostida qolib ketadi.
 *
 * Faqat `hide()` yetarli emas — fokus maydonda qolsa, tizim klaviaturani darrov qaytaradi.
 * Shuning uchun fokus ham tozalanadi.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberKeyboardDismiss(): () -> Unit {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    return remember(keyboard, focusManager) {
        {
            keyboard?.hide()
            focusManager.clearFocus()
        }
    }
}
