package com.chicken.spaceattack.domain

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

    private val levelConfigs = listOf(
        LevelConfig(level = 1, smallEnemies = 12, mediumEnemies = 4),
        LevelConfig(level = 2, smallEnemies = 8, mediumEnemies = 8),
        LevelConfig(level = 3, smallEnemies = 6, mediumEnemies = 10),
        LevelConfig(level = 4, smallEnemies = 4, mediumEnemies = 8, boss = true)
    )

    fun initialEnemies() = spawnLevel(levelConfigs.first())

    fun nextLevel(currentLevel: Int): Pair<Int, List<Enemy>>? {
        val config = levelConfigs.getOrNull(levelConfigs.indexOfFirst { it.level == currentLevel } + 1)
            ?: return null
        return config.level to spawnLevel(config)
    }

    fun currentLevelConfig(level: Int) = levelConfigs.firstOrNull { it.level == level } ?: levelConfigs.last()

    private fun spawnLevel(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var row = 0
        var column = 0
        repeat(config.smallEnemies) {
            enemies += Enemy(
                type = EnemyType.SMALL,
                position = gridPosition(row, column)
            )
            column++
            if (column >= 6) {
                column = 0
                row++
            }
        }
        repeat(config.mediumEnemies) {
            enemies += Enemy(
                type = EnemyType.MEDIUM,
                position = gridPosition(row, column)
            )
            column++
            if (column >= 6) {
                column = 0
                row++
            }
        }
        if (config.boss) {
            enemies += Enemy(type = EnemyType.BOSS, position = Position(0.5f, 0.1f), direction = 1f)
        }
        return enemies
    }

    private fun gridPosition(row: Int, column: Int): Position {
        val spacingX = 1f / 7f
        val spacingY = 0.08f
        val startX = spacingX
        val x = startX + column * spacingX
        val y = 0.12f + row * spacingY
        return Position(x, y)
    }

    fun updateEnemies(enemies: List<Enemy>, deltaMillis: Long, speedModifier: Float): List<Enemy> {
        val delta = deltaMillis / 1000f
        return enemies.map { enemy ->
            val horizontalSpeed = when (enemy.type) {
                EnemyType.SMALL -> 0.25f
                EnemyType.MEDIUM -> 0.2f
                EnemyType.BOSS -> 0.15f
            } * enemy.direction * delta * speedModifier

            val newX = (enemy.position.x + horizontalSpeed).coerceIn(horizontalBounds)
            val newDirection = when {
                newX <= horizontalBounds.start -> 1f
                newX >= horizontalBounds.endInclusive -> -1f
                else -> enemy.direction
            }
            val descent = when (enemy.type) {
                EnemyType.SMALL -> 0.02f
                EnemyType.MEDIUM -> 0.018f
                EnemyType.BOSS -> 0.01f
            } * delta * speedModifier
            enemy.copy(
                position = Position(newX, (enemy.position.y + descent).coerceAtMost(verticalBounds.endInclusive)),
                direction = newDirection
            )
        }
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
            val hit = updatedEnemies.firstOrNull { collides(projectile.position, it.position, radius = 0.05f) }
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
            if (collides(playerPosition, projectile.position, playerRadius)) {
                hit = true
                iterator.remove()
            }
        }
        return PlayerHitResult(remaining, hit)
    }

    fun spawnEnemyShots(enemies: List<Enemy>, chance: Float): List<Projectile> {
        if (enemies.isEmpty()) return emptyList()
        val shouldShoot = Random.nextFloat() < chance
        if (!shouldShoot) return emptyList()
        val shooter = enemies.random()
        val speed = when (shooter.type) {
            EnemyType.SMALL -> 0.35f
            EnemyType.MEDIUM -> 0.32f
            EnemyType.BOSS -> 0.28f
        }
        return listOf(
            Projectile(
                position = shooter.position,
                velocity = Position(0f, speed),
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
            velocity = Position(0f, -0.7f),
            isPlayer = true,
            damage = shotType.damage,
            sprite = shotType.sprite
        )
    }

    fun rollBoost(position: Position): Boost? {
        val roll = Random.nextFloat()
        val boostType = when {
            roll < 0.25f -> BoostType.SHIELD
            roll < 0.45f -> BoostType.SLOW_TIME
            roll < 0.65f -> BoostType.LIGHTNING
            roll < 0.75f -> BoostType.NUCLEAR
            else -> return null
        }
        return Boost(type = boostType, position = position)
    }

    fun tickBoosts(boosts: List<Boost>, deltaMillis: Long): List<Boost> {
        return boosts.mapNotNull { boost ->
            val remaining = boost.ttlMillis - deltaMillis
            if (remaining <= 0) null else boost.copy(ttlMillis = remaining)
        }
    }

    private fun collides(a: Position, b: Position, radius: Float): Boolean {
        return hypot(a.x - b.x, a.y - b.y) < radius
    }

    data class HitResult(
        val enemies: List<Enemy>,
        val destroyed: List<Enemy>,
        val hits: List<Enemy>,
        val projectiles: List<Projectile>
    )

    data class PlayerHitResult(val projectiles: List<Projectile>, val hit: Boolean)
}
