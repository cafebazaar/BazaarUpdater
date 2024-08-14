package com.farsitel.bazaar.updater

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.farsitel.bazaar.IAutoUpdateCheckService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class AutoUpdateServiceConnection(
    private val packageName: String,
    private val scope: CoroutineScope,
    private val onResult: ((Boolean) -> Unit),
    private val onError: ((Throwable) -> Unit),
) : ServiceConnection {

    private var service: IAutoUpdateCheckService? = null

    override fun onServiceConnected(name: ComponentName?, boundService: IBinder?) {
        service = IAutoUpdateCheckService.Stub.asInterface(boundService)
        try {
            scope.launch(Dispatchers.IO) {
                val isEnabled = service?.isAutoUpdateEnable(packageName)
                Log.i("asdkjhasjkdbasjd", isEnabled.toString())
                onResult(isEnabled ?: false)
            }
        } catch (t: Throwable) {
            onError(t)
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }
}