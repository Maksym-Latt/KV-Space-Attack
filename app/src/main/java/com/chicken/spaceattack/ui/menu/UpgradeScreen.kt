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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton

@Composable
fun UpgradeScreen(viewModel: MenuViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                CoinBadge(coins = state.coins)
            }

            OutlinedText(
                text = "Upgrades",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp)
            )

            UpgradeCard(
                title = "Shield Booster",
                description = "Duration ${shieldDurationForLevel(state.shieldLevel)}s",
                level = state.shieldLevel,
                cost = state.shieldLevel * 120,
                canAfford = state.coins >= state.shieldLevel * 120,
                onUpgrade = viewModel::upgradeShield,
                modifier = Modifier.fillMaxWidth()
            )

            UpgradeCard(
                title = "Nuclear Shots",
                description = "Shots x${nuclearShotsForLevel(state.nuclearLevel)}",
                level = state.nuclearLevel,
                cost = state.nuclearLevel * 120,
                canAfford = state.coins >= state.nuclearLevel * 120,
                onUpgrade = viewModel::upgradeNuclear,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
