package uz.elonuz

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dev.core.di.initKoin
import dev.core.network.OkHttpInterceptors
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/** ElonUz — biznes egalari uchun alohida ilova. Koin shu yerda ishga tushadi. */
class ElonUzApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Chucker — Debug HTTP inspektori. Klient qurilishidan OLDIN ro'yxatga qo'shiladi.
        // Debug'да: har API so'rovi bildirishnomaда ko'rinadi, bosganда alohida ekran ochadi.
        // Release'да: chucker-noop tufayli hech narsa qilmaydi.
        OkHttpInterceptors.interceptors += ChuckerInterceptor.Builder(this).build()

        initKoin {
            androidLogger()
            androidContext(this@ElonUzApp)
        }
    }
}
