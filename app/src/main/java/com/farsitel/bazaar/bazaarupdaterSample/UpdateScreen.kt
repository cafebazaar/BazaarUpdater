package com.farsitel.bazaar.bazaarupdaterSample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarUpdaterSampleTheme
import com.farsitel.bazaar.updater.AutoUpdateState
import com.farsitel.bazaar.updater.NewVersionState
import com.farsitel.bazaar.updater.NewVersionStateResult
import com.farsitel.bazaar.updater.UpdateResult

@Composable
fun UpdateScreen(
    updateState: State<UpdateState?>,
    modifier: Modifier = Modifier,
    onUpdateClick: () -> Unit = {},
    onCheckVersionClick: () -> Unit = {},
    onAutoUpdateClick: () -> Unit = {},
    onInstallDownloadedUpdateClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = updateState.value?.updateResult) {
            UpdateResult.AlreadyUpdated -> {
                AlreadyUpdatedView()
            }

            is UpdateResult.Error -> {
                ErrorView(
                    message = state.throwable.message.orEmpty(),
                )
            }

            is UpdateResult.NeedUpdate -> {
                NeedUpdateView(
                    targetVersion = state.getTargetVersionCode(),
                    onClick = onUpdateClick,
                )
            }

            else -> {
                CheckUpdateStateView(
                    isLoading = updateState.value?.isCheckingUpdateState == true,
                    onClick = onCheckVersionClick,
                )
            }
        }

        when (val result = updateState.value?.autoUpdateResult) {
            null -> {}
            else -> when (result.getState()) {
                AutoUpdateState.ENABLED -> Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = stringResource(R.string.auto_update_enable_description),
                )
                AutoUpdateState.DISABLED -> UpdateButton(text = "Enable Autoupdate") {
                    onAutoUpdateClick()
                }
                AutoUpdateState.NOT_SUPPORTED -> ErrorView(
                    message = result.getError()?.message.orEmpty(),
                )
            }
        }

        when (val result = updateState.value?.newVersionStateResult) {
            null -> {}
            is NewVersionStateResult.Error -> ErrorView(
                message = result.getError()?.message.orEmpty(),
            )
            is NewVersionStateResult.Result -> when (result.getState()) {
                NewVersionState.NEW_VERSION_DOWNLOADED -> UpdateButton(
                    text = "Finish the downloaded update",
                    isLoading = updateState.value?.isInstallingDownloadedUpdate == true,
                ) {
                    onInstallDownloadedUpdateClick()
                }

                NewVersionState.NEW_VERSION_AVAILABLE -> UpdateButton(
                    text = "Update to the new version",
                ) {
                    onUpdateClick()
                }

                NewVersionState.NO_NEW_VERSION, null -> Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = "No new version for this app",
                )
            }
        }
    }
}

@Composable
private fun AlreadyUpdatedView(
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.your_application_is_updated),
    )
}

@Composable
private fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        text = message,
    )
}

@Composable
private fun NeedUpdateView(
    targetVersion: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    UpdateButton(
        text = stringResource(R.string.there_is_new_update_version, targetVersion),
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun CheckUpdateStateView(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
) {
    UpdateButton(
        text = stringResource(R.string.check_update),
        modifier = modifier,
        isLoading = isLoading,
        onClick = onClick,
    )
}

@Composable
private fun UpdateButton(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
) {
    Button(
        modifier = modifier,
        enabled = isLoading.not(),
        onClick = onClick,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateScreenPreview() {
    BazaarUpdaterSampleTheme {
        UpdateScreen(
            updateState = remember {
                mutableStateOf(null)
            }
        )
    }
}