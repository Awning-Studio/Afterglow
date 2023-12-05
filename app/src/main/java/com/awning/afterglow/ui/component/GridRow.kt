package com.awning.afterglow.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


/**
 * 对 [list] 依据单行最大 [count] 个元素进行分组
 * @receiver LazyListScope
 * @param count Int
 * @param list List
 * @param itemContent
 */
fun <T> LazyListScope.itemsGrouped(count: Int, list: List<T>, itemContent: @Composable BoxScope.(item: T) -> Unit) {
    list.forEachGrouped(count) { group ->
        item {
            GridRow(count = count, list = group) {
                itemContent(it)
            }
        }
    }
}


/**
 * 一行从左到右等比排列
 * @param count 最大数目，不可小于 [list] 大小
 * @param list
 * @param modifier Modifier
 * @param itemContent
 */
@Composable
private fun <T> GridRow(count: Int, list: List<T>, modifier: Modifier = Modifier, itemContent: @Composable BoxScope.(item: T) -> Unit) {
    Row(modifier = modifier.fillMaxWidth()) {
        for (column in list.indices) {
            val itemWeight = 1f /(count - column)
            Box(
                modifier = Modifier.fillMaxWidth(itemWeight)
            ) {
                itemContent(list[column])
            }
        }
    }
}


/**
 * 对列表进行分组
 * @receiver List<T>
 * @param memberCount 每一组最大数
 * @param onEachGroup 分组回调
 */
fun <T> List<T>.forEachGrouped(memberCount: Int, onEachGroup: (group: List<T>) -> Unit) {
    var group = arrayListOf<T>()

    forEachIndexed { index, item ->
        if (index != 0 && index % memberCount == 0) {
            onEachGroup(group)
            group = arrayListOf()
        }
        group.add(item)
    }

    if (group.isNotEmpty()) {
        onEachGroup(group)
    }
}