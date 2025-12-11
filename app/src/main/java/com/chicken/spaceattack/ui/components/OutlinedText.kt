package com.chicken.spaceattack.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun OutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    fill: Color = MaterialTheme.colorScheme.secondary,
    outline: Color = Color.Black,
    strokeWidth: Float = 6f,
    textAlign: TextAlign = TextAlign.Center
) {
    Box(modifier = modifier) {
        Text(
            text = text,
            style = style.copy(
                color = outline,
                textAlign = textAlign,
                fontWeight = FontWeight.Black,
                drawStyle = Stroke(width = strokeWidth)
            ),
            textAlign = textAlign
        )
        Text(
            text = text,
            style = style.copy(color = fill, textAlign = textAlign, fontWeight = FontWeight.Black),
            textAlign = textAlign
        )
    }
}
