package com.chicken.spaceattack.ui.splash

import android.R.attr.scaleX
import android.R.attr.scaleY
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.ui.components.OutlinedText
import kotlinx.coroutines.delay
@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200)
        onFinished()
    }

    val pulse = rememberInfiniteTransition(label = "loading_pulse")
        .animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_anim"
        ).value

    val dots = listOf("", ".", "..", "...")
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(350)
            index = (index + 1) % dots.size
        }
    }

    val loadText = "LOADING" + dots[index]

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 50.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.8f),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                        rotationZ = -8f
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x33FFFFFF),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.player_game),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp)
                )
            }

            OutlinedText(
                text = loadText,
                fontSize = 28.sp,
                fill = Color(0xFFFFC107)
            )
        }
    }
}
