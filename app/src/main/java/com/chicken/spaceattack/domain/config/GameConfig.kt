package com.chicken.spaceattack.domain.config

import com.chicken.spaceattack.domain.model.BoostType
import com.chicken.spaceattack.domain.model.EnemyType

object GameConfig {
    object Movement {
        const val enemyFormationHorizontalSpeed = 0.22f
        const val enemyFormationDescentSpeed = 0.012f
        const val enemyFormationPassiveDescentSpeed = 0.018f
    }

    object Shooting {
        const val playerShotCooldownMillis = 550L
        const val enemyShotChancePerTick = 0.006f
    }

    object Projectiles {
        val enemyProjectileSpeeds = mapOf(
            EnemyType.SMALL to 0.28f,
            EnemyType.MEDIUM to 0.26f,
            EnemyType.BOSS to 0.24f
        )
        const val playerProjectileSpeed = -0.7f
    }

    object Boosts {
        const val fallSpeed = 0.18f
        const val ttlMillis = 8000L
        val dropChances = linkedMapOf(
            BoostType.SHIELD to 0.2f,
            BoostType.SLOW_TIME to 0.25f,
            BoostType.LIGHTNING to 0.2f,
            BoostType.NUCLEAR to 0.15f
        )
    }

    object Collision {
        const val colliderScale = 1.0f
        const val showDebug = false
        const val enemyRadius = 0.07f
        const val playerRadius = 0.065f
        const val boostRadius = 0.055f
        const val projectileRadius = 0.03f
    }
}
