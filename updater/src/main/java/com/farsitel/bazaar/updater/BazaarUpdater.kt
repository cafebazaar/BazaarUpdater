package com.farsitel.bazaar.updater

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.farsitel.bazaar.updater.VersionParser.parseUpdateResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.lang.ref.WeakReference

public object BazaarUpdater {

    private var connection: WeakReference<UpdateServiceConnection>? = null

    @JvmStatic
    public fun getLastUpdateState(
        context: Context,
        listener: OnUpdateResult,
    ) {
        getLastUpdateState(
            context = context,
            scope = retrieveScope(context),
            listener = listener,
        )
    }

    @JvmSynthetic
    public fun getLastUpdateState(
        context: Context,
        scope: CoroutineScope,
        listener: OnUpdateResult,
    ) {
        if (verifyBazaarIsInstalled(context).not()) {
            listener.onResult(UpdateResult.Error(BazaarIsNotInstalledException()))
        } else {
            initService(
                context = context,
                scope = scope,
                listener = listener,
            )
        }
    }

    @JvmStatic
    public fun updateApplication(context: Context) {
        val intent = if (verifyBazaarIsInstalled(context).not()) {
            Intent(Intent.ACTION_VIEW, "$BAZAAR_WEB_APP_DETAIL${context.packageName}".toUri())
        } else {
            Intent(
                Intent.ACTION_VIEW,
                "$BAZAAR_THIRD_PARTY_APP_DETAIL${context.packageName}".toUri(),
            ).apply {
                setPackage(BAZAAR_PACKAGE_NAME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        context.startActivity(intent)
    }

    /**
     * Tells which of the three update situations this application is in: the installed
     * version is the newest one Bazaar knows, a newer version exists, or a newer
     * version was already downloaded and only its install is left, for example because
     * the device refused the silent install that the Bazaar scheduler attempted.
     *
     * When it answers [NewVersionState.NEW_VERSION_DOWNLOADED], [installDownloadedUpdate]
     * is the way to finish that update.
     */
    @JvmStatic
    public fun getNewVersionState(
        context: Context,
        listener: OnNewVersionStateResult,
    ) {
        getNewVersionState(
            context = context,
            scope = retrieveScope(context),
            listener = listener,
        )
    }

    @JvmSynthetic
    public fun getNewVersionState(
        context: Context,
        scope: CoroutineScope,
        listener: OnNewVersionStateResult,
    ) {
        if (verifyBazaarIsInstalled(context).not()) {
            listener.onResult(NewVersionStateResult.Error(BazaarIsNotInstalledException()))
        } else if (isNewVersionStateSupported(context).not()) {
            listener.onResult(NewVersionStateResult.Error(newVersionStateNotSupported()))
        } else {
            initNewVersionStateService(
                context = context,
                scope = scope,
                listener = listener,
            )
        }
    }

    /**
     * Opens Bazaar's install flow for the update it has already downloaded for this
     * application, so the user only sees the system install dialog on top of your
     * screen. Nothing is downloaded again: Bazaar installs the file it is holding.
     *
     * Call it from a foreground screen when [getNewVersionState] answers
     * [NewVersionState.NEW_VERSION_DOWNLOADED]. On a device where Bazaar is not allowed
     * to install apps yet, Bazaar asks for that permission in this same flow and
     * finishes the install once it is granted.
     */
    @JvmStatic
    public fun installDownloadedUpdate(context: Context) {
        openDownloadedUpdateInstall(context = context)
    }

    /**
     * Same as [installDownloadedUpdate], but it waits for Bazaar to fetch the update's
     * install metadata before any Bazaar screen is opened. The install dialog is only
     * shown when that request succeeds, and [listener] is told about the failure
     * otherwise, for example when there is no connection or the server answers with an
     * error.
     */
    @JvmStatic
    public fun installDownloadedUpdate(
        context: Context,
        listener: OnInstallDownloadedUpdateResult,
    ) {
        if (verifyBazaarIsInstalled(context).not()) {
            listener.onResult(
                InstallDownloadedUpdateResult.Error(BazaarIsNotInstalledException()),
            )
        } else if (isNewVersionStateSupported(context).not()) {
            listener.onResult(InstallDownloadedUpdateResult.Error(newVersionStateNotSupported()))
        } else {
            initPrepareInstallConnection(context = context, listener = listener)
        }
    }

    private fun initPrepareInstallConnection(
        context: Context,
        listener: OnInstallDownloadedUpdateResult,
    ) {
        val connection = PrepareInstallServiceConnection(
            context = context,
            packageName = context.packageName,
            scope = retrieveScope(context),
            onPrepared = {
                openDownloadedUpdateInstall(context = context, isPrepared = true)
                listener.onResult(InstallDownloadedUpdateResult.Success)
            },
            onError = { throwable ->
                listener.onResult(InstallDownloadedUpdateResult.Error(throwable))
            },
        )
        if (connection.bind().not()) {
            listener.onResult(InstallDownloadedUpdateResult.Error(UnknownException()))
        }
    }

    private fun openDownloadedUpdateInstall(context: Context, isPrepared: Boolean = false) {
        val preparedParameter = if (isPrepared) "&$PREPARED_QUERY" else ""
        val intent = Intent(
            Intent.ACTION_VIEW,
            "$BAZAAR_THIRD_PARTY_INSTALL_DOWNLOADED_UPDATE${context.packageName}$preparedParameter"
                .toUri(),
        ).apply {
            setPackage(BAZAAR_PACKAGE_NAME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            // Installed Bazaar version does not support this deep link yet.
        }
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

    private fun initNewVersionStateService(
        context: Context,
        scope: CoroutineScope,
        listener: OnNewVersionStateResult,
    ) {
        lateinit var con: NewVersionStateServiceConnection
        con = NewVersionStateServiceConnection(
            packageName = context.packageName,
            scope = scope,
            onResult = { state ->
                listener.onResult(NewVersionStateResult.Result(state))
                releaseNewVersionStateService(context, con)
            },
            onError = { throwable ->
                listener.onResult(NewVersionStateResult.Error(throwable))
                releaseNewVersionStateService(context, con)
            },
        )
        if (bindUpdateCheckService(context, con).not()) {
            listener.onResult(NewVersionStateResult.Error(UnknownException()))
            releaseNewVersionStateService(context, con)
        }
    }

    private fun bindUpdateCheckService(
        context: Context,
        con: NewVersionStateServiceConnection,
    ): Boolean {
        val intent = Intent(BAZAAR_UPDATE_INTENT)
        intent.setPackage(BAZAAR_PACKAGE_NAME)
        return try {
            val isBound = context.bindService(intent, con, Context.BIND_AUTO_CREATE)
            // From here on the connection is registered with the system and
            // must be released exactly once via unbindService.
            synchronized(this) { con.isBound = isBound }
            isBound
        } catch (ignored: Exception) {
            false
        }
    }

    /**
     * Un-binds the given connection. Safe to call more than once for the same or
     * overlapping connections: each connection is unbound exactly once, and a
     * stale/never-registered connection is a no-op.
     */
    private fun releaseNewVersionStateService(
        context: Context,
        con: NewVersionStateServiceConnection,
    ) {
        val shouldUnbind = synchronized(this) {
            val wasBound = con.isBound
            con.isBound = false
            wasBound
        }
        if (shouldUnbind) {
            try {
                context.unbindService(con)
            } catch (ignored: IllegalArgumentException) {
                // Already unbound, or the bind never fully registered.
            }
        }
    }

    private fun isNewVersionStateSupported(context: Context): Boolean {
        return getBazaarVersionCode(context) >= BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED
    }

    private fun newVersionStateNotSupported(): Throwable {
        return BazaarIsNotUpdate(
            "The new-version state is supported in bazaar version" +
                " $BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED and above",
        )
    }

    private fun initService(
        context: Context,
        scope: CoroutineScope,
        listener: OnUpdateResult,
    ) {
        connection = WeakReference(
            UpdateServiceConnection(
                packageName = context.packageName,
                scope = scope,
                bazaarVersionCode = getBazaarVersionCode(context),
                onResult = { targetVersion ->
                    listener.onResult(
                        parseUpdateResponse(
                            version = targetVersion,
                            context = context,
                        ),
                    )
                    releaseService(context)
                },
                onError = { message ->
                    listener.onResult(UpdateResult.Error(message))
                    releaseService(context)
                },
            ),
        )

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
