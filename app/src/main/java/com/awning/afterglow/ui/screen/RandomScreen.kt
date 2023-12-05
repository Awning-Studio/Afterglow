package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.borderOfSurfaceVariant
import com.awning.afterglow.ui.component.AfterglowTextFiled
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.RadioButton
import com.awning.afterglow.ui.component.RunAndResetButton
import com.awning.afterglow.ui.component.TitleText
import com.awning.afterglow.ui.component.itemsGrouped
import com.awning.afterglow.ui.component.limitTextSize
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding


private val filteredNum = mutableStateListOf<Int>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var filterDialogVisible by remember { mutableStateOf(false) }
    var resultBottomSheetVisible by remember { mutableStateOf(false) }

    var first by remember { mutableIntStateOf(1) }
    var second by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(1) }
    var single by remember { mutableStateOf(true) }
    val result = remember { mutableStateListOf<Int>() }

    MTopAppBar(title = ModuleRoute.ModuleRandom.title, navController = navController) {
        LazyColumn(
            contentPadding = PaddingValues(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
            }
            item {
                Row {
                    AfterglowTextFiled(
                        value = if (first == 0) "" else first.toString(),
                        onValueChange = { first = if (it.isBlank()) 0 else it.toInt() },
                        modifier = Modifier.weight(1f),
                        numberOnly = true,
                        label = { Text(text = "一个数") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(padding))
                    AfterglowTextFiled(
                        value = if (second == 0) "" else second.toString(),
                        onValueChange = { second = if (it.isBlank()) 0 else it.toInt() },
                        modifier = Modifier.weight(1f),
                        numberOnly = true,
                        label = { Text(text = "另一个数") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(padding))
                    AfterglowTextFiled(
                        value = if (count == 0) "" else count.toString(),
                        onValueChange = {
                            count = if (it.isBlank()) 0 else limitTextSize(it, 3).toInt()
                        },
                        modifier = Modifier.weight(1f),
                        numberOnly = true,
                        label = { Text(text = "数量") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(padding))
                }
                Spacer(modifier = Modifier.height(padding))
            }
            item {
                RadioButton(label = "唯一", selected = single) {
                    single = it
                }
                Spacer(modifier = Modifier.height(padding))
            }
            item {
                OutlinedCard(
                    onClick = { filterDialogVisible = true },
                    border = borderOfSurfaceVariant()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(twiceOfPadding)
                    ) {
                        Text(text = "过滤", color = MaterialTheme.colorScheme.primary)

                        val height by animateDpAsState(
                            targetValue = if (filteredNum.size == 0) 0.dp else 100.dp,
                            label = "FilterPanel"
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height)
                        ) {
                            itemsGrouped(5, filteredNum) {
                                ElevatedFilterChip(
                                    selected = true,
                                    onClick = { filteredNum.remove(it) },
                                    label = { Text(text = it.toString()) },
                                    modifier = Modifier.padding(horizontal = halfOfPadding)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(padding))
            }
            item {
                RunAndResetButton(
                    onReset = {
                        focusManager.clearFocus()
                        first = 1
                        second = 0
                        count = 1
                        single = true
                        result.clear()
                        filteredNum.clear()
                    }
                ) {
                    focusManager.clearFocus()
                    if (count == 0) {
                        Toast.makeText(context, "请输入个数", Toast.LENGTH_SHORT).show()
                    } else {
                        val range = if (first > second) {
                            second..first
                        } else {
                            first..second
                        }

                        var filteredCount = 0
                        filteredNum.forEach {
                            if (it in range) {
                                filteredCount++
                            }
                        }

                        if (single) {
                            val rangeCount = range.count()
                            if (rangeCount - filteredCount < count) {
                                Toast.makeText(
                                    context,
                                    "已自动调整数量",
                                    Toast.LENGTH_SHORT
                                ).show()
                                count = rangeCount - filteredCount
                            }
                            result.clear()

                            while (result.size < count) {
                                val random = range.random()
                                if (!result.contains(random) && !filteredNum.contains(random)) {
                                    result.add(random)
                                }
                            }
                        } else {
                            result.clear()
                            while (result.size < count) {
                                val random = range.random()
                                if (!filteredNum.contains(random)) {
                                    result.add(random)
                                }
                            }
                        }
                        resultBottomSheetVisible = true
                        Toast.makeText(context, "已生成", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    FilterDialog(filterDialogVisible) {
        filterDialogVisible = false
    }

    ResultBottomSheet(
        visible = resultBottomSheetVisible,
        onDismiss = { resultBottomSheetVisible = false },
        result = result,
        filter = filteredNum
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    result: List<Int>,
    filter: List<Int>
) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss
        ) {
            LazyColumn(
                contentPadding = PaddingValues(twiceOfPadding),
                modifier = Modifier.height(500.dp)
            ) {
                item {
                    TitleText(text = "过滤")
                }
                if (filter.isEmpty()) {
                    item { EmptyContent() }
                } else {
                    itemsGrouped(5, filter) {
                        ElevatedFilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = it.toString()) },
                            modifier = Modifier.padding(horizontal = halfOfPadding)
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(padding))
                }
                item {
                    TitleText(text = "结果")
                }
                itemsGrouped(5, result) {
                    ElevatedFilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text(text = it.toString()) },
                        modifier = Modifier.padding(horizontal = halfOfPadding)
                    )
                }
            }
        }
    }
}


@Composable
private fun FilterDialog(visible: Boolean, onDismiss: () -> Unit) {
    if (visible) {
        var filter by remember { mutableStateOf("") }
        val regex = Regex("\\D")

        AlertDialog(
            title = { Text(text = "过滤的号码") },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    if (filter.isNotBlank()) {
                        filter.split(regex).forEach {
                            try {
                                val num = it.toInt()
                                if (!filteredNum.contains(num)) {
                                    filteredNum.add(num)
                                }
                            } catch (_: Exception) { }
                        }
                    }
                    onDismiss()
                }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "取消")
                }
            },
            text = {
                Column {
                    AfterglowTextFiled(value = filter, onValueChange = { filter = it })
                    Text(
                        text = "可以用除数字外的其他一个或多个字符隔开",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
    }
}