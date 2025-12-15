package com.chicken.spaceattack.ui.game

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.audio.AudioEngine
import com.chicken.spaceattack.ui.components.ButtonType
import com.chicken.spaceattack.ui.components.StrokeLabel
import com.chicken.spaceattack.ui.components.PrimaryButton


@Composable
fun PauseOverlay(
    audioEngine: AudioEngine,
    onQuit: () -> Unit,
    onResume: () -> Unit
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
                    painter = painterResource(id = R.drawable.title_paused),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryButton(
                        text = "Quit",
                        modifier = Modifier.weight(1f),
                        onClick = onQuit,
                        type = ButtonType.RED,
                        fontSize = 20.sp
                    )
                    PrimaryButton(
                        text = "Resume",
                        modifier = Modifier.weight(1f),
                        onClick = onResume,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}


@Composable
fun PauseToggleButton(
    @DrawableRes icon: Int,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFA726),
                        Color(0xFFF57C00)
                    )
                )
            )
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(50),
                clip = false,
                ambientColor = Color(0xFF975500),
                spotColor = Color(0xFF975500)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 6.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x00FFFFFF),
                            Color(0x33FFFFFF)
                        )
                    ),
                    RoundedCornerShape(50)
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            StrokeLabel(
                text = text,
                fill = Color.White,
                fontSize = 22.sp
            )
        }
    }
}
