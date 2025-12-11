package com.chicken.spaceattack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .background(background, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedText(text = text, style = MaterialTheme.typography.displaySmall)
    }
}
