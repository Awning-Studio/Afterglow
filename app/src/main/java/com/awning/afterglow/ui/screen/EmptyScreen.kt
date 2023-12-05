package com.awning.afterglow.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun EmptyScreen(desc: @Composable () -> Unit = { Text(text = "空的", fontSize = 16.sp) }) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        desc()
    }
}


@Composable
fun EmptyContent(
    desc: @Composable () -> Unit = {
        Text(
            text = "空的",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
) {
    desc()
}