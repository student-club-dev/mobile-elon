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
 * `profileModule` va `discountsModule` har doim backend bilan ishlaydi; javob kelmasa
 * har biri o'z zaxirasiga tushadi (Firestore faqat ilovaga kirish uchun qoladi).
 */
fun appModules() = coreModules() +
    authFeatureModule +
    profileModule() +
    discountsModule()

/** Umumiy Koin start (androidApp shu yerga androidContext qo'shadi). */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(appModules())
    }
