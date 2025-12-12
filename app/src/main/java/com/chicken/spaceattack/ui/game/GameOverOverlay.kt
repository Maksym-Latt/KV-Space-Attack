package com.chicken.spaceattack.ui.game

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.ui.components.ButtonType
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton

import kotlin.math.roundToInt

@Composable
fun GameOverOverlay(
    won: Boolean,
    score: Int,
    onRetry: () -> Unit,
    onMenu: () -> Unit
) {
    val title = if (won) "EGG-CELLENT!" else "GAME OVER!"
    val coinsEarned = (score / 10f).roundToInt()
    val chickenRes = if (won) R.drawable.player_game else R.drawable.player_lose
    val titleRes = if (won) R.drawable.title_break else R.drawable.title_game_over

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xe6000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(2f))
            Image(
                painter = painterResource(id = titleRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(1f),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = chickenRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .graphicsLayer {
                        rotationZ = 45f
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedText(
                    text = "+ $coinsEarned",
                    fontSize = 22.sp,
                    fill = Color.Yellow
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Try again",
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onRetry
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrimaryButton(
                text = "Main Menu",
                modifier = Modifier
                    .fillMaxWidth(),
                type = ButtonType.RED,
                onClick = onMenu
            )
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}
