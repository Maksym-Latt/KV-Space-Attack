package com.chicken.spaceattack.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.ui.components.CircleIconButton
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton

@Composable
fun MainMenuScreen(
        audioController: AudioController,
        viewModel: MenuViewModel,
        onStart: () -> Unit,
        onOpenUpgrades: () -> Unit
) {
    val upgradeState by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    brush =
                                            Brush.verticalGradient(
                                                    listOf(
                                                            MaterialTheme.colorScheme.background,
                                                            MaterialTheme.colorScheme.surface
                                                    )
                                            )
                            )
                            .padding(24.dp)
    ) {
        // Settings button in top-left corner
        CircleIconButton(
                icon = R.drawable.ic_launcher_foreground,
                onClick = { showSettings = true },
                modifier = Modifier.align(Alignment.TopStart)
        )

        Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
            ) { CoinBadge(coins = upgradeState.coins) }
            Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.padding(top = 12.dp, bottom = 18.dp).size(220.dp)
            )
            PrimaryButton(text = "Play", modifier = Modifier.fillMaxWidth(0.8f)) {
                audioController.playGameMusic()
                onStart()
            }

            Spacer(modifier = Modifier.height(6.dp))

            PrimaryButton(
                    text = "Upgrades",
                    modifier = Modifier.fillMaxWidth(0.8f),
                    onClick = onOpenUpgrades
            )
        }

        // Settings overlay
        if (showSettings) {
            SettingsOverlay(audioController = audioController, onClose = { showSettings = false })
        }
    }
}

@Composable
fun CoinBadge(coins: Int) {
    Box(
            modifier =
                    Modifier.shadow(6.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
            )
            OutlinedText(
                    text = coins.toString(),
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
fun UpgradeCard(
        title: String,
        description: String,
        level: Int,
        cost: Int,
        canAfford: Boolean,
        onUpgrade: () -> Unit,
        modifier: Modifier = Modifier
) {
    Column(
            modifier =
                    modifier.padding(6.dp)
                            .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedText(text = title, style = MaterialTheme.typography.displaySmall)
        OutlinedText(text = "Level $level", style = MaterialTheme.typography.bodyLarge)
        OutlinedText(text = description, style = MaterialTheme.typography.bodyMedium)

        val isMaxLevel = level >= 3
        val buttonText = if (isMaxLevel) "Maxed" else "Upgrade ($cost)"
        PrimaryButton(
                text = buttonText,
                onClick = { if (!isMaxLevel && canAfford) onUpgrade },
                modifier = Modifier.padding(top = 8.dp)
        )
    }
}

fun shieldDurationForLevel(level: Int): Int =
        when (level) {
            1 -> 5
            2 -> 7
            else -> 9
        }

fun nuclearShotsForLevel(level: Int): Int =
        when (level) {
            1 -> 2
            2 -> 3
            else -> 5
        }

fun lightningDurationForLevel(level: Int): Int =
        when (level) {
            1 -> 6
            2 -> 8
            else -> 10
        }

fun slowTimeDurationForLevel(level: Int): Int =
        when (level) {
            1 -> 5
            2 -> 7
            else -> 9
        }

@Composable
private fun SettingsOverlay(audioController: AudioController, onClose: () -> Unit) {
    Box(
            modifier = Modifier.fillMaxSize().background(Color(0x99000000)),
            contentAlignment = Alignment.Center
    ) {
        Column(
                modifier =
                        Modifier.clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedText(text = "SETTINGS", style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Music toggle
            Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedText(text = "Music", style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(
                        text = if (audioController.isMusicEnabled) "ON" else "OFF",
                        onClick = { audioController.toggleMusic() },
                        modifier = Modifier.width(80.dp)
                )
            }

            // Sound toggle
            Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedText(text = "Sound", style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(
                        text = if (audioController.isSoundEnabled) "ON" else "OFF",
                        onClick = { audioController.toggleSound() },
                        modifier = Modifier.width(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                    text = "Back",
                    background = MaterialTheme.colorScheme.secondary,
                    onClick = onClose,
                    modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
