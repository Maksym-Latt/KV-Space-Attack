package com.chicken.spaceattack.domain

import com.chicken.spaceattack.domain.config.GameConfig
import com.chicken.spaceattack.domain.model.Boost
import com.chicken.spaceattack.domain.model.BoostType
import com.chicken.spaceattack.domain.model.Enemy
import com.chicken.spaceattack.domain.model.EnemyType
import com.chicken.spaceattack.domain.model.LevelConfig
import com.chicken.spaceattack.domain.model.Position
import com.chicken.spaceattack.domain.model.Projectile
import com.chicken.spaceattack.domain.model.ShotType
import kotlin.math.hypot
import kotlin.random.Random

class GameEngine {
    private val horizontalBounds = 0f..1f
    private val verticalBounds = 0f..1f
    private val formationStartY = 0.2f
    private val formationSpacingY = 0.08f
    private val formationColumns = 6

    private val levelConfigs = listOf(
        LevelConfig(level = 1, smallEnemies = 12, mediumEnemies = 4),
        LevelConfig(level = 2, smallEnemies = 8, mediumEnemies = 8),
        LevelConfig(level = 3, smallEnemies = 6, mediumEnemies = 10),
        LevelConfig(level = 4, smallEnemies = 0, mediumEnemies = 0, boss = true)
    )

    fun initialEnemies() = spawnLevel(levelConfigs.first())

    fun nextLevel(currentLevel: Int): Pair<Int, List<Enemy>>? {
        val config = levelConfigs.getOrNull(levelConfigs.indexOfFirst { it.level == currentLevel } + 1)
            ?: return null
        return config.level to spawnLevel(config)
    }

    fun currentLevelConfig(level: Int) = levelConfigs.firstOrNull { it.level == level } ?: levelConfigs.last()

    private fun spawnLevel(config: LevelConfig): List<Enemy> {
        val totalEnemies = config.smallEnemies + config.mediumEnemies
        val slots = mutableListOf<FormationSlot>()
        var row = 0
        var column = 0
        repeat(totalEnemies) {
            slots += FormationSlot(row, column, gridPosition(row, column))
            column++
            if (column >= formationColumns) {
                column = 0
                row++
            }
        }

        val pattern = FormationPattern.entries.random()
        val assignedTypes = when (pattern) {
            FormationPattern.FRONT_RED_BACK_BLUE -> frontRedBackBlue(config, slots.size)
            FormationPattern.CHECKERBOARD -> checkerboard(config, slots)
            FormationPattern.RED_SIDES -> redSides(config, slots)
        }

        val enemies = slots.mapIndexed { index, slot ->
            Enemy(
                type = assignedTypes.getOrElse(index) { EnemyType.SMALL },
                position = slot.position
            )
        }.toMutableList()

        if (config.boss) {
            enemies += Enemy(type = EnemyType.BOSS, position = Position(0.5f, 0.18f), direction = 1f)
        }
        return enemies
    }

    private fun frontRedBackBlue(config: LevelConfig, totalSlots: Int): List<EnemyType> {
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        return List(totalSlots) {
            when {
                remainingMedium > 0 -> {
                    remainingMedium--
                    EnemyType.MEDIUM
                }

                remainingSmall > 0 -> {
                    remainingSmall--
                    EnemyType.SMALL
                }

                else -> EnemyType.SMALL
            }
        }
    }

    private fun checkerboard(config: LevelConfig, slots: List<FormationSlot>): List<EnemyType> {
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        return slots.map { slot ->
            val prefersMedium = (slot.row + slot.column) % 2 == 0
            when {
                prefersMedium && remainingMedium > 0 -> {
                    remainingMedium--
                    EnemyType.MEDIUM
                }

                remainingSmall > 0 -> {
                    remainingSmall--
                    EnemyType.SMALL
                }

                remainingMedium > 0 -> {
                    remainingMedium--
                    EnemyType.MEDIUM
                }

                else -> EnemyType.SMALL
            }
        }
    }

    private fun redSides(config: LevelConfig, slots: List<FormationSlot>): List<EnemyType> {
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        return slots.map { slot ->
            val onSide = slot.column == 0 || slot.column == formationColumns - 1
            when {
                onSide && remainingMedium > 0 -> {
                    remainingMedium--
                    EnemyType.MEDIUM
                }

                remainingSmall > 0 -> {
                    remainingSmall--
                    EnemyType.SMALL
                }

                remainingMedium > 0 -> {
                    remainingMedium--
                    EnemyType.MEDIUM
                }

                else -> EnemyType.SMALL
            }
        }
    }

    private fun gridPosition(row: Int, column: Int): Position {
        val spacingX = 1f / 7f
        val startX = spacingX
        val x = startX + column * spacingX
        val y = formationStartY + row * formationSpacingY
        return Position(x, y)
    }

    fun updateEnemies(
        enemies: List<Enemy>,
        deltaMillis: Long,
        speedModifier: Float,
        direction: Float
    ): EnemyMovement {
        val delta = deltaMillis / 1000f
        val horizontalSpeed = GameConfig.Movement.enemyFormationHorizontalSpeed * direction * delta * speedModifier
        val passiveDescent = GameConfig.Movement.enemyFormationPassiveDescentSpeed * delta * speedModifier

        var needsDescent = false
        val moved = enemies.map { enemy ->
            val newX = (enemy.position.x + horizontalSpeed).coerceIn(horizontalBounds)
            val newY = (enemy.position.y + passiveDescent).coerceAtMost(verticalBounds.endInclusive)
            needsDescent = needsDescent || newX <= horizontalBounds.start || newX >= horizontalBounds.endInclusive
            enemy.copy(
                position = Position(newX, newY),
                direction = direction
            )
        }

        var newDirection = direction
        val finalEnemies = if (needsDescent) {
            newDirection *= -1f
            val descent = GameConfig.Movement.enemyFormationDescentSpeed * delta * speedModifier
            moved.map { enemy ->
                enemy.copy(
                    position = Position(
                        enemy.position.x,
                        (enemy.position.y + descent).coerceAtMost(verticalBounds.endInclusive)
                    ),
                    direction = newDirection
                )
            }
        } else {
            moved
        }

        return EnemyMovement(finalEnemies, newDirection)
    }

    fun tickProjectiles(projectiles: List<Projectile>, deltaMillis: Long): List<Projectile> {
        val delta = deltaMillis / 1000f
        return projectiles.map { projectile ->
            val newX = projectile.position.x + projectile.velocity.x * delta
            val newY = projectile.position.y + projectile.velocity.y * delta
            projectile.copy(position = Position(newX, newY))
        }.filter { it.position.y in verticalBounds }
    }

    fun resolveHits(
        enemies: List<Enemy>,
        projectiles: List<Projectile>
    ): HitResult {
        val remainingProjectiles = projectiles.toMutableList()
        val updatedEnemies = enemies.toMutableList()
        val destroyed = mutableListOf<Enemy>()
        val hits = mutableListOf<Enemy>()

        val iterator = remainingProjectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            if (!projectile.isPlayer) continue
            val hit = updatedEnemies.firstOrNull {
                collides(
                    projectile.position,
                    it.position,
                    radius =
                    (GameConfig.Collision.enemyRadius + GameConfig.Collision.projectileRadius) * GameConfig.Collision.colliderScale
                )
            }
            if (hit != null) {
                iterator.remove()
                val newHealth = hit.health - projectile.damage
                val updated = hit.copy(health = newHealth)
                updatedEnemies.remove(hit)
                if (newHealth <= 0) {
                    destroyed += updated
                } else {
                    hits += updated
                    updatedEnemies += updated
                }
            }
        }

        return HitResult(
            enemies = updatedEnemies,
            destroyed = destroyed,
            hits = hits,
            projectiles = remainingProjectiles
        )
    }

    fun resolvePlayerHits(
        playerPosition: Position,
        playerRadius: Float,
        projectiles: List<Projectile>
    ): PlayerHitResult {
        val remaining = projectiles.toMutableList()
        var hit = false
        val iterator = remaining.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            if (projectile.isPlayer) continue
            val scaledRadius =
                (playerRadius + GameConfig.Collision.projectileRadius) * GameConfig.Collision.colliderScale
            if (collides(playerPosition, projectile.position, scaledRadius)) {
                hit = true
                iterator.remove()
            }
        }
        return PlayerHitResult(remaining, hit)
    }

    fun spawnEnemyShots(enemies: List<Enemy>, chance: Float, speedModifier: Float): List<Projectile> {
        if (enemies.isEmpty()) return emptyList()
        val shouldShoot = Random.nextFloat() < chance
        if (!shouldShoot) return emptyList()
        val shooter = enemies.random()
        val speed = GameConfig.Projectiles.enemyProjectileSpeeds[shooter.type] ?: 0.3f
        return listOf(
            Projectile(
                position = shooter.position,
                velocity = Position(0f, speed * speedModifier),
                isPlayer = false,
                sprite = when (shooter.type) {
                    EnemyType.BOSS -> com.chicken.spaceattack.R.drawable.nuclear_shot
                    else -> com.chicken.spaceattack.R.drawable.lightning_shot
                }
            )
        )
    }

    fun spawnPlayerShot(position: Position, shotType: ShotType): Projectile {
        return Projectile(
            position = Position(position.x, position.y - 0.05f),
            velocity = Position(0f, GameConfig.Projectiles.playerProjectileSpeed),
            isPlayer = true,
            damage = shotType.damage,
            sprite = shotType.sprite
        )
    }

    fun rollBoost(position: Position): Boost? {
        val roll = Random.nextFloat()
        var cumulative = 0f
        GameConfig.Boosts.dropChances.forEach { (type, chance) ->
            cumulative += chance
            if (roll < cumulative) {
                return Boost(type = type, position = position)
            }
        }
        return null
    }

    fun tickBoosts(boosts: List<Boost>, deltaMillis: Long): List<Boost> {
        val delta = deltaMillis / 1000f
        val fallDistance = GameConfig.Boosts.fallSpeed * delta
        return boosts.mapNotNull { boost ->
            val remaining = boost.ttlMillis - deltaMillis
            if (remaining <= 0) return@mapNotNull null

            val newY = boost.position.y + fallDistance
            if (newY > verticalBounds.endInclusive + GameConfig.Collision.boostRadius) return@mapNotNull null

            boost.copy(
                position = Position(boost.position.x, newY),
                ttlMillis = remaining
            )
        }
    }

    private enum class FormationPattern { FRONT_RED_BACK_BLUE, CHECKERBOARD, RED_SIDES }

    private data class FormationSlot(val row: Int, val column: Int, val position: Position)

    fun collides(a: Position, b: Position, radius: Float): Boolean {
        return hypot(a.x - b.x, a.y - b.y) < radius
    }

    data class EnemyMovement(val enemies: List<Enemy>, val direction: Float)

    data class HitResult(
        val enemies: List<Enemy>,
        val destroyed: List<Enemy>,
        val hits: List<Enemy>,
        val projectiles: List<Projectile>
    )

    data class PlayerHitResult(val projectiles: List<Projectile>, val hit: Boolean)
}
