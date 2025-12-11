package com.chicken.spaceattack.ui.menu

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton

@Composable
fun SettingsOverlay(audioController: AudioController, onClose: () -> Unit) {
    var musicEnabled by remember { mutableStateOf(audioController.isMusicEnabled) }
    var soundEnabled by remember { mutableStateOf(audioController.isSoundEnabled) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedText(text = "SETTINGS", style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedText(text = "Music", style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(
                    text = if (musicEnabled) "ON" else "OFF",
                    onClick = {
                        audioController.toggleMusic()
                        musicEnabled = audioController.isMusicEnabled
                    },
                    modifier = Modifier.width(80.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedText(text = "Sound", style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(
                    text = if (soundEnabled) "ON" else "OFF",
                    onClick = {
                        audioController.toggleSound()
                        soundEnabled = audioController.isSoundEnabled
                    },
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
