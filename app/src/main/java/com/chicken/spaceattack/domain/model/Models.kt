package com.chicken.spaceattack.domain.model

import androidx.annotation.DrawableRes
import com.chicken.spaceattack.R
import java.util.UUID

data class Position(val x: Float, val y: Float)

enum class EnemyType(@DrawableRes val sprite: Int, val health: Int, val score: Int) {
    SMALL(R.drawable.enemy_small_blue, health = 1, score = 25),
    MEDIUM(R.drawable.enemy_medium_red, health = 2, score = 50),
    BOSS(R.drawable.enemy_boss, health = 60, score = 500)
}

data class Enemy(
    val id: String = UUID.randomUUID().toString(),
    val type: EnemyType,
    val position: Position,
    val direction: Float = 1f,
    val health: Int = type.health
)

data class Projectile(
    val id: String = UUID.randomUUID().toString(),
    val position: Position,
    val velocity: Position,
    val isPlayer: Boolean,
    val damage: Int = 1,
    @DrawableRes val sprite: Int
)

enum class ShotType(val damage: Int, @DrawableRes val sprite: Int) {
    REGULAR(damage = 1, sprite = R.drawable.regular_shot),
    LIGHTNING(damage = 2, sprite = R.drawable.lightning_shot),
    NUCLEAR(damage = 4, sprite = R.drawable.nuclear_shot)
}

enum class BoostType(@DrawableRes val icon: Int) {
    LIGHTNING(R.drawable.boost_lightning),
    NUCLEAR(R.drawable.boost_nuclear),
    SHIELD(R.drawable.boost_save),
    SLOW_TIME(R.drawable.boost_time)
}

data class Boost(
    val id: String = UUID.randomUUID().toString(),
    val type: BoostType,
    val position: Position,
    val isActive: Boolean = false,
    val ttlMillis: Long = 8000L
)

data class LevelConfig(val level: Int, val smallEnemies: Int, val mediumEnemies: Int, val boss: Boolean = false)
