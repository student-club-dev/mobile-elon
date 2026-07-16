package dev.core.common.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

/**
 * iOS internet holati — `NWPathMonitor` (Network framework).
 * Monitor fon navbatida ishlaydi va holat o'zgarganда [_online] ni yangilaydi.
 */
actual class NetworkConnectivity {

    private val _online = MutableStateFlow(true)
    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("dev.elonuz.connectivity", null)

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            _online.value = nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }

    actual fun isOnline(): Boolean = _online.value

    actual val online: Flow<Boolean> = _online.asStateFlow()
}
