package com.farsitel.bazaar.updater

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.farsitel.bazaar.updater.Security.verifyBazaarIsInstalled
import com.farsitel.bazaar.updater.VersionParser.parseUpdateResponse
import java.lang.ref.WeakReference

object BazaarUpdater {

    private var connection: WeakReference<UpdateServiceConnection>? = null

    fun getLastUpdateState(
        context: Context,
        onResult: (UpdateResult) -> Unit
    ) {
        if (verifyBazaarIsInstalled(context).not()) {
            onResult(UpdateResult.Error(ERROR_BAZAAR_IS_NOT_INSTALL))
        } else {
            initService(context, onResult)
        }
    }

    fun updateApplication(context: Context) {
        val intent = if (verifyBazaarIsInstalled(context).not()) {
            Intent(Intent.ACTION_VIEW, "$BAZAAR_WEB_APP_DETAIL${context.packageName}".toUri())
        } else {
            Intent(
                Intent.ACTION_VIEW, "$BAZAAR_THIRD_PARTY_APP_DETAIL${context.packageName}".toUri()
            ).apply {
                setPackage(BAZAAR_PACKAGE_NAME)
            }
        }
        context.startActivity(intent)
    }

    private fun initService(context: Context, onResult: (UpdateResult) -> Unit) {
        connection = WeakReference(
            UpdateServiceConnection(
                packageName = context.packageName,
                onResult = { updateVersion ->
                    onResult(parseUpdateResponse(version = updateVersion, context = context))
                    releaseService(context)
                },
                onError = { message ->
                    onResult(UpdateResult.Error(message))
                    releaseService(context)
                }
            ))

        val intent = Intent(BAZAAR_UPDATE_INTENT)
        intent.setPackage(BAZAAR_PACKAGE_NAME)
        try {
            connection?.get()?.let { con ->
                context.bindService(intent, con, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            releaseService(context)
        }
    }

    /** This is our function to un-binds this activity from our service.  */
    private fun releaseService(context: Context) {
        connection?.get()?.let { con -> context.unbindService(con) }
        connection = null
    }
}