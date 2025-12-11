package com.chicken.spaceattack.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.chicken.spaceattack.R
import com.chicken.spaceattack.domain.model.EnemyType
import com.chicken.spaceattack.ui.components.CircleIconButton
import com.chicken.spaceattack.ui.components.OutlinedText
import com.chicken.spaceattack.ui.components.PrimaryButton

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
                val widthPx = constraints.maxWidth
                val heightPx = constraints.maxHeight
                val density = LocalDensity.current

                val toOffsetX: (Float) -> Dp = { percent ->
                    with(density) { (percent * widthPx).toDp() }
                }
                val toOffsetY: (Float) -> Dp = { percent ->
                    with(density) { (percent * heightPx).toDp() }
                }

                GameBackground()

                GameContent(
                    state = state,
                    toOffsetX = toOffsetX,
                    toOffsetY = toOffsetY,
                    onDrag = { delta -> viewModel.movePlayer(delta / widthPx) },
                    onPause = { viewModel.togglePause() },
                    onCollectBoost = { viewModel.collectBoost(it) }
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
    toOffsetX: (Float) -> Dp,
    toOffsetY: (Float) -> Dp,
    onDrag: (Float) -> Unit,
    onPause: () -> Unit,
    onCollectBoost: (String) -> Unit
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
            val size = if (enemy.type == EnemyType.BOSS) 180.dp else 54.dp
            Image(
                painter = painterResource(id = enemy.type.sprite),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .align(Alignment.TopStart)
                    .padding(
                        start = toOffsetX(enemy.position.x),
                        top = toOffsetY(enemy.position.y)
                    )
            )
        }

        state.playerProjectiles.forEach { projectile ->
            Image(
                painter = painterResource(id = projectile.sprite),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopStart)
                    .padding(
                        start = toOffsetX(projectile.position.x),
                        top = toOffsetY(projectile.position.y)
                    )
            )
        }

        state.enemyProjectiles.forEach { projectile ->
            Image(
                painter = painterResource(id = projectile.sprite),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopStart)
                    .padding(
                        start = toOffsetX(projectile.position.x),
                        top = toOffsetY(projectile.position.y)
                    )
            )
        }

        state.boosts.forEach { boost ->
            Image(
                painter = painterResource(id = boost.type.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopStart)
                    .padding(
                        start = toOffsetX(boost.position.x),
                        top = toOffsetY(boost.position.y)
                    )
                    .pointerInput(boost.id) {
                        detectTapGestures {
                            onCollectBoost(boost.id)
                        }
                    }
            )
        }

        with(this) {
            PlayerSprite(
                state = state,
                toOffsetX = toOffsetX
            )
        }

        if (state.bossHealth > 0f) {
            BossHealthBar(
                progress = state.bossHealth.coerceIn(0f, 1f)
            )
        }
    }
}

@Composable
private fun Hud(
    state: GameUiState,
    onPause: () -> Unit
) {
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
}

@Composable
private fun BoxScope.PlayerSprite(
    state: GameUiState,
    toOffsetX: (Float) -> Dp
) {
    val spriteRes = if (state.shieldActive) {
        R.drawable.player_shield
    } else {
        R.drawable.player_game
    }

    Image(
        painter = painterResource(id = spriteRes),
        contentDescription = null,
        modifier = Modifier
            .size(120.dp)
            .align(Alignment.BottomStart)
            .padding(
                start = toOffsetX(state.playerX),
                bottom = 24.dp
            )
    )
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
