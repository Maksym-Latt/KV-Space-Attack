package com.chicken.spaceattack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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
