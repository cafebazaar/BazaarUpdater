package com.farsitel.bazaar.updater

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.farsitel.bazaar.IUpdateCheckService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal class NewVersionStateServiceConnection(
    private val packageName: String,
    private val scope: CoroutineScope,
    private val onResult: (NewVersionState) -> Unit,
    private val onError: (Throwable) -> Unit,
) : ServiceConnection {

    /**
     * Tracks whether this specific connection is currently registered with the
     * system. Guarded by the lock in [BazaarUpdater] so release happens exactly
     * once per instance.
     */
    @JvmField
    internal var isBound: Boolean = false

    private val completed = AtomicBoolean(false)

    override fun onServiceConnected(name: ComponentName?, boundService: IBinder?) {
        try {
            val service = IUpdateCheckService.Stub.asInterface(boundService)
            scope.launch(Dispatchers.IO) {
                try {
                    val state = service?.getNewVersionState(packageName)
                    if (state != null) {
                        complete { onResult(newVersionStateOf(state)) }
                    } else {
                        complete { onError(UnknownException()) }
                    }
                } catch (throwable: Throwable) {
                    // e.g. DeadObjectException/RemoteException when the Bazaar
                    // service process dies before the transaction completes.
                    complete { onError(throwable) }
                }
            }
        } catch (throwable: Throwable) {
            complete { onError(throwable) }
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        complete { onError(ServiceDisconnectionException(componentName)) }
    }

    override fun onBindingDied(name: ComponentName?) {
        complete { onError(ServiceDisconnectionException(name)) }
    }

    /**
     * Answers the caller exactly once, on the main thread, whichever of the
     * connection callbacks or the remote call finishes first. The caller un-binds
     * from inside that single answer, so a later callback must stay silent.
     */
    private fun complete(callback: () -> Unit) {
        if (completed.compareAndSet(false, true).not()) return
        mainThreadScope.launch { callback() }
    }
}
