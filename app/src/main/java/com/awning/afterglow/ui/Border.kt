package com.awning.afterglow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val NoBorder = BorderStroke(0.dp, Color.Transparent)

@Composable
fun borderOfSurfaceVariant(width: Dp = 0.5.dp, alpha: Float = 1f) =
    BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha))

@Composable
fun borderOfGray(width: Dp = 0.5.dp, alpha: Float = 1f) =
    BorderStroke(width, Color.LightGray.copy(alpha))


@Composable
fun borderOfPrimary(width: Dp = 0.5.dp, alpha: Float = 1f) =
    BorderStroke(width, MaterialTheme.colorScheme.primary.copy(alpha))