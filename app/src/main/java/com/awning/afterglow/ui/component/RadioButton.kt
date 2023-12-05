package com.awning.afterglow.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


/**
 * 带 [label] 的 [RadioButton]
 * @param label 标签
 * @param selected 是否选中
 * @param modifier Modifier
 * @param enabled 是否可操作
 * @param onCheckedChange 在选中状态改变时调用
 */
@Composable
fun RadioButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        RadioButton(
            enabled = enabled,
            selected = selected,
            onClick = { onCheckedChange(!selected) }
        )
        Text(text = label)
    }
}