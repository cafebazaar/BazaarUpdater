package com.farsitel.bazaar.updater

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import com.farsitel.bazaar.IUpdateCheckService
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
public class BazaarUpdaterTest {

    private lateinit var application: Application

    @Before
    public fun setUp() {
        application = RuntimeEnvironment.getApplication()
    }

    @Test
    public fun `successful prepare opens the prepared install window and reports success`() {
        val context = BazaarContext(prepareResult = true)
        val results = mutableListOf<InstallDownloadedUpdateResult>()
        var callbackOnMainThread = false

        BazaarUpdater.installDownloadedUpdate(context) { result ->
            callbackOnMainThread = Looper.myLooper() == Looper.getMainLooper()
            results += result
        }
        awaitResult(results)

        assertTrue(results.single().isSuccess())
        assertTrue(callbackOnMainThread)
        assertEquals(
            "$BAZAAR_THIRD_PARTY_INSTALL_DOWNLOADED_UPDATE${context.packageName}" +
                "&$PREPARED_QUERY",
            context.startedActivities.single().data.toString(),
        )
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `a window that cannot be opened is reported as an error`() {
        val context = BazaarContext(prepareResult = true, startActivityFails = true)
        val results = mutableListOf<InstallDownloadedUpdateResult>()

        BazaarUpdater.installDownloadedUpdate(context, results::add)
        awaitResult(results)

        assertTrue(results.single().getError() is InstallWindowNotOpenedException)
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `failed prepare reports error without opening any window`() {
        val context = BazaarContext(prepareResult = false)
        val results = mutableListOf<InstallDownloadedUpdateResult>()

        BazaarUpdater.installDownloadedUpdate(context, results::add)
        awaitResult(results)

        assertTrue(results.single().getError() is InstallDownloadedUpdateException)
        assertTrue(context.startedActivities.isEmpty())
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `missing Bazaar reports error without binding`() {
        val context = BazaarContext(prepareResult = true, isBazaarInstalled = false)
        val results = mutableListOf<InstallDownloadedUpdateResult>()

        BazaarUpdater.installDownloadedUpdate(context, results::add)
        awaitResult(results)

        assertTrue(results.single().getError() is BazaarIsNotInstalledException)
        assertEquals(0, context.bindCount)
        assertTrue(context.startedActivities.isEmpty())
    }

    @Test
    public fun `unsupported Bazaar version reports error without binding`() {
        val context = BazaarContext(
            prepareResult = true,
            bazaarVersionCode = BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED - 1,
        )
        val results = mutableListOf<InstallDownloadedUpdateResult>()

        BazaarUpdater.installDownloadedUpdate(context, results::add)
        awaitResult(results)

        assertTrue(results.single().getError() is BazaarIsNotUpdate)
        assertEquals(0, context.bindCount)
    }

    @Test
    public fun `fire and forget install keeps opening the plain install window`() {
        val context = BazaarContext(prepareResult = true)

        BazaarUpdater.installDownloadedUpdate(context)

        assertEquals(
            "$BAZAAR_THIRD_PARTY_INSTALL_DOWNLOADED_UPDATE${context.packageName}",
            context.startedActivities.single().data.toString(),
        )
        assertEquals(0, context.bindCount)
    }

    @Test
    public fun `each state Bazaar reports arrives on the main thread`() {
        val context = BazaarContext(state = NewVersionState.NEW_VERSION_DOWNLOADED)
        val results = mutableListOf<NewVersionStateResult>()
        var callbackOnMainThread = false

        BazaarUpdater.getNewVersionState(context) { result ->
            callbackOnMainThread = Looper.myLooper() == Looper.getMainLooper()
            results += result
        }
        awaitResult(results)

        assertEquals(NewVersionState.NEW_VERSION_DOWNLOADED, results.single().getState())
        assertTrue(callbackOnMainThread)
        assertEquals(1, context.bindCount)
        assertEquals(1, context.unbindCount)
        assertTrue(context.startedActivities.isEmpty())
    }

    @Test
    public fun `a new version that is only known arrives as available`() {
        val context = BazaarContext(state = NewVersionState.NEW_VERSION_AVAILABLE)
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)

        assertEquals(NewVersionState.NEW_VERSION_AVAILABLE, results.single().getState())
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `no new version arrives as its own state`() {
        val context = BazaarContext(state = NewVersionState.NO_NEW_VERSION)
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)

        assertEquals(NewVersionState.NO_NEW_VERSION, results.single().getState())
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `an unknown answer from Bazaar counts as unknown`() {
        val context = BazaarContext(stateValue = UNKNOWN_STATE_VALUE)
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)

        assertEquals(NewVersionState.UNKNOWN, results.single().getState())
    }

    @Test
    public fun `each wire number maps to its state`() {
        assertEquals(NewVersionState.UNKNOWN, newVersionStateOf(0))
        assertEquals(NewVersionState.NO_NEW_VERSION, newVersionStateOf(1))
        assertEquals(NewVersionState.NEW_VERSION_AVAILABLE, newVersionStateOf(2))
        assertEquals(NewVersionState.NEW_VERSION_DOWNLOADED, newVersionStateOf(3))
        assertEquals(NewVersionState.UNKNOWN, newVersionStateOf(UNKNOWN_STATE_VALUE))
    }

    @Test
    public fun `missing Bazaar answers the state check without binding`() {
        val context = BazaarContext(isBazaarInstalled = false)
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)

        assertTrue(results.single().getError() is BazaarIsNotInstalledException)
        assertEquals(NewVersionState.UNKNOWN, results.single().getState())
        assertEquals(0, context.bindCount)
    }

    @Test
    public fun `an older Bazaar answers a newer version from the legacy check`() {
        val context = BazaarContext(
            bazaarVersionCode = BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED - 1,
            legacyVersionCode = 1,
        )
        val results = mutableListOf<NewVersionStateResult>()
        var callbackOnMainThread = false

        BazaarUpdater.getNewVersionState(context) { result ->
            callbackOnMainThread = Looper.myLooper() == Looper.getMainLooper()
            results += result
        }
        awaitResult(results)

        assertEquals(NewVersionState.NEW_VERSION_AVAILABLE, results.single().getState())
        assertTrue(callbackOnMainThread)
        assertEquals(1, context.bindCount)
        assertEquals(1, context.unbindCount)
        assertEquals(1, context.legacyRemoteVersionCodeCalls)
        assertEquals(0, context.legacyVersionCodeCalls)
    }

    @Test
    public fun `an older Bazaar that is up to date answers no new version`() {
        val context = BazaarContext(
            bazaarVersionCode = BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED - 1,
            legacyVersionCode = 0,
        )
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)

        assertEquals(NewVersionState.NO_NEW_VERSION, results.single().getState())
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `an older Bazaar whose legacy check fails answers the error`() {
        val context = BazaarContext(
            bazaarVersionCode = BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED - 1,
            legacyError = true,
        )
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)

        assertTrue(results.single().getError() != null)
        assertEquals(NewVersionState.UNKNOWN, results.single().getState())
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `a Bazaar that predates the remote check asks for the installed version`() {
        val context = BazaarContext(
            bazaarVersionCode = BAZAAR_CODE_REMOTE_VERSION_SUPPORTED - 1,
            legacyVersionCode = 1,
        )
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)

        assertEquals(NewVersionState.NEW_VERSION_AVAILABLE, results.single().getState())
        assertEquals(1, context.legacyVersionCodeCalls)
        assertEquals(0, context.legacyRemoteVersionCodeCalls)
    }

    @Test
    @Suppress("DEPRECATION")
    public fun `the deprecated check still answers a newer version`() {
        val context = BazaarContext(legacyVersionCode = 1)
        val results = mutableListOf<UpdateResult>()

        BazaarUpdater.getLastUpdateState(context) { results += it }
        awaitResult(results)

        assertTrue(results.single().isUpdateNeeded())
        assertEquals(1L, results.single().getTargetVersionCode())
        assertEquals(1, context.unbindCount)
    }

    @Test
    public fun `the deprecated check is deprecated on both overloads`() {
        val staticCheck = BazaarUpdater::class.java.getMethod(
            "getLastUpdateState",
            Context::class.java,
            OnUpdateResult::class.java,
        )
        val scopedCheck = BazaarUpdater::class.java.getMethod(
            "getLastUpdateState",
            Context::class.java,
            CoroutineScope::class.java,
            OnUpdateResult::class.java,
        )

        assertTrue(staticCheck.isAnnotationPresent(Deprecated::class.java))
        assertTrue(scopedCheck.isAnnotationPresent(Deprecated::class.java))
    }

    @Test
    public fun `disconnect during the state check answers exactly once`() {
        val context = BazaarContext(disconnectAfterConnect = true)
        val results = mutableListOf<NewVersionStateResult>()

        BazaarUpdater.getNewVersionState(context, results::add)
        awaitResult(results)
        // Give the call that lost the race the chance to answer a second time.
        settleMainLooper()

        assertEquals(1, results.size)
        assertEquals(1, context.unbindCount)
    }

    private fun awaitResult(results: Collection<*>) {
        val deadline = System.currentTimeMillis() + AWAIT_TIMEOUT_MILLIS
        while (results.isEmpty() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(AWAIT_STEP_MILLIS)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun settleMainLooper() {
        shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(AWAIT_STEP_MILLIS * 5)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private inner class BazaarContext(
        private val prepareResult: Boolean = true,
        private val startActivityFails: Boolean = false,
        private val state: NewVersionState = NewVersionState.NEW_VERSION_DOWNLOADED,
        private val stateValue: Int? = null,
        private val disconnectAfterConnect: Boolean = false,
        private val legacyVersionCode: Long = 0,
        private val legacyError: Boolean = false,
        isBazaarInstalled: Boolean = true,
        bazaarVersionCode: Int = BAZAAR_CODE_UPDATE_DOWNLOADED_SUPPORTED,
    ) : ContextWrapper(application) {

        var bindCount: Int = 0
        var unbindCount: Int = 0
        var legacyVersionCodeCalls: Int = 0
        var legacyRemoteVersionCodeCalls: Int = 0
        val startedActivities = mutableListOf<Intent>()

        init {
            if (isBazaarInstalled) {
                shadowOf(packageManager).installPackage(
                    PackageInfo().apply {
                        packageName = BAZAAR_PACKAGE_NAME
                        versionCode = bazaarVersionCode
                        applicationInfo = ApplicationInfo().apply {
                            packageName = BAZAAR_PACKAGE_NAME
                        }
                    },
                )
            }
        }

        override fun bindService(
            service: Intent,
            conn: ServiceConnection,
            flags: Int,
        ): Boolean {
            bindCount++
            val componentName = ComponentName(BAZAAR_PACKAGE_NAME, "UpdateCheckService")
            Handler(Looper.getMainLooper()).post {
                conn.onServiceConnected(componentName, updateCheckService.asBinder())
            }
            if (disconnectAfterConnect) {
                Handler(Looper.getMainLooper()).post {
                    conn.onServiceDisconnected(componentName)
                }
            }
            return true
        }

        override fun unbindService(conn: ServiceConnection) {
            unbindCount++
        }

        override fun startActivity(intent: Intent) {
            if (startActivityFails) throw ActivityNotFoundException()
            startedActivities += intent
        }

        private fun legacyVersion(): Long {
            if (legacyError) throw RemoteException("the legacy check failed")
            return legacyVersionCode
        }

        private val updateCheckService: IUpdateCheckService = object : IUpdateCheckService.Stub() {
            override fun getVersionCode(packageName: String): Long {
                legacyVersionCodeCalls++
                return legacyVersion()
            }

            override fun getRemoteVersionCode(packageName: String): Long {
                legacyRemoteVersionCodeCalls++
                return legacyVersion()
            }

            override fun getNewVersionState(packageName: String): Int = stateValue ?: when (state) {
                NewVersionState.UNKNOWN -> NEW_VERSION_STATE_UNKNOWN
                NewVersionState.NEW_VERSION_AVAILABLE -> NEW_VERSION_STATE_AVAILABLE
                NewVersionState.NEW_VERSION_DOWNLOADED -> NEW_VERSION_STATE_DOWNLOADED
                NewVersionState.NO_NEW_VERSION -> NEW_VERSION_STATE_NONE
            }

            override fun prepareDownloadedUpdateInstall(packageName: String): Boolean =
                prepareResult
        }
    }

    private companion object {
        const val AWAIT_TIMEOUT_MILLIS = 5_000L
        const val AWAIT_STEP_MILLIS = 10L

        /** A state number a future Bazaar could send that this SDK does not know. */
        const val UNKNOWN_STATE_VALUE = 99
    }
}
