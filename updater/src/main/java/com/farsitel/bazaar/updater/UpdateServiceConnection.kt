package com.farsitel.bazaar.updater

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.farsitel.bazaar.IUpdateCheckService

class UpdateServiceConnection(
    private val packageName: String,
    private val onResult: ((Long) -> Unit),
    private val onError: ((String) -> Unit),
) : ServiceConnection {

    private var service: IUpdateCheckService? = null

    override fun onServiceConnected(name: ComponentName?, boundService: IBinder?) {
        service = IUpdateCheckService.Stub.asInterface(boundService)
        try {
            val versionCode = service?.getVersionCode(packageName)
            versionCode?.let { onResult(it) }
        } catch (e: Exception) {
            onError(e.message.orEmpty())
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }
}