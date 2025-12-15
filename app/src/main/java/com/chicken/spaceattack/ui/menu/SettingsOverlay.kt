package com.chicken.spaceattack.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.audio.AudioEngine
import com.chicken.spaceattack.ui.components.ButtonType
import com.chicken.spaceattack.ui.components.PrimaryButton
import com.chicken.spaceattack.ui.game.PauseToggleButton

@Composable
fun SettingsOverlay(
    audioEngine: AudioEngine,
    onClose: () -> Unit
) {
    var musicEnabled by remember { mutableStateOf(audioEngine.isMusicEnabled) }
    var soundEnabled by remember { mutableStateOf(audioEngine.isSoundEnabled) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFFB6671A))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.title_settings),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(18.dp))

                PauseToggleButton(
                    icon = R.drawable.ic_sound,
                    text = if (soundEnabled) "Sound: On" else "Sound: Off",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    audioEngine.toggleSound()
                    soundEnabled = audioEngine.isSoundEnabled
                }

                Spacer(modifier = Modifier.height(12.dp))

                PauseToggleButton(
                    icon = R.drawable.ic_music,
                    text = if (musicEnabled) "Music: On" else "Music: Off",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    audioEngine.toggleMusic()
                    musicEnabled = audioEngine.isMusicEnabled
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Quit",
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    onClick = onClose,
                    type = ButtonType.RED,
                    fontSize = 20.sp
                )
            }
        }
    }
}