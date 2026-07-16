package dev.core.common.network

import kotlinx.coroutines.flow.Flow

/**
 * Platformaga bog'liq internet holati tekshiruvchisi.
 *
 * - [isOnline] — hozir onlaynmi (bir martalik, repository so'rovdan oldin tekshiradi).
 * - [online] — holat oqimi (real-time; UI "internet yo'q" banner'ini ko'rsatishi mumkin).
 *
 * Android: `ConnectivityManager` (VALIDATED capability). iOS: `NWPathMonitor`.
 */
expect class NetworkConnectivity {
    fun isOnline(): Boolean
    val online: Flow<Boolean>
}
