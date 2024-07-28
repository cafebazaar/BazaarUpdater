package com.farsitel.bazaar.updater

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log

object BazaarUpdater {

    private val TAG: String = "UpdateCheck"

    private var connection: UpdateServiceConnection? = null
    fun getLastUpdateVersion(activity: Activity, onResult: (Long) -> Unit) {
        initService(activity, onResult)
    }

    private fun initService(activity: Activity, onResult: (Long) -> Unit) {
        connection = UpdateServiceConnection(onResult = onResult)
        val i = Intent(BAZAAR_UPDATE_INTENT)
        i.setPackage(BAZAAR_PACKAGE_NAME)
        try {
            activity.bindService(i, requireNotNull(connection), Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.d(TAG, "error: ${e.message}")
        }
    }

    /** This is our function to un-binds this activity from our service.  */
    private fun releaseService(activity: Activity) {
        connection?.let { activity.unbindService(it) }
        connection = null
        Log.d(TAG, "releaseService(): unbound.")
    }
}