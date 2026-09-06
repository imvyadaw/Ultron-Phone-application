package com.ultron.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Small circular "U" mark with a red glow, used wherever the old UI had nothing. */
@Composable
fun UltronMark(size: androidx.compose.ui.unit.Dp = 34.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Brush.radialGradient(listOf(UltronColors.Red, UltronColors.RedDim)),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = "ULTRON",
            tint = Color.White,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

/** Branded header row: mark + wordmark + optional trailing status dot. */
@Composable
fun UltronBrandHeader(subtitle: String? = null, connected: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        UltronMark()
        Box(Modifier.width(10.dp))
        androidx.compose.foundation.layout.Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ULTRON",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(Modifier.width(8.dp))
                StatusDot(connected)
            }
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusDot(connected: Boolean) {
    Box(
        Modifier
            .size(9.dp)
            .background(
                if (connected) UltronColors.Cyan else MaterialTheme.colorScheme.outline,
                CircleShape
            )
    )
}
