package com.chicken.spaceattack.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.domain.GameEngine
import com.chicken.spaceattack.domain.UpgradeRepository
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class GameViewModel
@Inject
constructor(
        private val engine: GameEngine,
        val audioController: AudioController,
        private val upgradeRepository: UpgradeRepository
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
            val newPhase =
                    when (current.phase) {
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
            val boost =
                    current.boosts.firstOrNull { it.id == boostId } ?: return@updateState current
            val withoutBoost = current.copy(boosts = current.boosts.filterNot { it.id == boostId })
            applyBoost(type = boost.type, state = withoutBoost)
        }
    }

    private fun applyBoost(type: BoostType, state: GameUiState): GameUiState {
        var shotType = state.shotType
        var activeShotBoost = state.activeShotBoost
        var shotBoostRemaining = state.shotBoostRemaining
        var speedModifier = state.enemySpeedModifier
        var shield = state.shieldActive
        var shieldRemaining = state.shieldRemaining
        var slowRemaining = state.slowRemaining
        var nuclearShots = state.nuclearShotsRemaining

        when (type) {
            BoostType.LIGHTNING -> {
                shotType = ShotType.LIGHTNING
                activeShotBoost = BoostType.LIGHTNING
                shotBoostRemaining = GameConfig.Boosts.ttlMillis
                nuclearShots = 0
            }
            BoostType.NUCLEAR -> {
                shotType = ShotType.NUCLEAR
                activeShotBoost = BoostType.NUCLEAR
                shotBoostRemaining = 0
                nuclearShots = upgradeRepository.nuclearShots()
            }
            BoostType.SHIELD -> {
                shield = true
                shieldRemaining = upgradeRepository.shieldDurationMillis()
            }
            BoostType.SLOW_TIME -> {
                slowRemaining = GameConfig.Boosts.ttlMillis
            }
        }

        speedModifier = if (slowRemaining > 0) 0.6f else 1f

        return state.copy(
                shotType = shotType,
                activeShotBoost = activeShotBoost,
                shotBoostRemaining = shotBoostRemaining,
                enemySpeedModifier = speedModifier,
                shieldActive = shield,
                shieldRemaining = shieldRemaining,
                slowRemaining = slowRemaining,
                nuclearShotsRemaining = nuclearShots
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
        val (collectedBoosts, floatingBoosts) =
                movedBoosts.partition {
                    val collectionRadius =
                            (GameConfig.Collision.playerRadius + GameConfig.Collision.boostRadius) *
                                    GameConfig.Collision.colliderScale
                    engine.collides(playerPosition, it.position, collectionRadius)
                }

        var workingState = current.copy(boosts = floatingBoosts)
        collectedBoosts.forEach { boost -> workingState = applyBoost(boost.type, workingState) }

        var shotType = workingState.shotType
        var activeShotBoost = workingState.activeShotBoost
        var shotBoostRemaining = workingState.shotBoostRemaining
        var nuclearShotsRemaining = workingState.nuclearShotsRemaining
        var shieldActive = workingState.shieldActive
        var shieldRemaining = (workingState.shieldRemaining - delta).coerceAtLeast(0)
        var slowRemaining = (workingState.slowRemaining - delta).coerceAtLeast(0)

        if (activeShotBoost == BoostType.LIGHTNING) {
            shotBoostRemaining = (shotBoostRemaining - delta).coerceAtLeast(0)
            if (shotBoostRemaining == 0L) {
                shotType = ShotType.REGULAR
                activeShotBoost = null
            }
        }

        val speedModifier = if (slowRemaining > 0) 0.6f else 1f
        if (activeShotBoost == BoostType.NUCLEAR && nuclearShotsRemaining == 0) {
            shotType = ShotType.REGULAR
            activeShotBoost = null
        }
        if (shieldRemaining == 0L) {
            shieldActive = false
        }

        val movement =
                engine.updateEnemies(
                        workingState.enemies,
                        delta,
                        speedModifier,
                        workingState.enemyDirection
                )
        val enemies = movement.enemies
        val enemyShots =
                (workingState.enemyProjectiles +
                                engine.spawnEnemyShots(
                                        enemies,
                                        chance = GameConfig.Shooting.enemyShotChancePerTick,
                                        speedModifier = speedModifier
                                ))
                        .let { engine.tickProjectiles(it, delta) }

        shotCooldown = (shotCooldown - delta).coerceAtLeast(0)
        val playerShots = buildList {
            addAll(engine.tickProjectiles(workingState.playerProjectiles, delta))
            if (shotCooldown == 0L) {
                add(engine.spawnPlayerShot(Position(current.playerX, 0.86f), shotType))
                shotCooldown = GameConfig.Shooting.playerShotCooldownMillis
                audioController.playShot()
                if (activeShotBoost == BoostType.NUCLEAR && shotType == ShotType.NUCLEAR) {
                    nuclearShotsRemaining = (nuclearShotsRemaining - 1).coerceAtLeast(0)
                    if (nuclearShotsRemaining == 0) {
                        shotType = ShotType.REGULAR
                        activeShotBoost = null
                    }
                }
            }
        }

        val hitResult = engine.resolveHits(enemies, playerShots)
        var score = current.score + hitResult.destroyed.sumOf { it.type.score }
        val destroyedBoosts = hitResult.destroyed.mapNotNull { engine.rollBoost(it.position) }

        val playerHit =
                engine.resolvePlayerHits(
                        playerPosition,
                        GameConfig.Collision.playerRadius,
                        enemyShots
                )
        val lives = if (playerHit.hit && !shieldActive) current.lives - 1 else current.lives
        shieldActive =
                if (playerHit.hit && shieldActive) {
                    shieldRemaining = 0
                    false
                } else {
                    shieldActive
                }

        val newEnemies = hitResult.enemies
        val boss = newEnemies.firstOrNull { it.type == EnemyType.BOSS }
        val destroyedBoss = hitResult.destroyed.firstOrNull { it.type == EnemyType.BOSS }
        val bossSnapshot = boss ?: destroyedBoss
        val bossHealth = boss?.health?.toFloat()?.div(boss.type.health) ?: 0f
        var bossDropsTriggered = current.bossDropsTriggered
        val bossDamageBoosts = mutableListOf<Boost>()
        if (bossSnapshot != null) {
            val maxHealth = bossSnapshot.type.health
            val remainingHealth = bossSnapshot.health.coerceAtLeast(0)
            val lostHealth = maxHealth - remainingHealth
            val milestonesReached = (lostHealth / (maxHealth / 10f)).toInt().coerceAtMost(10)
            if (milestonesReached > bossDropsTriggered) {
                repeat(milestonesReached - bossDropsTriggered) {
                    engine.rollBoost(bossSnapshot.position)?.let { bossDamageBoosts += it }
                }
                bossDropsTriggered = milestonesReached
            }
            if (boss == null) {
                bossDropsTriggered = 0
            }
        } else {
            bossDropsTriggered = 0
        }

        val phase =
                when {
                    lives <= 0 -> GamePhase.LOST
                    newEnemies.any { it.position.y >= 0.9f } -> GamePhase.LOST
                    newEnemies.isEmpty() -> {
                        val next = engine.nextLevel(current.level)
                        if (next == null) GamePhase.WON else GamePhase.LEVEL_COMPLETE
                    }
                    else -> GamePhase.RUNNING
                }

        val nextLevelData =
                if (phase == GamePhase.LEVEL_COMPLETE) engine.nextLevel(current.level) else null
        val finalEnemies = nextLevelData?.second ?: newEnemies
        val nextLevel = nextLevelData?.first ?: current.level

        val updatedState =
                current.copy(
                        enemies = finalEnemies,
                        playerProjectiles = hitResult.projectiles,
                        enemyProjectiles = playerHit.projectiles,
                        score = score,
                        lives = lives,
                        boosts = destroyedBoosts + bossDamageBoosts + workingState.boosts,
                        activeShotBoost = activeShotBoost,
                        shotBoostRemaining = shotBoostRemaining,
                        nuclearShotsRemaining = nuclearShotsRemaining,
                        slowRemaining = slowRemaining,
                        shieldRemaining = shieldRemaining,
                        shotType = shotType,
                        enemySpeedModifier = speedModifier,
                        shieldActive = shieldActive,
                        bossHealth = bossHealth,
                        bossDropsTriggered = bossDropsTriggered,
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

    private fun createInitialState(): GameUiState =
            GameUiState(
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
        val activeShotBoost: BoostType? = null,
        val shotBoostRemaining: Long = 0L,
        val nuclearShotsRemaining: Int = 0,
        val shotType: ShotType = ShotType.REGULAR,
        val enemySpeedModifier: Float = 1f,
        val slowRemaining: Long = 0L,
        val shieldRemaining: Long = 0L,
        val shieldActive: Boolean = false,
        val bossHealth: Float = 0f,
        val bossDropsTriggered: Int = 0,
        val enemyDirection: Float = 1f,
        val phase: GamePhase = GamePhase.RUNNING
)

enum class GamePhase {
    RUNNING,
    PAUSED,
    LOST,
    WON,
    LEVEL_COMPLETE
}
