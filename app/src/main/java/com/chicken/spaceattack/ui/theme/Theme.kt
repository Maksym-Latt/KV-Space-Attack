package com.chicken.spaceattack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

private val ColorPalette = darkColorScheme(
    primary = AccentOrange,
    secondary = AccentYellow,
    tertiary = AccentPurple,
    background = MidnightBlue,
    surface = DeepBlue,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun ChickenWarsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorPalette,
        typography = Typography,
        content = content
    )
}
