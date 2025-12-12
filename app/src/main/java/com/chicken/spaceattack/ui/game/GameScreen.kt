package com.chicken.spaceattack.ui.game

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.chicken.spaceattack.R
import com.chicken.spaceattack.domain.model.BoostType
import com.chicken.spaceattack.domain.model.EnemyType
import com.chicken.spaceattack.domain.model.Explosion
import com.chicken.spaceattack.ui.components.CapsuleIconButton
import com.chicken.spaceattack.ui.components.OutlinedText
import kotlin.math.roundToInt

@Composable
fun GameScreen(viewModel: GameViewModel, onBackToMenu: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Handle system back button - only open pause menu
    BackHandler(enabled = state.phase == GamePhase.RUNNING) { viewModel.togglePause() }

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

        onDispose { lifecycle.removeObserver(observer) }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                    painter = painterResource(id = R.drawable.bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
            )

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

                GameContent(
                        state = state,
                        positioned = positioned,
                        onDrag = { delta -> viewModel.movePlayer(delta / widthPx) },
                        onPause = { viewModel.togglePause() }
                )
            }

            AnimatedVisibility(visible = state.phase == GamePhase.PAUSED) {
                PauseOverlay(
                        audioController = viewModel.audioController,
                        onQuit = onBackToMenu,
                        onResume = { viewModel.togglePause() }
                )
            }

            AnimatedVisibility(
                    visible = state.phase == GamePhase.LOST || state.phase == GamePhase.WON
            ) {
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
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    brush =
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
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
            modifier =
                    Modifier.fillMaxSize().pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount -> onDrag(dragAmount) }
                    }
    ) {
        Hud(state = state, onPause = onPause)

        state.enemies.forEach { enemy ->
            val size =
                    when (enemy.type) {
                        EnemyType.SMALL -> 68.dp
                        EnemyType.MEDIUM -> 72.dp
                        EnemyType.BOSS -> 216.dp
                    }
            Image(
                    painter = painterResource(id = enemy.type.sprite),
                    contentDescription = null,
                    modifier =
                            Modifier.size(size)
                                    .align(Alignment.TopStart)
                                    .then(
                                            positioned(
                                                    enemy.position.x,
                                                    enemy.position.y,
                                                    size,
                                                    size
                                            )
                                    )
            )
        }

        state.playerProjectiles.forEach { projectile ->
            val size = 84.dp // Increased from 44dp for better visibility
            Image(
                    painter = painterResource(id = projectile.sprite),
                    contentDescription = null,
                    modifier =
                            Modifier.size(size)
                                    .align(Alignment.TopStart)
                                    .then(
                                            positioned(
                                                    projectile.position.x,
                                                    projectile.position.y,
                                                    size,
                                                    size
                                            )
                                    )
            )
        }

        state.enemyProjectiles.forEach { projectile ->
            val size = 56.dp
            Image(
                    painter = painterResource(id = projectile.sprite),
                    contentDescription = null,
                    modifier =
                            Modifier.size(size)
                                    .align(Alignment.TopStart)
                                    .then(
                                            positioned(
                                                    projectile.position.x,
                                                    projectile.position.y,
                                                    size,
                                                    size
                                            )
                                    )
                                    .rotate(180f)
            )
        }

        // Render explosions with a small burst animation
        state.explosions.forEach { explosion ->
            ExplosionSprite(explosion = explosion, positioned = positioned)
        }

        state.boosts.forEach { boost ->
            val size = 40.dp
            Image(
                    painter = painterResource(id = boost.type.icon),
                    contentDescription = null,
                    modifier =
                            Modifier.size(size)
                                    .align(Alignment.TopStart)
                                    .then(
                                            positioned(
                                                    boost.position.x,
                                                    boost.position.y,
                                                    size,
                                                    size
                                            )
                                    )
            )
        }

        PlayerSprite(state = state, positioned = positioned)
    }
}

@Composable
private fun Hud(state: GameUiState, onPause: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(WindowInsets.safeDrawing.asPaddingValues())) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val icon =
                            if (index < state.lives) {
                                R.drawable.egg_life
                            } else {
                                R.drawable.egg_crack
                            }

                    Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ScoreBadge(score = state.score)

            Spacer(modifier = Modifier.weight(1f))

            CapsuleIconButton(icon = R.drawable.ic_pause, onClick = onPause)
        }

        if (state.bossHealth > 0f) {
            Spacer(modifier = Modifier.height(4.dp))
            BossHealthBar(progress = state.bossHealth.coerceIn(0f, 1f))
            Spacer(modifier = Modifier.height(4.dp))
        }

        ActiveBoostIndicator(state = state)
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    Box(
            modifier =
                    Modifier.height(54.dp)
                            .defaultMinSize(minWidth = 110.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF19254B))
                            .border(
                                    width = 3.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
    ) {
        OutlinedText(
                text = score.toString(),
                fontSize = 30.sp,
                fill = Color(0xFFFFC107),
                modifier = Modifier.offset(y = (5).dp)
        )
    }
}

@Composable
private fun ActiveBoostIndicator(state: GameUiState) {
    val boosts = buildList {
        state.activeShotBoost?.let { boost ->
            val progress =
                    if (boost == BoostType.NUCLEAR) {
                        (state.nuclearShotsRemaining / 5f).coerceIn(0f, 1f)
                    } else {
                        (state.shotBoostRemaining / 9000f).coerceIn(0f, 1f)
                    }
            add(boost.icon to progress)
        }

        if (state.shieldActive) {
            add(BoostType.SHIELD.icon to (state.shieldRemaining / 9000f).coerceIn(0f, 1f))
        }

        if (state.slowRemaining > 0) {
            add(BoostType.SLOW_TIME.icon to (state.slowRemaining / 9000f).coerceIn(0f, 1f))
        }
    }

    if (boosts.isEmpty()) return

    Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
    ) {
        boosts.forEachIndexed { index, (iconRes, progress) ->
            BoostIcon(icon = iconRes, progress = progress)
            if (index != boosts.lastIndex) {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun BoostIcon(@DrawableRes icon: Int, progress: Float) {
    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 4.dp.toPx()
            drawCircle(color = Color(0x55000000), style = Stroke(width = strokeWidth))

            if (progress > 0f) {
                drawArc(
                        color = Color(0xFFFFD54F),
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun BoxScope.PlayerSprite(
        state: GameUiState,
        positioned: (Float, Float, Dp, Dp) -> Modifier
) {
    val spriteRes =
            if (state.shieldActive) {
                R.drawable.player_shield
            } else {
                R.drawable.player_game
            }

    val size = 120.dp
    val hitProgress =
            (state.playerHitEffectMillis.toFloat() / PLAYER_HIT_EFFECT_DURATION.toFloat()).coerceIn(
                    0f,
                    1f
            )

    if (hitProgress > 0f) {
        Box(
                modifier =
                        Modifier.size(size * 1.6f)
                                .align(Alignment.TopStart)
                                .then(positioned(state.playerX, 0.94f, size * 1.6f, size * 1.6f))
                                .graphicsLayer {
                                    alpha = 0.35f * hitProgress
                                    scaleX = 1.05f + 0.25f * hitProgress
                                    scaleY = 1.05f + 0.25f * hitProgress
                                }
                                .background(
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(Color(0xFFFF6B6B), Color(0x00FF6B6B))
                                        )
                                )
        )
    }

    Image(
            painter = painterResource(id = spriteRes),
            contentDescription = null,
            modifier =
                    Modifier.size(size)
                            .align(Alignment.TopStart)
                            .then(positioned(state.playerX, 0.94f, size, size))
                            .graphicsLayer {
                                scaleX = 1f + 0.12f * hitProgress
                                scaleY = 1f + 0.12f * hitProgress
                                alpha = 1f - 0.2f * hitProgress
                            }
    )
}

@Composable
private fun BoxScope.ExplosionSprite(
        explosion: Explosion,
        positioned: (Float, Float, Dp, Dp) -> Modifier
) {
    val size = 96.dp // Larger than projectiles for visual impact
    val progress =
            (explosion.remainingMillis.toFloat() / explosion.durationMillis.toFloat()).coerceIn(
                    0f,
                    1f
            )
    val scale = 1f + 0.35f * (1f - progress)

    Box(
            modifier =
                    Modifier.size(size * 1.4f)
                            .align(Alignment.TopStart)
                            .then(
                                    positioned(
                                            explosion.position.x,
                                            explosion.position.y,
                                            size * 1.4f,
                                            size * 1.4f
                                    )
                            )
                            .graphicsLayer { alpha = 0.35f * progress }
                            .background(
                                    Brush.radialGradient(
                                            colors = listOf(Color(0xFFFFC34D), Color.Transparent)
                                    )
                            )
    )

    Image(
            painter = painterResource(id = explosion.sprite),
            contentDescription = null,
            modifier =
                    Modifier.size(size)
                            .align(Alignment.TopStart)
                            .then(
                                    positioned(
                                            explosion.position.x,
                                            explosion.position.y,
                                            size,
                                            size
                                    )
                            )
                            .graphicsLayer {
                                this.scaleX = scale
                                this.scaleY = scale
                                alpha = 0.4f + 0.6f * progress
                                rotationZ = (1f - progress) * 14f
                            }
    )
}

@Composable
private fun BossHealthBar(progress: Float) {
    val clamped = progress.coerceIn(0f, 1f)
    val percent = (clamped * 100f).roundToInt()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
                text = "$percent/100",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.luckiestguy_regular))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x33000000))
        ) {
            if (clamped > 0f) {
                Box(
                        modifier =
                                Modifier.fillMaxHeight()
                                        .fillMaxWidth(clamped)
                                        .background(Color(0xFFB71C1C))
                )

                Box(
                        modifier =
                                Modifier.align(Alignment.CenterEnd)
                                        .width(6.dp)
                                        .fillMaxHeight()
                                        .background(Color.White)
                )
            }
        }
    }
}
