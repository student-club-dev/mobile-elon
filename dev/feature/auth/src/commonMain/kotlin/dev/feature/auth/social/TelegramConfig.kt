package dev.feature.auth.social

/**
 * Telegram Login sozlamalari — o'zingizning qiymatlaringiz bilan to'ldiring.
 *
 * Oqim: ilova [LOGIN_URL] ni (Firebase Hosting'dagi Telegram Login Widget sahifasi)
 * brauzer sessiyasida ochadi → foydalanuvchi Telegram'da tasdiqlaydi →
 * sahifa Cloud Function'ga ma'lumotni yuboradi, custom token oladi va
 * `${REDIRECT_SCHEME}://${REDIRECT_HOST}?token=...` deep-link'iga qaytaradi →
 * ilova token bilan Firebase'ga kiradi.
 */
object TelegramConfig {
    /** Firebase Hosting'dagi login sahifa manzili (o'zgartiring). */
    const val LOGIN_URL = "https://elonuz-5dcca.web.app/telegram-login.html"

    /** Deep-link sxemasi — Android manifest va iOS ASWebAuthenticationSession bilan mos bo'lsin. */
    const val REDIRECT_SCHEME = "studentclubs"
    const val REDIRECT_HOST = "telegram"

    /** To'liq qaytish manzili — LOGIN_URL sahifasiga `redirect_uri` sifatida uzatiladi. */
    val redirectUri: String get() = "$REDIRECT_SCHEME://$REDIRECT_HOST"

    /** Web sessiyaga uzatiladigan to'liq URL. */
    val authUrl: String get() = "$LOGIN_URL?redirect_uri=$redirectUri"
}
