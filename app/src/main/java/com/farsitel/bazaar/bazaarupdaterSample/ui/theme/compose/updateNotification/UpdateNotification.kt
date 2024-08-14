package com.farsitel.bazaar.bazaarupdaterSample.ui.theme.compose.updateNotification

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun NotificationAnimation(
    onClickNotificationUpdater: () -> Unit
) {
    var showNotification by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val circleSize by animateDpAsState(
        targetValue = if (showNotification || isClicked) 120.dp else 40.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val alpha by animateFloatAsState(
        targetValue = if (showNotification || isClicked) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(Unit) {
        showNotification = true
        delay(2000) // Show for 2 seconds
        showNotification = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp)
            .wrapContentSize(align = Alignment.TopCenter)
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .background(Color.Cyan, shape = CircleShape)
                .alpha(alpha)
                .clickable {
                    if (!isClicked) {
                        isClicked = true
                    } else {
                        isClicked = false
                        onClickNotificationUpdater.invoke()
                    }
                }
                .animateContentSize(animationSpec = tween(durationMillis = 300)),
            contentAlignment = Alignment.Center
        ) {
            if (circleSize > 40.dp) {
                Text(
                    text = "HI",
                    color = Color.White,
                    fontSize = 16.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Update Icon",
                    tint = Color.White
                )
            }
        }
    }
}