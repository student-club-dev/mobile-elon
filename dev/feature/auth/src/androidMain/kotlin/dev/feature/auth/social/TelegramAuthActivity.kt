package dev.feature.auth.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.CompletableDeferred

/**
 * Telegram web-login oqimini boshqaradi: Custom Tab ochadi va
 * `studentclubs://telegram?token=...` deep-link'i orqali qaytgan custom token'ni kutadi.
 */
internal object TelegramAuthLauncher {

    private var pending: CompletableDeferred<String?>? = null

    /** Custom Tab ochadi va token (yoki bekor qilinsa null) qaytaradi. */
    suspend fun launch(context: Context, url: String): String? {
        // Oldingi kutilayotgan so'rov bo'lsa bekor qilamiz
        pending?.complete(null)
        val deferred = CompletableDeferred<String?>()
        pending = deferred

        val intent = CustomTabsIntent.Builder().build()
        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.launchUrl(context, Uri.parse(url))

        return deferred.await()
    }

    /** Redirect Activity'dan chaqiriladi. */
    fun onRedirect(token: String?) {
        pending?.complete(token)
        pending = null
    }
}

/**
 * `studentclubs://telegram` deep-link'ini ushlaydi. Custom Tab custom token bilan
 * shu manzilga qaytadi; Activity token'ni [TelegramAuthLauncher] ga uzatib yopiladi.
 */
class TelegramAuthActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent?.data?.getQueryParameter("token")
        TelegramAuthLauncher.onRedirect(token)
        finish()
    }
}
