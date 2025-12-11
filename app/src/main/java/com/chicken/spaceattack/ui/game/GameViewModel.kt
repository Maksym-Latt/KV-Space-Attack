package com.chicken.spaceattack.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.domain.GameEngine
import com.chicken.spaceattack.domain.config.GameConfig
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
import kotlinx.coroutines.isActive

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
        updatePlayerPosition(_state.value.playerX + delta)
    }

    fun setPlayerPosition(fraction: Float) {
        updatePlayerPosition(fraction)
    }

    private fun updatePlayerPosition(newValue: Float) {
        updateState { current ->
            if (current.phase != GamePhase.RUNNING) return@updateState current
            val newX = newValue.coerceIn(0.05f, 0.95f)
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
        updateState { current ->
            val boost = current.boosts.firstOrNull { it.id == boostId } ?: return@updateState current
            val withoutBoost = current.copy(boosts = current.boosts.filterNot { it.id == boostId })
            applyBoost(type = boost.type, state = withoutBoost)
        }
    }

    private fun applyBoost(type: BoostType, state: GameUiState): GameUiState {
        val newShotType = when (type) {
            BoostType.LIGHTNING -> ShotType.LIGHTNING
            BoostType.NUCLEAR -> ShotType.NUCLEAR
            else -> state.shotType
        }
        val speedModifier = if (type == BoostType.SLOW_TIME) 0.6f else state.enemySpeedModifier
        val shield = type == BoostType.SHIELD || state.shieldActive
        return state.copy(
            shotType = newShotType,
            enemySpeedModifier = speedModifier,
            shieldActive = shield,
            activeBoost = type,
            activeBoostRemaining = GameConfig.Boosts.ttlMillis
        )
    }

    private fun startLoops() {
        viewModelScope.launch {
            while (isActive) {
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

        val playerPosition = Position(current.playerX, 0.92f)

        val movedBoosts = engine.tickBoosts(current.boosts, delta)
        val (collectedBoosts, floatingBoosts) = movedBoosts.partition {
            val collectionRadius =
                (GameConfig.Collision.playerRadius + GameConfig.Collision.boostRadius) * GameConfig.Collision.colliderScale
            engine.collides(playerPosition, it.position, collectionRadius)
        }

        var workingState = current.copy(boosts = floatingBoosts)
        collectedBoosts.forEach { boost ->
            workingState = applyBoost(boost.type, workingState)
        }

        val boostRemaining = (workingState.activeBoostRemaining - delta).coerceAtLeast(0)
        val boostExpired = boostRemaining == 0L && workingState.activeBoost != null
        val shotType = if (boostExpired) ShotType.REGULAR else workingState.shotType
        val speedModifier = if (boostExpired && workingState.activeBoost == BoostType.SLOW_TIME) 1f else workingState.enemySpeedModifier
        val shield = if (boostExpired && workingState.activeBoost == BoostType.SHIELD) false else workingState.shieldActive

        val movement = engine.updateEnemies(workingState.enemies, delta, speedModifier, workingState.enemyDirection)
        val enemies = movement.enemies
        val enemyShots = (workingState.enemyProjectiles +
            engine.spawnEnemyShots(enemies, chance = GameConfig.Shooting.enemyShotChancePerTick, speedModifier = speedModifier))
            .let { engine.tickProjectiles(it, delta) }

        shotCooldown = (shotCooldown - delta).coerceAtLeast(0)
        val playerShots = buildList {
            addAll(engine.tickProjectiles(workingState.playerProjectiles, delta))
            if (shotCooldown == 0L) {
                add(engine.spawnPlayerShot(Position(current.playerX, 0.86f), shotType))
                shotCooldown = GameConfig.Shooting.playerShotCooldownMillis
                audioController.playShot()
            }
        }

        val hitResult = engine.resolveHits(enemies, playerShots)
        var score = current.score + hitResult.destroyed.sumOf { it.type.score }
        val destroyedBoosts = hitResult.destroyed.mapNotNull { engine.rollBoost(it.position) }

        val playerHit = engine.resolvePlayerHits(playerPosition, GameConfig.Collision.playerRadius, enemyShots)
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
            boosts = destroyedBoosts + workingState.boosts,
            activeBoost = if (boostExpired) null else workingState.activeBoost,
            activeBoostRemaining = boostRemaining,
            shotType = shotType,
            enemySpeedModifier = speedModifier,
            shieldActive = shieldActive,
            bossHealth = bossHealth,
            level = nextLevel,
            enemyDirection = if (nextLevelData != null) 1f else movement.direction,
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
        boosts = emptyList(),
        enemyDirection = 1f
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
    val enemyDirection: Float = 1f,
    val phase: GamePhase = GamePhase.RUNNING
)

enum class GamePhase { RUNNING, PAUSED, LOST, WON, LEVEL_COMPLETE }
