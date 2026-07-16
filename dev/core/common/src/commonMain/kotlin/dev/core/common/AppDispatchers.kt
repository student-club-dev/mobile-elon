package dev.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Platformaga bog'liq bo'lmagan dispatcher provayderi (test uchun almashtirsa bo'ladi). */
interface AppDispatchers {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

class DefaultAppDispatchers : AppDispatchers {
    override val io: CoroutineDispatcher get() = ioDispatcher()
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val main: CoroutineDispatcher get() = Dispatchers.Main
}

/** iOS da Dispatchers.IO yo'q — har platforma o'zini beradi. */
expect fun ioDispatcher(): CoroutineDispatcher
