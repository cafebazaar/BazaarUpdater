package com.farsitel.bazaar.updater

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.farsitel.bazaar.IUpdateCheckService

class UpdateServiceConnection(private val onResult: ((Long) -> Unit)) : ServiceConnection {

    private var service: IUpdateCheckService? = null
    private val TAG: String = "UpdateCheck"

    override fun onServiceConnected(name: ComponentName?, boundService: IBinder?) {
        Log.i(TAG, "onServiceConnected() start")
        service = IUpdateCheckService.Stub.asInterface(boundService)
        try {
            val versionCode = service?.getVersionCode("com.farsitel.bazaar.bazaarupdaterSample")
            versionCode?.let { onResult(it) }
            Log.i(TAG, "onServiceConnected(): vCode = $versionCode")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(TAG, "onServiceConnected(): Connected")
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        Log.d(TAG, "onServiceConnected(): onServiceDisconnected")
    }
}