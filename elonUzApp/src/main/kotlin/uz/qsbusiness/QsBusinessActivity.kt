package uz.qsbusiness

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import dev.shared.BusinessApp

/**
 * QS Business yagona Activity'si — to'g'ridan-to'g'ri biznes login oqimi + BusinessShell'ni ochadi.
 * Rol tanlash yo'q (bu butunlay biznes ilovasi). Chiqishда (logout) Activity qayta ishga
 * tushadi va login ekraniga qaytadi.
 */
class QsBusinessActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BusinessApp(onExit = ::recreate) }
    }
}
