package com.chicken.spaceattack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R

val LuckiestGuy = FontFamily(Font(R.font.luckiestguy_regular))

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 48.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 42.sp
    ),
    displaySmall = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 34.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 30.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = LuckiestGuy,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
)
