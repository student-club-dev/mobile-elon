package dev.core.di

import dev.feature.auth.di.authFeatureModule
import dev.feature.discounts.presentation.di.discountsModule
import dev.feature.profile.presentation.di.profileModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Ilovaning barcha Koin modullari. Har yangi feature o'z modulini shu yerga qo'shadi.
 *
 * `profileModule` va `discountsModule` masofaviy manbani [REMOTE_SYNC_ENABLED] ga qarab
 * tanlaydi: REST (backend bor) yoki local/Firestore (backendsiz).
 */
fun appModules() = coreModules() +
    authFeatureModule +
    profileModule(REMOTE_SYNC_ENABLED) +
    discountsModule(REMOTE_SYNC_ENABLED, USE_FIRESTORE_DISCOUNTS)

/** Umumiy Koin start (androidApp shu yerga androidContext qo'shadi). */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(appModules())
    }
