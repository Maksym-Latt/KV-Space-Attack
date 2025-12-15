package com.chicken.spaceattack.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.chicken.spaceattack.R

@Composable
fun StrokeLabel(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
    fill: Color = MaterialTheme.colorScheme.secondary,
    outline: Color = Color.Black,
    strokeWidth: Float = 6f,
    textAlign: TextAlign = TextAlign.Center,
    letterSpacing: TextUnit = 2.sp
) {
    val typeface = FontFamily(Font(R.font.luckiestguy_regular))

    val baseStyle = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        fontFamily = typeface,
        textAlign = textAlign,
        letterSpacing = letterSpacing
    )

    Box(modifier = modifier) {
        Text(
            text = text,
            color = outline,
            style = baseStyle.copy(
                drawStyle = Stroke(width = strokeWidth)
            )
        )
        Text(
            text = text,
            color = fill,
            style = baseStyle
        )
    }
}
