package com.farsitel.bazaar.bazaarupdaterSample

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarupdaterSampleTheme
import com.farsitel.bazaar.updater.BazaarUpdater
import com.farsitel.bazaar.updater.UpdateResult

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BazaarupdaterSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UpdateScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateScreen(
    modifier: Modifier = Modifier
) {
    val updateState = remember { mutableStateOf<UpdateResult?>(null) }
    val context = LocalContext.current
    fun onResult(result: UpdateResult) {
        updateState.value = result
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = updateState.value) {
            UpdateResult.AlreadyUpdated -> AlreadyUpdatedView()
            is UpdateResult.Error -> ErrorView(message = state.message)
            is UpdateResult.NeedUpdate -> NeedUpdateView(
                targetVersion = state.targetVersion,
                onResult = {
                    BazaarUpdater.updateApplication(context = context)
                }
            )

            null -> CheckUpdateStateView(context = LocalContext.current, onResult = ::onResult)
        }
    }
}

@Composable
private fun AlreadyUpdatedView(modifier: Modifier = Modifier) {
    Text(modifier = modifier, text = stringResource(R.string.your_application_is_updated))
}

@Composable
private fun ErrorView(message: String, modifier: Modifier = Modifier) {
    Column {
        Text(modifier = modifier, text = message)
    }
}

@Composable
private fun NeedUpdateView(
    targetVersion: Long,
    modifier: Modifier = Modifier,
    onResult: (UpdateResult) -> Unit = {}
) {
    UpdateButton(
        context = LocalContext.current,
        text = stringResource(R.string.there_is_new_update_version, targetVersion),
        modifier = modifier,
        onResult = onResult,
    )
}

@Composable
private fun UpdateButton(
    context: Context,
    text: String,
    modifier: Modifier = Modifier,
    onResult: (UpdateResult) -> Unit = {}
) {
    Button(
        modifier = modifier,
        onClick = {
            checkUpdateState(
                context = context,
                onResult = onResult
            )
        }
    ) {
        Text(text = text)
    }
}

@Composable
private fun CheckUpdateStateView(context: Context, onResult: (UpdateResult) -> Unit) {
    UpdateButton(context = context, onResult = onResult, text = stringResource(R.string.check_update))
}

private fun checkUpdateState(context: Context, onResult: (UpdateResult) -> Unit) {
    BazaarUpdater.getLastUpdateState(context = context){ result->
        when(result){
            UpdateResult.AlreadyUpdated -> TODO()
            is UpdateResult.Error -> TODO()
            is UpdateResult.NeedUpdate -> result.targetVersion
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateScreenPreview() {
    BazaarupdaterSampleTheme {
        UpdateScreen()
    }
}