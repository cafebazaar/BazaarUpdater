package com.farsitel.bazaar.bazaarupdaterSample.ui.theme.compose.updateNotification

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ComplexNotificationAnimation(
    onClickNotificationUpdater: () -> Unit,
    text: String = "Hello",
) {
    var showNotification by remember { mutableStateOf(false) }
    var isReturning by remember { mutableStateOf(false) }
    var triggerAnimation by remember { mutableStateOf(0) } // Key to trigger animation restart

    // Trigger the notification animation
    LaunchedEffect(triggerAnimation) {
        showNotification = true
        delay(3_000) // Show for 2 seconds
        isReturning = true
        delay(1000)
        showNotification = false
        isReturning = false
    }

    // Animation for size, vertical and horizontal movement
    val circleSize by animateDpAsState(
        targetValue = if (showNotification) 40.dp else 40.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val circleOffsetX by animateDpAsState(
        targetValue = if (showNotification && !isReturning) -20.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, delayMillis = 300)
    )

    val textBackgroundWidth by animateDpAsState(
        targetValue = if (showNotification && !isReturning) 160.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, delayMillis = 300)
    )

    // Animation for fading out the text background
    val textBackgroundAlpha by animateFloatAsState(
        targetValue = if (isReturning) 0f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp)
            .wrapContentSize(align = Alignment.TopCenter)
            .clickable {
                // Trigger animation restart and invoke callback
                triggerAnimation += 1 // Change the key to restart animation
                showNotification = true
                isReturning = false
                onClickNotificationUpdater.invoke()
            }
    ) {
        // Text background box
        Box(
            modifier = Modifier
                .offset(x = circleOffsetX + circleSize / 2) // Ensure text background starts where circle is
                .width(textBackgroundWidth)
                .alpha(textBackgroundAlpha) // Fade the text background
                .background(Color.White, shape = RoundedCornerShape(20.dp)) // Rounded corners
                .padding(start = 40.dp, end = 8.dp) // Ensure padding to fit the circle
                .height(40.dp), // Ensure the height matches the circle
            contentAlignment = Alignment.CenterStart // Align text to start to avoid overlap
        ) {
            Text(
                text = text,
                color = Color.Black, // Black color for text
                fontSize = 16.sp
            )
        }

        // Circle box
        Box(
            modifier = Modifier
                .offset(x = circleOffsetX)
                .size(circleSize)
                .background(Color(0xFF0E960E), shape = CircleShape) // Dark Green color
                .animateContentSize(animationSpec = tween(durationMillis = 300)),
            contentAlignment = Alignment.Center
        ) {
            // Info icon in the middle of the circle
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Info Icon",
                tint = Color.White,
                modifier = Modifier.size(24.dp) // Adjust size as needed
            )
        }
    }
}