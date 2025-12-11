package com.chicken.spaceattack.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.chicken.spaceattack.R
import com.chicken.spaceattack.domain.config.GameConfig
import com.chicken.spaceattack.domain.model.BoostType
import com.chicken.spaceattack.domain.model.EnemyType
import com.chicken.spaceattack.domain.model.Position
import com.chicken.spaceattack.ui.components.CircleIconButton
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton
import kotlin.math.min

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onAppPaused(true)
                Lifecycle.Event.ON_START -> viewModel.onAppPaused(false)
                else -> Unit
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF030616),
                            Color(0xFF050B24)
                        )
                    )
                )
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val density = LocalDensity.current
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()

                val positioned: (Float, Float, Dp, Dp) -> Modifier =
                    { xPercent, yPercent, width, height ->
                        val widthOffset = with(density) { width.toPx() }
                        val heightOffset = with(density) { height.toPx() }
                        Modifier.offset {
                            IntOffset(
                                x = (xPercent * widthPx - widthOffset / 2f).toInt(),
                                y = (yPercent * heightPx - heightOffset / 2f).toInt()
                            )
                        }
                    }

                GameBackground()

                GameContent(
                    state = state,
                    positioned = positioned,
                    onDrag = { delta -> viewModel.movePlayer(delta / widthPx) },
                    onPause = { viewModel.togglePause() }
                )
            }

            AnimatedVisibility(visible = state.phase == GamePhase.PAUSED) {
                PauseOverlay(
                    isSoundOn = true,
                    isMusicOn = true,
                    onQuit = onBackToMenu,
                    onResume = { viewModel.togglePause() }
                )
            }

            AnimatedVisibility(visible = state.phase == GamePhase.LOST || state.phase == GamePhase.WON) {
                GameOverOverlay(
                    won = state.phase == GamePhase.WON,
                    score = state.score,
                    onRetry = { viewModel.retry() },
                    onMenu = onBackToMenu
                )
            }
        }
    }
}

@Composable
private fun GameBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF02051A),
                        Color(0xFF050A23)
                    )
                )
            )
    )
}

@Composable
private fun GameContent(
    state: GameUiState,
    positioned: (Float, Float, Dp, Dp) -> Modifier,
    onDrag: (Float) -> Unit,
    onPause: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    onDrag(dragAmount)
                }
            }
    ) {
        Hud(
            state = state,
            onPause = onPause
        )

        state.enemies.forEach { enemy ->
            val size = when (enemy.type) {
                EnemyType.SMALL -> 68.dp
                EnemyType.MEDIUM -> 72.dp
                EnemyType.BOSS -> 216.dp
            }
            Image(
                painter = painterResource(id = enemy.type.sprite),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.TopStart)
                    .then(positioned(enemy.position.x, enemy.position.y, size, size))
            )
        }

        state.playerProjectiles.forEach { projectile ->
            val size = 44.dp
            Image(
                painter = painterResource(id = projectile.sprite),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.TopStart)
                    .then(positioned(projectile.position.x, projectile.position.y, size, size))
            )
        }

        state.enemyProjectiles.forEach { projectile ->
            val size = 44.dp
            Image(
                painter = painterResource(id = projectile.sprite),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.TopStart)
                    .then(positioned(projectile.position.x, projectile.position.y, size, size))
            )
        }

        state.boosts.forEach { boost ->
            val size = 40.dp
            Image(
                painter = painterResource(id = boost.type.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.TopStart)
                    .then(positioned(boost.position.x, boost.position.y, size, size))
            )
        }

        PlayerSprite(
            state = state,
            positioned = positioned
        )

        if (state.bossHealth > 0f) {
            BossHealthBar(
                progress = state.bossHealth.coerceIn(0f, 1f)
            )
        }

        if (GameConfig.Collision.showDebug) {
            ColliderDebugOverlay(state = state)
        }
    }
}


@Composable
private fun Hud(
    state: GameUiState,
    onPause: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val icon = if (index < state.lives) {
                        R.drawable.egg_life
                    } else {
                        R.drawable.egg_crack
                    }

                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        color = Color(0xFFFFA800),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedText(
                    text = state.score.toString(),
                    style = MaterialTheme.typography.displaySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CircleIconButton(
                icon = R.drawable.ic_launcher_foreground,
                onClick = onPause
            )
        }

        ActiveBoostIndicator(state = state)
    }
}

@Composable
private fun ActiveBoostIndicator(state: GameUiState) {
    val boost = state.activeBoost ?: return
    val boostName = boost.name.lowercase().replaceFirstChar { it.titlecase() }
    val description = when (boost) {
        BoostType.NUCLEAR -> "Shots left: ${state.nuclearShotsRemaining}"
        else -> "Time: ${(state.activeBoostRemaining / 1000f).coerceAtLeast(0f).let { String.format("%.1fs", it) }}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0x3300C4FF), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = boost.icon),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 8.dp)
        )
        Column {
            OutlinedText(text = "Bonus: $boostName", style = MaterialTheme.typography.bodyLarge)
            OutlinedText(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BoxScope.PlayerSprite(
    state: GameUiState,
    positioned: (Float, Float, Dp, Dp) -> Modifier
) {
    val spriteRes = if (state.shieldActive) {
        R.drawable.player_shield
    } else {
        R.drawable.player_game
    }

    val size = 120.dp
    Image(
        painter = painterResource(id = spriteRes),
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .align(Alignment.TopStart)
            .then(positioned(state.playerX, 0.94f, size, size))
    )
}

@Composable
private fun ColliderDebugOverlay(state: GameUiState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val minDimension = min(size.width, size.height)
        val playerRadius = GameConfig.Collision.playerRadius * GameConfig.Collision.colliderScale * minDimension
        val enemyRadius = GameConfig.Collision.enemyRadius * GameConfig.Collision.colliderScale * minDimension
        val boostRadius = GameConfig.Collision.boostRadius * GameConfig.Collision.colliderScale * minDimension
        val projectileRadius = GameConfig.Collision.projectileRadius * GameConfig.Collision.colliderScale * minDimension

        fun positionToOffset(position: Position) = Offset(position.x * size.width, position.y * size.height)

        drawCircle(
            color = Color.Green,
            radius = playerRadius,
            center = positionToOffset(Position(state.playerX, 0.92f)),
            style = Stroke(width = 2f)
        )

        state.enemies.forEach { enemy ->
            drawCircle(
                color = Color.Red,
                radius = enemyRadius,
                center = positionToOffset(enemy.position),
                style = Stroke(width = 2f)
            )
        }

        state.playerProjectiles.forEach { projectile ->
            drawCircle(
                color = Color.Cyan,
                radius = projectileRadius,
                center = positionToOffset(projectile.position),
                style = Stroke(width = 2f)
            )
        }

        state.enemyProjectiles.forEach { projectile ->
            drawCircle(
                color = Color.Magenta,
                radius = projectileRadius,
                center = positionToOffset(projectile.position),
                style = Stroke(width = 2f)
            )
        }

        state.boosts.forEach { boost ->
            drawCircle(
                color = Color.Yellow,
                radius = boostRadius,
                center = positionToOffset(boost.position),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun PauseOverlay(
    isSoundOn: Boolean,
    isMusicOn: Boolean,
    onQuit: () -> Unit,
    onResume: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedText(
                text = "PAUSED",
                style = MaterialTheme.typography.displayMedium
            )

            PrimaryButton(
                text = "Quit",
                background = Color.Red,
                onClick = onQuit,
                modifier = Modifier.padding(top = 12.dp)
            )

            PrimaryButton(
                text = "Resume",
                background = MaterialTheme.colorScheme.secondary,
                onClick = onResume,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun GameOverOverlay(
    won: Boolean,
    score: Int,
    onRetry: () -> Unit,
    onMenu: () -> Unit
) {
    val title = if (won) {
        "EGG-CELLENT!"
    } else {
        "FRIED CHICKEN..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedText(
                text = title,
                style = MaterialTheme.typography.displayLarge
            )

            OutlinedText(
                text = score.toString(),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            PrimaryButton(
                text = "Try again",
                onClick = onRetry,
                background = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            PrimaryButton(
                text = "Main Menu",
                onClick = onMenu,
                background = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun BossHealthBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 70.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Red,
                                Color.Yellow
                            )
                        )
                    )
            )
        }
    }
}
