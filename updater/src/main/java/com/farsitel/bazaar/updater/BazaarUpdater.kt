package com.farsitel.bazaar.updater

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.farsitel.bazaar.updater.Security.getBazaarVersionCode
import com.farsitel.bazaar.updater.Security.verifyBazaarIsInstalled
import com.farsitel.bazaar.updater.VersionParser.parseUpdateResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.lang.ref.WeakReference

public object BazaarUpdater {

    private var connection: WeakReference<UpdateServiceConnection>? = null

    public fun getLastUpdateState(
        context: Context,
        scope: CoroutineScope = retrieveScope(context),
        onResult: (UpdateResult) -> Unit
    ) {
        if (verifyBazaarIsInstalled(context).not()) {
            onResult(UpdateResult.Error(BazaarIsNotInstalledException()))
        } else {
            initService(
                context = context,
                onResult = onResult,
                scope = scope
            )
        }
    }

    public fun updateApplication(context: Context) {
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun retrieveScope(
        context: Context,
    ): CoroutineScope {
        return if (context is LifecycleOwner) {
            context.lifecycleScope
        } else {
            GlobalScope
        }
    }

    private fun initService(
        context: Context,
        scope: CoroutineScope,
        onResult: (UpdateResult) -> Unit
    ) {
        connection = WeakReference(
            UpdateServiceConnection(
                packageName = context.packageName,
                scope = scope,
                bazaarVersionCode = getBazaarVersionCode(context),
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