package com.chicken.spaceattack.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CapsuleIconButton(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFA726),
                        Color(0xFFF57C00)
                    )
                )
            )
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(50),
                clip = false,
                ambientColor = Color(0xFFCC6E00),
                spotColor = Color(0xFFCC6E00)
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

        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
