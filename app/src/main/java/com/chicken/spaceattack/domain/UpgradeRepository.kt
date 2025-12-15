package com.chicken.spaceattack.domain

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.upgradeDataStore by preferencesDataStore("upgrade_prefs")

@Singleton
class UpgradeRepository @Inject constructor(@ApplicationContext context: Context) {
    private val shieldDurations = listOf(5_000L, 7_000L, 9_000L)
    private val nuclearShotBundles = listOf(2, 3, 5)
    private val lightningDurations = listOf(6_000L, 8_000L, 10_000L)
    private val slowTimeDurations = listOf(5_000L, 7_000L, 9_000L)
    private val baseCost = 120

    private val defaultState = UpgradeState()
    private val _state = MutableStateFlow(defaultState)
    val state: StateFlow<UpgradeState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore = context.upgradeDataStore

    init {
        scope.launch { loadState() }
        scope.launch { persistOnChange() }
    }

    fun currentState(): UpgradeState = _state.value

    fun shieldDurationMillis(): Long = shieldDurations[_state.value.shieldLevel - 1]

    fun nuclearShots(): Int = nuclearShotBundles[_state.value.nuclearLevel - 1]

    fun lightningDurationMillis(): Long = lightningDurations[_state.value.lightningLevel - 1]

    fun slowTimeDurationMillis(): Long = slowTimeDurations[_state.value.slowTimeLevel - 1]

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

    fun upgradeLightning() {
        updateIfPossible(
            canUpgrade = _state.value.lightningLevel < lightningDurations.size,
            cost = upgradeCost(_state.value.lightningLevel),
        ) { state ->
            state.copy(lightningLevel = state.lightningLevel + 1)
        }
    }

    fun upgradeSlowTime() {
        updateIfPossible(
            canUpgrade = _state.value.slowTimeLevel < slowTimeDurations.size,
            cost = upgradeCost(_state.value.slowTimeLevel),
        ) { state ->
            state.copy(slowTimeLevel = state.slowTimeLevel + 1)
        }
    }

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        _state.value = _state.value.copy(coins = _state.value.coins + amount)
    }

    private fun upgradeCost(currentLevel: Int): Int = baseCost * currentLevel

    private inline fun updateIfPossible(canUpgrade: Boolean, cost: Int, block: (UpgradeState) -> UpgradeState) {
        if (!canUpgrade) return
        val current = _state.value
        if (current.coins < cost) return
        _state.value = block(current.copy(coins = current.coins - cost))
    }

    private suspend fun loadState() {
        val saved = dataStore.data.first()
        _state.value = UpgradeState(
                coins = saved[Keys.COINS] ?: defaultState.coins,
                shieldLevel = saved[Keys.SHIELD] ?: defaultState.shieldLevel,
                nuclearLevel = saved[Keys.NUCLEAR] ?: defaultState.nuclearLevel,
                lightningLevel = saved[Keys.LIGHTNING] ?: defaultState.lightningLevel,
                slowTimeLevel = saved[Keys.SLOW_TIME] ?: defaultState.slowTimeLevel,
        )
    }

    private suspend fun persistState(state: UpgradeState) {
        dataStore.edit { prefs ->
            prefs[Keys.COINS] = state.coins
            prefs[Keys.SHIELD] = state.shieldLevel
            prefs[Keys.NUCLEAR] = state.nuclearLevel
            prefs[Keys.LIGHTNING] = state.lightningLevel
            prefs[Keys.SLOW_TIME] = state.slowTimeLevel
        }
    }

    private suspend fun persistOnChange() {
        state.drop(1).collect { persistState(it) }
    }

    private object Keys {
        val COINS = intPreferencesKey("coins")
        val SHIELD = intPreferencesKey("shield_level")
        val NUCLEAR = intPreferencesKey("nuclear_level")
        val LIGHTNING = intPreferencesKey("lightning_level")
        val SLOW_TIME = intPreferencesKey("slow_time_level")
    }
}

data class UpgradeState(
    val coins: Int = 320,
    val shieldLevel: Int = 1,
    val nuclearLevel: Int = 1,
    val lightningLevel: Int = 1,
    val slowTimeLevel: Int = 1
)
