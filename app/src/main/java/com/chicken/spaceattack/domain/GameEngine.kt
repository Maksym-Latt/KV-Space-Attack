package com.chicken.spaceattack.domain

import com.chicken.spaceattack.domain.config.GameConfig
import com.chicken.spaceattack.domain.model.Boost
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
    private val formationStartY = 0.28f
    private val formationSpacingY = 0.08f
    private val formationColumns = 5 // Reduced from 6 to create more space

    private val levelConfigs =
            listOf(
                    LevelConfig(level = 1, smallEnemies = 5, mediumEnemies = 0), // Easy start
                    LevelConfig(level = 2, smallEnemies = 8, mediumEnemies = 1), // Getting harder
                    LevelConfig(level = 3, smallEnemies = 10, mediumEnemies = 5), // Balanced
                    LevelConfig(level = 4, smallEnemies = 8, mediumEnemies = 8), // More reds
                    LevelConfig(level = 5, smallEnemies = 10, mediumEnemies = 10), // Many enemies
                    LevelConfig(level = 6, smallEnemies = 5, mediumEnemies = 15), // Mostly reds
                    LevelConfig(
                            level = 7,
                            smallEnemies = 0,
                            mediumEnemies = 0,
                            boss = true
                    ) // Boss level
            )

    fun initialEnemies() = spawnLevel(levelConfigs.first())

    fun nextLevel(currentLevel: Int): Pair<Int, List<Enemy>>? {
        val config =
                levelConfigs.getOrNull(levelConfigs.indexOfFirst { it.level == currentLevel } + 1)
                        ?: return null
        return config.level to spawnLevel(config)
    }

    fun currentLevelConfig(level: Int) =
            levelConfigs.firstOrNull { it.level == level } ?: levelConfigs.last()

    private fun spawnLevel(config: LevelConfig): List<Enemy> {
        val pattern = FormationPattern.entries.random()
        val enemies =
                when (pattern) {
                    FormationPattern.FRONT_RED_BACK_BLUE -> createFrontRedBackBlue(config)
                    FormationPattern.CHECKERBOARD -> createCheckerboard(config)
                    FormationPattern.RED_SIDES -> createRedSides(config)
                    FormationPattern.V_SHAPE -> createVShape(config)
                    FormationPattern.DIAMOND -> createDiamond(config)
                    FormationPattern.SCATTERED -> createScattered(config)
                    FormationPattern.ALTERNATING_ROWS -> createAlternatingRows(config)
                    FormationPattern.CENTER_FORMATION -> createCenterFormation(config)
                }.toMutableList()

        if (config.boss) {
            enemies +=
                    Enemy(type = EnemyType.BOSS, position = Position(0.5f, 0.18f), direction = 1f)
        }
        return enemies
    }

    // Original patterns
    private fun createFrontRedBackBlue(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        var row = 0
        var column = 0

        while (remainingMedium > 0 || remainingSmall > 0) {
            val type =
                    if (remainingMedium > 0) {
                        remainingMedium--
                        EnemyType.MEDIUM
                    } else {
                        remainingSmall--
                        EnemyType.SMALL
                    }
            enemies += Enemy(type = type, position = gridPosition(row, column))
            column++
            if (column >= formationColumns) {
                column = 0
                row++
            }
        }
        return enemies
    }

    private fun createCheckerboard(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        var row = 0
        var column = 0

        while (remainingMedium > 0 || remainingSmall > 0) {
            val prefersMedium = (row + column) % 2 == 0
            val type =
                    when {
                        prefersMedium && remainingMedium > 0 -> {
                            remainingMedium--
                            EnemyType.MEDIUM
                        }
                        remainingSmall > 0 -> {
                            remainingSmall--
                            EnemyType.SMALL
                        }
                        else -> {
                            remainingMedium--
                            EnemyType.MEDIUM
                        }
                    }
            enemies += Enemy(type = type, position = gridPosition(row, column))
            column++
            if (column >= formationColumns) {
                column = 0
                row++
            }
        }
        return enemies
    }

    private fun createRedSides(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        var row = 0
        var column = 0

        while (remainingMedium > 0 || remainingSmall > 0) {
            val onSide = column == 0 || column == formationColumns - 1
            val type =
                    when {
                        onSide && remainingMedium > 0 -> {
                            remainingMedium--
                            EnemyType.MEDIUM
                        }
                        remainingSmall > 0 -> {
                            remainingSmall--
                            EnemyType.SMALL
                        }
                        else -> {
                            remainingMedium--
                            EnemyType.MEDIUM
                        }
                    }
            enemies += Enemy(type = type, position = gridPosition(row, column))
            column++
            if (column >= formationColumns) {
                column = 0
                row++
            }
        }
        return enemies
    }

    // New diverse patterns
    private fun createVShape(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        val rows = 4

        for (row in 0 until rows) {
            val gap = row // Gap increases with each row
            val startCol = gap
            val endCol = formationColumns - 1 - gap

            if (startCol <= endCol) {
                // Left side of V
                if (remainingMedium > 0 || remainingSmall > 0) {
                    val type =
                            if (remainingMedium > 0) {
                                remainingMedium--
                                EnemyType.MEDIUM
                            } else {
                                remainingSmall--
                                EnemyType.SMALL
                            }
                    enemies += Enemy(type = type, position = gridPosition(row, startCol))
                }
                // Right side of V
                if (startCol != endCol && (remainingMedium > 0 || remainingSmall > 0)) {
                    val type =
                            if (remainingMedium > 0) {
                                remainingMedium--
                                EnemyType.MEDIUM
                            } else {
                                remainingSmall--
                                EnemyType.SMALL
                            }
                    enemies += Enemy(type = type, position = gridPosition(row, endCol))
                }
            }
        }
        return enemies
    }

    private fun createDiamond(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        val center = formationColumns / 2

        // Top half of diamond
        for (row in 0..2) {
            val width = row + 1
            for (offset in 0 until width) {
                val col = center - offset
                if (col >= 0 &&
                                col < formationColumns &&
                                (remainingMedium > 0 || remainingSmall > 0)
                ) {
                    val type =
                            if (Random.nextBoolean() && remainingMedium > 0) {
                                remainingMedium--
                                EnemyType.MEDIUM
                            } else if (remainingSmall > 0) {
                                remainingSmall--
                                EnemyType.SMALL
                            } else {
                                remainingMedium--
                                EnemyType.MEDIUM
                            }
                    enemies += Enemy(type = type, position = gridPosition(row, col))
                }
                if (offset > 0) {
                    val colRight = center + offset
                    if (colRight < formationColumns && (remainingMedium > 0 || remainingSmall > 0)
                    ) {
                        val type =
                                if (Random.nextBoolean() && remainingMedium > 0) {
                                    remainingMedium--
                                    EnemyType.MEDIUM
                                } else if (remainingSmall > 0) {
                                    remainingSmall--
                                    EnemyType.SMALL
                                } else {
                                    remainingMedium--
                                    EnemyType.MEDIUM
                                }
                        enemies += Enemy(type = type, position = gridPosition(row, colRight))
                    }
                }
            }
        }
        return enemies
    }

    private fun createScattered(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        val totalEnemies = remainingMedium + remainingSmall
        val positions = mutableListOf<Pair<Int, Int>>()

        // Generate all possible positions
        for (row in 0..3) {
            for (col in 0 until formationColumns) {
                positions.add(row to col)
            }
        }
        positions.shuffle()

        // Place enemies randomly
        for (i in 0 until minOf(totalEnemies, positions.size)) {
            val (row, col) = positions[i]
            val type =
                    if (remainingMedium > 0 && Random.nextBoolean()) {
                        remainingMedium--
                        EnemyType.MEDIUM
                    } else if (remainingSmall > 0) {
                        remainingSmall--
                        EnemyType.SMALL
                    } else if (remainingMedium > 0) {
                        remainingMedium--
                        EnemyType.MEDIUM
                    } else continue

            enemies += Enemy(type = type, position = gridPosition(row, col))
        }
        return enemies
    }

    private fun createAlternatingRows(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies

        for (row in 0..3) {
            val columnsToUse =
                    if (row % 2 == 0) {
                        listOf(0, 2, 4) // Even rows: skip every other column
                    } else {
                        listOf(1, 3) // Odd rows: offset pattern
                    }

            for (col in columnsToUse) {
                if (col < formationColumns && (remainingMedium > 0 || remainingSmall > 0)) {
                    val type =
                            if (row < 2 && remainingMedium > 0) {
                                remainingMedium--
                                EnemyType.MEDIUM
                            } else if (remainingSmall > 0) {
                                remainingSmall--
                                EnemyType.SMALL
                            } else if (remainingMedium > 0) {
                                remainingMedium--
                                EnemyType.MEDIUM
                            } else continue

                    enemies += Enemy(type = type, position = gridPosition(row, col))
                }
            }
        }
        return enemies
    }

    private fun createCenterFormation(config: LevelConfig): List<Enemy> {
        val enemies = mutableListOf<Enemy>()
        var remainingMedium = config.mediumEnemies
        var remainingSmall = config.smallEnemies
        val center = formationColumns / 2

        // Create a concentrated formation in the center
        for (row in 0..3) {
            val width = if (row < 2) 3 else 2 // Wider at top, narrower at bottom
            val startCol = center - width / 2

            for (offset in 0 until width) {
                val col = startCol + offset
                if (col >= 0 &&
                                col < formationColumns &&
                                (remainingMedium > 0 || remainingSmall > 0)
                ) {
                    val type =
                            if (col == center && remainingMedium > 0) {
                                remainingMedium--
                                EnemyType.MEDIUM
                            } else if (remainingSmall > 0) {
                                remainingSmall--
                                EnemyType.SMALL
                            } else if (remainingMedium > 0) {
                                remainingMedium--
                                EnemyType.MEDIUM
                            } else continue

                    enemies += Enemy(type = type, position = gridPosition(row, col))
                }
            }
        }
        return enemies
    }

    private fun gridPosition(row: Int, column: Int): Position {
        // Spread enemies across full screen width with even spacing
        val leftMargin = 0.05f // 5% margin from left edge
        val rightMargin = 0.05f // 5% margin from right edge
        val usableWidth = 1f - leftMargin - rightMargin // 0.9 (90% of screen)
        val spacingX = usableWidth / (formationColumns - 1) // Space between enemies
        val x = leftMargin + (column * spacingX)
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
        val horizontalSpeed =
                GameConfig.Movement.enemyFormationHorizontalSpeed *
                        direction *
                        delta *
                        speedModifier
        val passiveDescent =
                GameConfig.Movement.enemyFormationPassiveDescentSpeed * delta * speedModifier

        var needsDescent = false
        val moved =
                enemies.map { enemy ->
                    val newX = (enemy.position.x + horizontalSpeed).coerceIn(horizontalBounds)
                    val newY =
                            (enemy.position.y + passiveDescent).coerceAtMost(
                                    verticalBounds.endInclusive
                            )
                    needsDescent =
                            needsDescent ||
                                    newX <= horizontalBounds.start ||
                                    newX >= horizontalBounds.endInclusive
                    enemy.copy(position = Position(newX, newY), direction = direction)
                }

        var newDirection = direction
        val finalEnemies =
                if (needsDescent) {
                    newDirection *= -1f
                    val descent =
                            GameConfig.Movement.enemyFormationDescentSpeed * delta * speedModifier
                    moved.map { enemy ->
                        enemy.copy(
                                position =
                                        Position(
                                                enemy.position.x,
                                                (enemy.position.y + descent).coerceAtMost(
                                                        verticalBounds.endInclusive
                                                )
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
        return projectiles
                .map { projectile ->
                    val newX = projectile.position.x + projectile.velocity.x * delta
                    val newY = projectile.position.y + projectile.velocity.y * delta
                    projectile.copy(position = Position(newX, newY))
                }
                .filter { it.position.y in verticalBounds }
    }

    fun resolveHits(enemies: List<Enemy>, projectiles: List<Projectile>): HitResult {
        val remainingProjectiles = projectiles.toMutableList()
        val updatedEnemies = enemies.toMutableList()
        val destroyed = mutableListOf<Enemy>()
        val hits = mutableListOf<Enemy>()

        val iterator = remainingProjectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            if (!projectile.isPlayer) continue
            val hit =
                    updatedEnemies.firstOrNull {
                        collides(
                                projectile.position,
                                it.position,
                                radius =
                                        (GameConfig.Collision.enemyRadius +
                                                GameConfig.Collision.projectileRadius) *
                                                GameConfig.Collision.colliderScale
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
                    (playerRadius + GameConfig.Collision.projectileRadius) *
                            GameConfig.Collision.colliderScale
            if (collides(playerPosition, projectile.position, scaledRadius)) {
                hit = true
                iterator.remove()
            }
        }
        return PlayerHitResult(remaining, hit)
    }

    fun spawnEnemyShots(
            enemies: List<Enemy>,
            chance: Float,
            speedModifier: Float
    ): List<Projectile> {
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
                        sprite =
                                when (shooter.type) {
                                    EnemyType.BOSS ->
                                            com.chicken.spaceattack.R.drawable.nuclear_shot
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
            if (newY > verticalBounds.endInclusive + GameConfig.Collision.boostRadius)
                    return@mapNotNull null

            boost.copy(position = Position(boost.position.x, newY), ttlMillis = remaining)
        }
    }

    private enum class FormationPattern {
        FRONT_RED_BACK_BLUE,
        CHECKERBOARD,
        RED_SIDES,
        V_SHAPE,
        DIAMOND,
        SCATTERED,
        ALTERNATING_ROWS,
        CENTER_FORMATION
    }

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
