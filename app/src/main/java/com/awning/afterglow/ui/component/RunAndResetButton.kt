package com.awning.afterglow.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * 附带运行和重置按钮的单行小组件
 * @param onReset 重置按钮点击事件
 * @param onRun 运行按钮点击事件
 */
@Composable
fun RunAndResetButton(modifier: Modifier = Modifier, onReset: () -> Unit, onRun: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Button(onClick = onReset) {
            Icon(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = "RestartAlt"
            )
        }

        Spacer(modifier = Modifier.width(40.dp))

        Button(
            onClick = onRun
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "PlayArrow"
            )
        }
    }
}