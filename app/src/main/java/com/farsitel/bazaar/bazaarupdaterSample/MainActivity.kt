package com.farsitel.bazaar.bazaarupdaterSample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarupdaterSampleTheme
import com.farsitel.bazaar.updater.BazaarUpdater
import com.farsitel.bazaar.updater.doOnUpdateNeeded

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BazaarUpdater.getLastUpdateVersion(context = this) { result ->
            result.doOnUpdateNeeded { version ->
                BazaarUpdater.updateApplication(this)
            }
        }
        setContent {
            BazaarupdaterSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BazaarupdaterSampleTheme {
        Greeting("Android")
    }
}