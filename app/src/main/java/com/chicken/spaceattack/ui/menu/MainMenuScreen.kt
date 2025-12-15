package com.chicken.spaceattack.ui.menu

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.audio.AudioEngine
import com.chicken.spaceattack.ui.components.BalanceBubble
import com.chicken.spaceattack.ui.components.CapsuleIconButton
import com.chicken.spaceattack.ui.components.PrimaryButton

@Composable
fun MainMenuScreen(
    audioEngine: AudioEngine,
    viewModel: MenuViewModel,
    onStart: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showShop by remember { mutableStateOf(false) }
    var showLeaderboard by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        ChickenWithShotsBackground(
            modifier = Modifier.fillMaxSize()
        )

        BalanceBubble(
            coins = state.coins,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "PLAY",
                modifier = Modifier.fillMaxWidth(0.8f),
                onClick = onStart
            )

            Spacer(modifier = Modifier.weight(0.2f))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(1f)
            ) {
                CapsuleIconButton(
                    icon = R.drawable.ic_settings,
                    onClick = { showSettings = true }
                )
                CapsuleIconButton(
                    icon = R.drawable.ic_trophy,
                    onClick = { showLeaderboard = true }
                )
                CapsuleIconButton(
                    icon = R.drawable.ic_bag,
                    onClick = { showShop = true }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        if (showSettings) {
            SettingsOverlay(audioEngine = audioEngine) { showSettings = false }
        }

        if (showShop) {
            UpgradeOverlay(viewModel = viewModel) { showShop = false }
        }

        if (showLeaderboard) {
            LeaderboardOverlay(playerCoins = state.coins) { showLeaderboard = false }
        }
    }
}

@Composable
private fun ChickenWithShotsBackground(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val rocketSize = 280.dp
        val rocketHorizontalOffset = 110.dp

        val transition = rememberInfiniteTransition(label = "shot")
        val shotProgress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shotProgress"
        )

        if (shotProgress < 1f) {
            val angleDeg = -30f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val distance = 660f
            val dx = (-kotlin.math.cos(angleRad).toFloat() * distance * shotProgress).dp
            val dy = (kotlin.math.sin(angleRad).toFloat() * distance * shotProgress).dp

            val baseOffsetX = rocketHorizontalOffset - 100.dp
            val baseOffsetY = 40.dp

            Image(
                painter = painterResource(id = R.drawable.regular_shot),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(
                        x = baseOffsetX + dx,
                        y = baseOffsetY + dy
                    )
                    .rotate(-60f)
                    .size(150.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = rocketHorizontalOffset)
                .rotate(30f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.menu_chicken),
                contentDescription = null,
                modifier = Modifier.size(rocketSize)
            )
        }
    }
}


@Composable
private fun UpgradeOverlay(
    viewModel: MenuViewModel,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            UpgradeScreen(viewModel = viewModel, onBack = onClose)
        }
    }
}
