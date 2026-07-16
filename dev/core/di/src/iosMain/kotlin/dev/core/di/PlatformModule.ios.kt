package dev.core.di

import dev.core.common.network.NetworkConnectivity
import dev.core.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DriverFactory() }
    single { NetworkConnectivity() }
}
