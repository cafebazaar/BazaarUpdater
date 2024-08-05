package com.farsitel.bazaar.updater

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.farsitel.bazaar.IUpdateCheckService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class UpdateServiceConnection(
    private val packageName: String,
    private val scope: CoroutineScope,
    private val bazaarVersionCode: Long,
    private val onResult: ((Long) -> Unit),
    private val onError: ((String) -> Unit),
) : ServiceConnection {

    private var service: IUpdateCheckService? = null

    override fun onServiceConnected(name: ComponentName?, boundService: IBinder?) {
        service = IUpdateCheckService.Stub.asInterface(boundService)
        try {
            scope.launch(Dispatchers.IO) {
                val versionCode = if (bazaarVersionCode >= BAZAAR_CODE_REMOTE_VERSION_SUPPORTED) {
                    service?.getRemoteVersionCode(packageName)
                } else {
                    service?.getVersionCode(packageName)
                }
                versionCode?.let { onResult(it) }
            }
        } catch (e: Exception) {
            onError(e.message.orEmpty())
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }
}