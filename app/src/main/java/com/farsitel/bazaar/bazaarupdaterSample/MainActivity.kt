package com.farsitel.bazaar.bazaarupdaterSample

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarupdaterSampleTheme
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.compose.snap.WebViewScreen
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.compose.updateNotification.ComplexNotificationAnimation
import com.farsitel.bazaar.updater.AutoUpdateResult
import com.farsitel.bazaar.updater.BazaarUpdater
import com.farsitel.bazaar.updater.UpdateResult
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        BazaarUpdater.enableAutoUpdate(this)
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
    val showButton = remember { mutableStateOf(false) }
    val updateState = remember { mutableStateOf<UpdateResult?>(null) }
    val context = LocalContext.current
    fun onResult(result: UpdateResult) {
        updateState.value = result
    }

    Box {
        WebViewScreen(onUrlMatched = { urlMatched ->
            showButton.value = urlMatched
        })

        if (showButton.value) {
            when (val state = updateState.value) {
                UpdateResult.AlreadyUpdated -> AlreadyUpdatedView()
                is UpdateResult.Error -> ErrorView(message = state.throwable.message.orEmpty())
                is UpdateResult.NeedUpdate -> NeedUpdateView(
                    targetVersion = state.targetVersion,
                    onResult = {
                        BazaarUpdater.updateApplication(context = context)
                    }
                )

                null -> checkUpdateState(
                    context = context,
                    onResult = ::onResult
                )

            }
        }

    }
}

@Composable
private fun AlreadyUpdatedView(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(modifier = modifier, text = stringResource(R.string.your_application_is_updated))
    }
}

@Composable
private fun ErrorView(message: String, modifier: Modifier = Modifier) {
    ButtonFromRightEdge(
        context = LocalContext.current,
        text = stringResource(R.string.there_is_new_update_version),
        modifier = modifier,
        onResult = { Error(message) },
    )
}

@Composable
private fun NeedUpdateView(
    targetVersion: Long,
    modifier: Modifier = Modifier,
    onResult: (UpdateResult) -> Unit = {}
) {
    val context = LocalContext.current

//    NotificationAnimation { checkUpdateState(context, onResult) }
    ComplexNotificationAnimation(
        { checkUpdateState(context, onResult) },
        stringResource(R.string.there_is_new_update_version, targetVersion)
    )
//    ButtonFromRightEdge(
//        context = LocalContext.current,
//        text = stringResource(R.string.there_is_new_update_version, targetVersion),
//        modifier = modifier,
//        onResult = onResult,
//    )
}

@Composable
private fun ButtonFromRightEdge(
    text: String,
    context: Context,
    modifier: Modifier,
    onResult: (UpdateResult) -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Url("https://assets5.lottiefiles.com/packages/lf20_FGHx4D.json"))
    var visible by remember { mutableStateOf(false) }
    var offsetY by remember { mutableStateOf(-1000.dp) } // Start from off-screen

    LaunchedEffect(Unit) {
        delay(2000)
        visible = true
        offsetY = 3.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 1000)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 1000)
            )
        ) {
            Button(
                onClick = {
                    checkUpdateState(context, onResult)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green), // Set button color to green
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = offsetY)
                    .size(60.dp)
            ) {
                Text(text = text, color = Color.White)
            }
        }

        if (visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .align(Alignment.Center)
            ) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}

@Composable
private fun UpdateButton(
    context: Context,
    text: String,
    modifier: Modifier = Modifier,
    onResult: (UpdateResult) -> Unit = {}
) {
    Button(
        shape = CircleShape,
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
    UpdateButton(
        context = context,
        onResult = onResult,
        text = stringResource(R.string.check_update)
    )
}

private fun checkUpdateState(context: Context, onResult: (UpdateResult) -> Unit) {
    BazaarUpdater.getLastUpdateState(context = context, onResult = onResult)
}

private fun checkAutoUpdateState(context: Context, onResult: (AutoUpdateResult) -> Unit) {
    BazaarUpdater.getAutoUpdateState(context = context, onResult = onResult)
}

@Preview(showBackground = true)
@Composable
fun UpdateScreenPreview() {
    BazaarupdaterSampleTheme {
        UpdateScreen()
    }
}