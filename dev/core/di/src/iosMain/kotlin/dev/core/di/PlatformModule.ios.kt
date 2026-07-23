package dev.core.di

import dev.core.common.auth.SecureStorage
import dev.core.common.network.NetworkConnectivity
import dev.core.data.auth.createSecureStorage
import dev.core.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DriverFactory() }
    single { NetworkConnectivity() }
    // Sessiya tokenlari uchun xavfsiz ombor (Keychain).
    single<SecureStorage> { createSecureStorage() }
}
