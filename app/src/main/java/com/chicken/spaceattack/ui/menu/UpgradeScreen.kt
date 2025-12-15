package com.chicken.spaceattack.ui.menu

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R
import com.chicken.spaceattack.ui.components.BalanceBubble
import com.chicken.spaceattack.ui.components.ButtonType
import com.chicken.spaceattack.ui.components.StrokeLabel
import com.chicken.spaceattack.ui.components.PrimaryButton


@Composable
fun UpgradeScreen(
    viewModel: MenuViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PrimaryButton(
                    text = "BACK",
                    modifier = Modifier.width(120.dp).height(50.dp),
                    onClick = onBack,
                    type = ButtonType.RED
                )

                BalanceBubble(
                    coins = state.coins
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            StrokeLabel(
                text = "UPGRADES",
                fontSize = 48.sp,
                fill = Color(0xFFFFA726),
                outline = Color.Black,
                strokeWidth = 6f,
            )

            Spacer(modifier = Modifier.height(12.dp))

            UpgradeGrid(
                items = listOf(
                    {
                        UpgradeCard(
                            title = "SHIELD",
                            description = "duration ${shieldDurationForLevel(state.shieldLevel)}s",
                            level = state.shieldLevel,
                            cost = state.shieldLevel * 120,
                            canAfford = state.coins >= state.shieldLevel * 120,
                            onUpgrade = viewModel::upgradeShield,
                            icon = R.drawable.boost_save,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    },
                    {
                        UpgradeCard(
                            title = "NUCLEAR",
                            description = "duration ${nuclearShotsForLevel(state.nuclearLevel)}s",
                            level = state.nuclearLevel,
                            cost = state.nuclearLevel * 120,
                            canAfford = state.coins >= state.nuclearLevel * 120,
                            onUpgrade = viewModel::upgradeNuclear,
                            icon = R.drawable.boost_nuclear,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    },
                    {
                        UpgradeCard(
                            title = "LIGHTNING",
                            description = "duration ${lightningDurationForLevel(state.lightningLevel)}s",
                            level = state.lightningLevel,
                            cost = state.lightningLevel * 120,
                            canAfford = state.coins >= state.lightningLevel * 120,
                            onUpgrade = viewModel::upgradeLightning,
                            icon = R.drawable.boost_lightning,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    },
                    {
                        UpgradeCard(
                            title = "SLOW TIME",
                            description = "duration ${slowTimeDurationForLevel(state.slowTimeLevel)}s",
                            level = state.slowTimeLevel,
                            cost = state.slowTimeLevel * 120,
                            canAfford = state.coins >= state.slowTimeLevel * 120,
                            onUpgrade = viewModel::upgradeSlowTime,
                            icon = R.drawable.boost_time,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun UpgradeGrid(
    items: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(items.size) { index ->
            items[index]()
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
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier
) {
    val buyColor = if (canAfford) Color(0xFF3DC13C) else Color(0xFF6A9169)

    Box(
        modifier = modifier
            .height(250.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false,
                ambientColor = Color(0xFF0A0D20),
                spotColor = Color(0xFF0A0D20)
            )
            .clip(RoundedCornerShape(26.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = Color(0xFFFFA726),
                        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                StrokeLabel(
                    text = "LEVEL $level",
                    fontSize = 22.sp,
                    fill = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFB6671A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier.size(70.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StrokeLabel(
                        text = description,
                        fontSize = 16.sp,
                        fill = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = buyColor,
                        shape = RoundedCornerShape(
                            bottomStart = 26.dp,
                            bottomEnd = 26.dp
                        )
                    )
                    .clickable(enabled = canAfford) {
                        if (canAfford) onUpgrade()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StrokeLabel(
                        text = cost.toString(),
                        fontSize = 20.sp,
                        fill = Color.White
                    )
                }
            }
        }
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
        1 -> 5
        2 -> 7
        else -> 9
    }

fun lightningDurationForLevel(level: Int): Int =
    when (level) {
        1 -> 5
        2 -> 7
        else -> 9
    }

fun slowTimeDurationForLevel(level: Int): Int =
    when (level) {
        1 -> 5
        2 -> 7
        else -> 9
    }
