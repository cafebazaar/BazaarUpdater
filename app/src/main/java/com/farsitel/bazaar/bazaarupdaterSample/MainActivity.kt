package com.farsitel.bazaar.bazaarupdaterSample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarUpdaterSampleTheme
import com.farsitel.bazaar.updater.BazaarAutoUpdater
import com.farsitel.bazaar.updater.BazaarUpdater

class MainActivity : ComponentActivity() {

    private val updateState = mutableStateOf(UpdateState())

    override fun onResume() {
        super.onResume()
        checkAutoUpdateState()
        checkNewVersionState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BazaarUpdaterSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UpdateScreen(
                        updateState = updateState,
                        modifier = Modifier.padding(innerPadding),
                        onUpdateClick = ::updateApplication,
                        onAutoUpdateClick = ::enableAutoUpdate,
                        onCheckVersionClick = ::checkUpdateState,
                        onInstallDownloadedUpdateClick = ::installDownloadedUpdate,
                    )
                }
            }
        }
    }

    private fun updateApplication() {
        BazaarUpdater.updateApplication(context = this)
    }

    private fun enableAutoUpdate() {
        BazaarAutoUpdater.enableAutoUpdate(context = this)
    }

    private fun checkAutoUpdateState() {
        BazaarAutoUpdater.getLastAutoUpdateState(context = this) { result ->
            updateState.value = updateState.value.copy(autoUpdateResult = result)
        }
    }

    private fun checkNewVersionState() {
        BazaarUpdater.getNewVersionState(context = this) { result ->
            updateState.value = updateState.value.copy(newVersionStateResult = result)
        }
    }

    private fun installDownloadedUpdate() {
        updateState.value = updateState.value.copy(isInstallingDownloadedUpdate = true)
        BazaarUpdater.installDownloadedUpdate(context = this) { result ->
            updateState.value = updateState.value.copy(isInstallingDownloadedUpdate = false)
            result.getError()?.let { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUpdateState() {
        updateState.value = updateState.value.copy(isCheckingUpdateState = true)
        BazaarUpdater.getLastUpdateState(context = this) { result ->
            updateState.value = updateState.value.copy(
                updateResult = result,
                isCheckingUpdateState = false,
            )
        }
    }
}
