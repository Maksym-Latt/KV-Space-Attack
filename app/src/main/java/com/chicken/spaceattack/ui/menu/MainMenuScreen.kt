package com.chicken.spaceattack.ui.menu

import android.R.attr.text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.ui.components.BalanceBubble
import com.chicken.spaceattack.ui.components.CapsuleIconButton
import com.chicken.spaceattack.ui.components.CoinBadge
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton

@Composable
fun MainMenuScreen(
    coins: Int,
    onPlay: () -> Unit,
    onShop: () -> Unit,
    onTrophies: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        BalanceBubble(
            coins = coins,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            Image(
                painter = painterResource(id = R.drawable.menu_chicken),
                contentDescription = null,
                modifier = Modifier
                    .width(260.dp)
                    .height(140.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            PrimaryButton(
                text = "PLAY",
                modifier = Modifier
                    .fillMaxWidth(0.7f),
                onClick = onPlay
            )

            Spacer(modifier = Modifier.height(80.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                CapsuleIconButton(
                    icon = R.drawable.ic_settings,
                    onClick = onShop
                )
                CapsuleIconButton(
                    icon = R.drawable.ic_trophy,
                    onClick = onTrophies
                )
                CapsuleIconButton(
                    icon = R.drawable.ic_bag,
                    onClick = onInventory
                )
            }
        }
    }
}