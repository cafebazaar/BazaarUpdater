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

object BazaarUpdater {

    private var updateConnection: WeakReference<UpdateServiceConnection>? = null
    private var autoUpdateConnection: WeakReference<AutoUpdateServiceConnection>? = null

    fun getLastUpdateState(
        context: Context,
        scope: CoroutineScope = retrieveScope(context),
        onResult: (UpdateResult) -> Unit
    ) {
        if (verifyBazaarIsInstalled(context).not()) {
            onResult(UpdateResult.Error(BazaarIsNotInstalledException()))
        } else {
            initUpdateService(
                context = context,
                onResult = onResult,
                scope = scope
            )
        }
    }

    fun getAutoUpdateState(
        context: Context,
        scope: CoroutineScope = retrieveScope(context),
        onResult: (AutoUpdateResult) -> Unit
    ) {
        if (verifyBazaarIsInstalled(context).not()) {
            onResult(AutoUpdateResult.Error(BazaarIsNotInstalledException()))
        } else {
            initAutoUpdateService(
                context = context,
                onResult = onResult,
                scope = scope
            )
        }
    }

    fun enableAutoUpdate(context: Context) {
        if (verifyBazaarIsInstalled(context).not()) {
            return
        } else {
            val intent = Intent(
                Intent.ACTION_VIEW, "$BAZAAR_THIRD_PARTY_APP_DETAIL${context.packageName}".toUri()
            ).apply {
                setPackage(BAZAAR_PACKAGE_NAME)
            }
            context.startActivity(intent)
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

    private fun initUpdateService(
        context: Context,
        scope: CoroutineScope,
        onResult: (UpdateResult) -> Unit
    ) {
        updateConnection = WeakReference(
            UpdateServiceConnection(
                packageName = context.packageName,
                scope = scope,
                bazaarVersionCode = getBazaarVersionCode(context),
                onResult = { updateVersion ->
                    onResult(parseUpdateResponse(version = updateVersion, context = context))
                    releaseUpdateService(context)
                },
                onError = { message ->
                    onResult(UpdateResult.Error(message))
                    releaseUpdateService(context)
                }
            ))

        val intent = Intent(BAZAAR_UPDATE_INTENT)
        intent.setPackage(BAZAAR_PACKAGE_NAME)
        try {
            updateConnection?.get()?.let { con ->
                context.bindService(intent, con, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            releaseUpdateService(context)
        }
    }

    private fun initAutoUpdateService(
        context: Context,
        scope: CoroutineScope,
        onResult: (AutoUpdateResult) -> Unit
    ) {
        autoUpdateConnection = WeakReference(
            AutoUpdateServiceConnection(
                packageName = context.packageName,
                scope = scope,
                onResult = { isEnable ->
                    onResult(AutoUpdateResult.Success(isEnable))
                    releaseAutoUpdateService(context)
                },
                onError = { message ->
                    onResult(AutoUpdateResult.Error(message))
                    releaseUpdateService(context)
                }
            ))

        val intent = Intent(BAZAAR_AUTO_UPDATE_INTENT)
        intent.setPackage(BAZAAR_PACKAGE_NAME)
        try {
            autoUpdateConnection?.get()?.let { con ->
                context.bindService(intent, con, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            releaseAutoUpdateService(context)
        }
    }

    /** This is our function to un-binds this activity from our service.  */
    private fun releaseUpdateService(context: Context) {
        updateConnection?.get()?.let { con -> context.unbindService(con) }
        updateConnection = null
    }

    private fun releaseAutoUpdateService(context: Context) {
        autoUpdateConnection?.get()?.let { con -> context.unbindService(con) }
        autoUpdateConnection = null
    }
}