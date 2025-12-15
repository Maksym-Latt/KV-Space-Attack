package com.chicken.spaceattack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonType {
    ORANGE,
    RED
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.ORANGE,
    fontSize: TextUnit = 26.sp,
    onClick: () -> Unit
) {
    val (topColor, bottomColor, shadowColor) = when (type) {
        ButtonType.ORANGE -> Triple(
            Color(0xFFFFA726),
            Color(0xFFF57C00),
            Color(0xFF975500)
        )
        ButtonType.RED -> Triple(
            Color(0xFFFF6B6B),
            Color(0xFFC62828),
            Color(0xFF7A0000)
        )
    }

    Box(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    listOf(topColor, bottomColor)
                )
            )
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(50),
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 6.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x00FFFFFF),
                            Color(0x33FFFFFF)
                        )
                    ),
                    RoundedCornerShape(50)
                )
        )

        StrokeLabel(
            text = text,
            fill = Color.White,
            fontSize = fontSize
        )
    }
}