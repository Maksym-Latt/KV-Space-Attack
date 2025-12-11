package com.chicken.spaceattack.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.domain.GameEngine
import com.chicken.spaceattack.domain.model.Boost
import com.chicken.spaceattack.domain.model.BoostType
import com.chicken.spaceattack.domain.model.Enemy
import com.chicken.spaceattack.domain.model.EnemyType
import com.chicken.spaceattack.domain.model.Position
import com.chicken.spaceattack.domain.model.Projectile
import com.chicken.spaceattack.domain.model.ShotType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GameViewModel @Inject constructor(
    private val engine: GameEngine,
    private val audioController: AudioController
) : ViewModel() {

    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var lastTickTime = 0L
    private var shotCooldown = 0L

    init {
        startLoops()
        audioController.playGameMusic()
    }

    fun movePlayer(delta: Float) {
        updateState { current ->
            if (current.phase != GamePhase.RUNNING) return@updateState current
            val newX = (current.playerX + delta).coerceIn(0.05f, 0.95f)
            current.copy(playerX = newX)
        }
    }

    fun togglePause() {
        updateState { current ->
            val newPhase = when (current.phase) {
                GamePhase.PAUSED -> GamePhase.RUNNING
                GamePhase.RUNNING -> GamePhase.PAUSED
                else -> current.phase
            }
            current.copy(phase = newPhase)
        }
    }

    fun retry() {
        _state.value = createInitialState()
        audioController.playGameMusic()
    }

    fun onAppPaused(paused: Boolean) {
        updateState { current ->
            if (paused && current.phase == GamePhase.RUNNING) current.copy(phase = GamePhase.PAUSED)
            else current
        }
    }

    fun collectBoost(boostId: String) {
        val current = _state.value
        val boost = current.boosts.firstOrNull { it.id == boostId } ?: return
        applyBoost(boost.type)
        _state.value = current.copy(boosts = current.boosts.filterNot { it.id == boostId })
    }

    private fun applyBoost(type: BoostType) {
        updateState { current ->
            val newShotType = when (type) {
                BoostType.LIGHTNING -> ShotType.LIGHTNING
                BoostType.NUCLEAR -> ShotType.NUCLEAR
                else -> current.shotType
            }
            val speedModifier = if (type == BoostType.SLOW_TIME) 0.6f else current.enemySpeedModifier
            val shield = type == BoostType.SHIELD || current.shieldActive
            current.copy(
                shotType = newShotType,
                enemySpeedModifier = speedModifier,
                shieldActive = shield,
                activeBoost = type,
                activeBoostRemaining = 8000L
            )
        }
    }

    private fun startLoops() {
        viewModelScope.launch {
            while (true) {
                delay(16)
                tick()
            }
        }
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val delta = if (lastTickTime == 0L) 16L else (now - lastTickTime).coerceAtMost(32L)
        lastTickTime = now

        val current = _state.value
        if (current.phase != GamePhase.RUNNING) return

        val updatedBoosts = engine.tickBoosts(current.boosts, delta)
        val boostRemaining = (current.activeBoostRemaining - delta).coerceAtLeast(0)
        val boostExpired = boostRemaining == 0L
        val shotType = if (boostExpired) ShotType.REGULAR else current.shotType
        val speedModifier = if (boostExpired && current.activeBoost == BoostType.SLOW_TIME) 1f else current.enemySpeedModifier
        val shield = if (boostExpired && current.activeBoost == BoostType.SHIELD) false else current.shieldActive

        val enemies = engine.updateEnemies(current.enemies, delta, speedModifier)
        val enemyShots = (current.enemyProjectiles + engine.spawnEnemyShots(enemies, chance = 0.01f))
            .let { engine.tickProjectiles(it, delta) }

        shotCooldown = (shotCooldown - delta).coerceAtLeast(0)
        val playerShots = buildList {
            addAll(engine.tickProjectiles(current.playerProjectiles, delta))
            if (shotCooldown == 0L) {
                add(engine.spawnPlayerShot(Position(current.playerX, 0.86f), shotType))
                shotCooldown = 500L
                audioController.playShot()
            }
        }

        val hitResult = engine.resolveHits(enemies, playerShots)
        var score = current.score + hitResult.destroyed.sumOf { it.type.score }
        val destroyedBoosts = hitResult.destroyed.mapNotNull { engine.rollBoost(it.position) }

        val playerHit = engine.resolvePlayerHits(Position(current.playerX, 0.92f), 0.06f, enemyShots)
        val lives = if (playerHit.hit && !shield) current.lives - 1 else current.lives
        val shieldActive = if (playerHit.hit && shield) false else shield

        val newEnemies = hitResult.enemies
        val boss = newEnemies.firstOrNull { it.type == EnemyType.BOSS }
        val bossHealth = boss?.health?.toFloat()?.div(boss.type.health) ?: 0f

        val phase = when {
            lives <= 0 -> GamePhase.LOST
            newEnemies.any { it.position.y >= 0.9f } -> GamePhase.LOST
            newEnemies.isEmpty() -> {
                val next = engine.nextLevel(current.level)
                if (next == null) GamePhase.WON else GamePhase.LEVEL_COMPLETE
            }
            else -> GamePhase.RUNNING
        }

        val nextLevelData = if (phase == GamePhase.LEVEL_COMPLETE) engine.nextLevel(current.level) else null
        val finalEnemies = nextLevelData?.second ?: newEnemies
        val nextLevel = nextLevelData?.first ?: current.level

        val updatedState = current.copy(
            enemies = finalEnemies,
            playerProjectiles = hitResult.projectiles,
            enemyProjectiles = playerHit.projectiles,
            score = score,
            lives = lives,
            boosts = destroyedBoosts + updatedBoosts,
            activeBoost = if (boostExpired) null else current.activeBoost,
            activeBoostRemaining = boostRemaining,
            shotType = shotType,
            enemySpeedModifier = speedModifier,
            shieldActive = shieldActive,
            bossHealth = bossHealth,
            level = nextLevel,
            phase = if (nextLevelData != null) GamePhase.RUNNING else phase
        )

        _state.value = updatedState
        handlePhaseAudio(updatedState.phase)
    }

    private fun handlePhaseAudio(phase: GamePhase) {
        when (phase) {
            GamePhase.WON -> audioController.playWin()
            GamePhase.LOST -> audioController.playLose()
            else -> Unit
        }
    }

    private fun createInitialState(): GameUiState = GameUiState(
        playerX = 0.5f,
        lives = 3,
        score = 0,
        level = 1,
        enemies = engine.initialEnemies(),
        playerProjectiles = emptyList(),
        enemyProjectiles = emptyList(),
        boosts = emptyList()
    )

    private inline fun updateState(block: (GameUiState) -> GameUiState) {
        _state.value = block(_state.value)
    }
}

data class GameUiState(
    val playerX: Float,
    val lives: Int,
    val score: Int,
    val level: Int,
    val enemies: List<Enemy>,
    val playerProjectiles: List<Projectile>,
    val enemyProjectiles: List<Projectile>,
    val boosts: List<Boost>,
    val activeBoost: BoostType? = null,
    val activeBoostRemaining: Long = 0L,
    val shotType: ShotType = ShotType.REGULAR,
    val enemySpeedModifier: Float = 1f,
    val shieldActive: Boolean = false,
    val bossHealth: Float = 0f,
    val phase: GamePhase = GamePhase.RUNNING
)

enum class GamePhase { RUNNING, PAUSED, LOST, WON, LEVEL_COMPLETE }
