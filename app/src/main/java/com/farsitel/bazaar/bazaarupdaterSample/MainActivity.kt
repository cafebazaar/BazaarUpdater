package com.farsitel.bazaar.bazaarupdaterSample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarUpdaterSampleTheme
import com.farsitel.bazaar.updater.BazaarUpdater
import com.farsitel.bazaar.updater.UpdateResult

class MainActivity : ComponentActivity() {

    private val updateState = mutableStateOf<UpdateResult?>(null)

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
                        onCheckVersionClick = ::checkUpdateState,
                    )
                }
            }
        }
    }

    private fun updateApplication() {
        BazaarUpdater.updateApplication(context = this)
    }

    private fun checkUpdateState() {
        BazaarUpdater.getLastUpdateState(context = this) { result ->
            updateState.value = result
        }
    }
}