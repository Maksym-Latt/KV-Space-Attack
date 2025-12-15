package com.chicken.spaceattack.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.ui.components.ButtonType
import com.chicken.spaceattack.ui.components.StrokeLabel
import com.chicken.spaceattack.ui.components.PrimaryButton

private data class LeaderboardEntry(
    val name: String,
    val coins: Int,
    val isPlayer: Boolean = false
)

@Composable
fun LeaderboardOverlay(
    playerCoins: Int,
    onClose: () -> Unit
) {
    val baseEntries = listOf(
        LeaderboardEntry("Captain C.", 1295),
        LeaderboardEntry("Nova N.", 720),
        LeaderboardEntry("Cosmo C.", 480),
        LeaderboardEntry("Galaxy G.", 36),
        LeaderboardEntry("Orbit O.", 160),
        LeaderboardEntry("Meteor M.", 80),
        LeaderboardEntry("Rocket R.", 20)
    )

    val leaderboard = remember(playerCoins) {
        (baseEntries + LeaderboardEntry(name = "YOU", coins = playerCoins, isPlayer = true))
            .sortedByDescending { it.coins }
    }

    val playerPosition = leaderboard.indexOfFirst { it.isPlayer }.let { if (it == -1) "-" else "#${it + 1}" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFFB6671A))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StrokeLabel(
                    text = "LEADERBOARD",
                    fill = Color.White,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Divider(
                    color = Color.White.copy(alpha = 0.55f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF27326A),
                                    Color(0xFF1A2146)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        leaderboard.forEachIndexed { index, entry ->
                            val isPlayer = entry.isPlayer

                            val rowBrush =
                                if (isPlayer) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xff4eb64e),
                                            Color(0xff00d50b)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFFD54F),
                                            Color(0xFFFFA000)
                                        )
                                    )
                                }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(rowBrush)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StrokeLabel(
                                    text = "#${index + 1}",
                                    fill = Color.White,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                StrokeLabel(
                                    text = entry.name,
                                    fill = Color.White,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StrokeLabel(
                                        text = entry.coins.toString(),
                                        fill = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                StrokeLabel(
                    text = "YOUR POSITION: $playerPosition",
                    fill = Color.White,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = "CLOSE",
                    modifier = Modifier
                        .fillMaxWidth(0.7f),
                    onClick = onClose,
                    type = ButtonType.RED,
                    fontSize = 22.sp
                )
            }
        }
    }
}
