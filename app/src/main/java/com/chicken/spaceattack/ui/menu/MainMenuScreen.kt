package com.chicken.spaceattack.ui.menu

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.ui.components.BalanceBubble
import com.chicken.spaceattack.ui.components.CapsuleIconButton
import com.chicken.spaceattack.ui.components.ButtonType
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton

@Composable
fun MainMenuScreen(
    audioController: AudioController,
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
            SettingsOverlay(audioController = audioController) { showSettings = false }
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

private data class LeaderboardEntry(
    val name: String,
    val coins: Int,
    val isPlayer: Boolean = false
)

@Composable
private fun LeaderboardOverlay(
    playerCoins: Int,
    onClose: () -> Unit
) {
    val baseEntries = listOf(
        LeaderboardEntry("Captain Claw", 5400),
        LeaderboardEntry("Nova Nugget", 4200),
        LeaderboardEntry("Cosmo Chick", 3600),
        LeaderboardEntry("Galaxy Goose", 3100),
        LeaderboardEntry("Orbit Owl", 2600),
        LeaderboardEntry("Meteor Mallard", 2100),
        LeaderboardEntry("Rocket Rooster", 1800)
    )

    val leaderboard = remember(playerCoins) {
        (baseEntries + LeaderboardEntry(name = "You", coins = playerCoins, isPlayer = true))
            .sortedByDescending { it.coins }
    }

    val playerPosition = leaderboard.indexOfFirst { it.isPlayer } + 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .background(Color(0xFF14274A), shape = RoundedCornerShape(28.dp))
                .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(28.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedText(text = "LEADERBOARD", fill = Color(0xFFFFD54F))

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0x66000000), Color(0x33000000))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    leaderboard.forEachIndexed { index, entry ->
                        val rowBrush =
                            if (entry.isPlayer) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1C2F5C), Color(0xFF1A2750))
                                )
                            }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(rowBrush)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedText(text = "#${index + 1}", fill = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedText(
                                text = entry.name,
                                fill = if (entry.isPlayer) Color(0xFF0D1B35) else Color.White
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedText(
                                    text = entry.coins.toString(),
                                    fill = if (entry.isPlayer) Color(0xFF0D1B35) else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedText(
                    text = "Your position: #$playerPosition",
                    fill = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryButton(
                    text = "Close",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClose,
                    type = ButtonType.RED
                )
            }
        }
    }
}
