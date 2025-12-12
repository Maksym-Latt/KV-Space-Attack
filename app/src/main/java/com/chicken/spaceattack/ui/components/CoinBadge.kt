package com.chicken.spaceattack.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chicken.spaceattack.R

@Composable
fun CoinBadge(coins: Int) {
    Box(
        modifier =
            Modifier.shadow(6.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            OutlinedText(
                text = coins.toString(),
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}