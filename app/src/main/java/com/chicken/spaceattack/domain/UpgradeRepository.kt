package com.chicken.spaceattack.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UpgradeRepository {
    private val shieldDurations = listOf(5_000L, 7_000L, 9_000L)
    private val nuclearShotBundles = listOf(2, 3, 5)
    private val baseCost = 120

    private val _state = MutableStateFlow(UpgradeState())
    val state: StateFlow<UpgradeState> = _state.asStateFlow()

    fun currentState(): UpgradeState = _state.value

    fun shieldDurationMillis(): Long = shieldDurations[_state.value.shieldLevel - 1]

    fun nuclearShots(): Int = nuclearShotBundles[_state.value.nuclearLevel - 1]

    fun upgradeShield() {
        updateIfPossible(
            canUpgrade = _state.value.shieldLevel < shieldDurations.size,
            cost = upgradeCost(_state.value.shieldLevel),
        ) { state ->
            state.copy(shieldLevel = state.shieldLevel + 1)
        }
    }

    fun upgradeNuclear() {
        updateIfPossible(
            canUpgrade = _state.value.nuclearLevel < nuclearShotBundles.size,
            cost = upgradeCost(_state.value.nuclearLevel),
        ) { state ->
            state.copy(nuclearLevel = state.nuclearLevel + 1)
        }
    }

    private fun upgradeCost(currentLevel: Int): Int = baseCost * currentLevel

    private inline fun updateIfPossible(canUpgrade: Boolean, cost: Int, block: (UpgradeState) -> UpgradeState) {
        if (!canUpgrade) return
        val current = _state.value
        if (current.coins < cost) return
        _state.value = block(current.copy(coins = current.coins - cost))
    }
}

data class UpgradeState(
    val coins: Int = 320,
    val shieldLevel: Int = 1,
    val nuclearLevel: Int = 1
)
