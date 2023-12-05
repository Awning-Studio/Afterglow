package com.awning.afterglow.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * 图标与文字的有机结合
 * @param icon 图标
 * @param contentDescription 图标内容描述
 * @param text 文字
 * @param modifier
 * @param maxLines 最大行数
 * @param contentColor 内容颜色
 * @param size Dp 整体大小
 */
@Composable
fun IconText(
    icon: ImageVector,
    contentDescription: String?,
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    contentColor: Color = LocalContentColor.current,
    size: Dp = 20.dp
) {
    LocalContentColor.provides(contentColor)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size / 4 * 3)
        )
        Spacer(modifier = Modifier.width(size / 3))
        Text(
            text = text,
            fontSize = (size.value / 3 * 2).sp,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}