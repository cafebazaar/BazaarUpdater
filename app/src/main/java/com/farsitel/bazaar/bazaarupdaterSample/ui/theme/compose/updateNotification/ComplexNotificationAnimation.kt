package com.farsitel.bazaar.bazaarupdaterSample.ui.theme.compose.updateNotification

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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

    // Trigger the notification animation
    LaunchedEffect(Unit) {
        showNotification = true
        delay(2000) // Show for 2 seconds
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

    val offsetY by animateDpAsState(
        targetValue = if (showNotification && !isReturning) 72.dp else 72.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val offsetX by animateDpAsState(
        targetValue = if (showNotification && !isReturning) -120.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, delayMillis = 300)
    )

    val textOffsetX by animateDpAsState(
        targetValue = if (showNotification && !isReturning) 40.dp else 0.dp, // Offset for text to appear from behind
        animationSpec = tween(durationMillis = 300, delayMillis = 300)
    )

    // Animate the background size of the text
    val textBackgroundWidth by animateDpAsState(
        targetValue = if (showNotification && !isReturning) 120.dp else 0.dp, // Background width starts small, expands, then retracts
        animationSpec = tween(durationMillis = 300, delayMillis = 300)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp)
            .wrapContentSize(align = Alignment.TopCenter)
            .clickable {
                showNotification = true // Restart the animation on click
                isReturning = false
                onClickNotificationUpdater.invoke()
            }
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .size(circleSize)
                .background(Color(0xFF006400), shape = CircleShape) // Darker Green color
                .animateContentSize(animationSpec = tween(durationMillis = 300)),
            contentAlignment = Alignment.Center
        ) {}

        if (circleSize > 0.dp) {
            Box(
                modifier = Modifier
                    .offset(x = textOffsetX)
                    .width(textBackgroundWidth)
                    .background(Color.White, shape = RectangleShape) // White background behind the text
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = Color.Black, // Black color for text
                    fontSize = 16.sp
                )
            }
        }
    }
}