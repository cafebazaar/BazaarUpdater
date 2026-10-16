package com.farsitel.bazaar.updater

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.farsitel.bazaar.IUpdateCheckService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Asks Bazaar to prepare the download it already holds for [packageName] and reports
 * once, on the main thread, whether that succeeded. Bazaar does the server request
 * inside the call, which is why it is made off the main thread.
 */
internal class PrepareInstallServiceConnection(
    private val context: Context,
    private val packageName: String,
    private val scope: CoroutineScope,
    private val onPrepared: () -> Unit,
    private val onError: (Throwable) -> Unit,
) : ServiceConnection {

    private val completed = AtomicBoolean(false)
    private val unbound = AtomicBoolean(false)

    @Volatile
    private var isBound: Boolean = false

    fun bind(): Boolean {
        val intent = Intent(BAZAAR_UPDATE_INTENT).setPackage(BAZAAR_PACKAGE_NAME)
        isBound = try {
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        } catch (ignored: Exception) {
            false
        }
        return isBound
    }

    override fun onServiceConnected(name: ComponentName?, boundService: IBinder?) {
        scope.launch(Dispatchers.IO) {
            val isPrepared = try {
                IUpdateCheckService.Stub.asInterface(boundService)
                    ?.prepareDownloadedUpdateInstall(packageName)
                    ?: false
            } catch (ignored: Throwable) {
                // e.g. DeadObjectException/RemoteException when the Bazaar service
                // process dies before the transaction completes.
                false
            }
            complete(isPrepared)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        complete(isPrepared = false)
    }

    override fun onBindingDied(name: ComponentName?) {
        complete(isPrepared = false)
    }

    private fun complete(isPrepared: Boolean) {
        if (completed.compareAndSet(false, true).not()) return
        unbind()
        mainThreadScope.launch {
            if (isPrepared) {
                onPrepared()
            } else {
                onError(InstallDownloadedUpdateException())
            }
        }
    }

    private fun unbind() {
        if (isBound && unbound.compareAndSet(false, true)) {
            try {
                context.unbindService(this)
            } catch (ignored: IllegalArgumentException) {
                // Already unbound, or the bind never fully registered.
            }
        }
    }
}
