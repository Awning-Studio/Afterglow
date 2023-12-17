package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.SportsHandball
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.api.TeachingEvaluationItem
import com.awning.afterglow.module.edusystem.api.evaluateTeaching
import com.awning.afterglow.module.edusystem.api.getTeachingEvaluationList
import com.awning.afterglow.module.webvpn.WebVPN
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.request.waterfall.Waterfall
import com.awning.afterglow.type.User
import com.awning.afterglow.ui.component.AfterglowTextFiled
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.PasswordOutlinedTextField
import com.awning.afterglow.ui.component.RadioButton
import com.awning.afterglow.ui.component.limitTextSize
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Composable
fun TeachingEvaluationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var helpEvaluationBottomSheetVisible by remember { mutableStateOf(false) }

    val list = remember { mutableStateListOf<TeachingEvaluationItem>() }
    var isLoading by remember { mutableStateOf(false) }

    MTopAppBar(
        title = ModuleRoute.ModuleTeachingEvaluation.title,
        navController = navController,
        actions = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        suspend fun repeat() {
                            if (list.isNotEmpty()) {
                                RuntimeVM.eduSystem?.evaluateTeaching(list[0])?.collect {
                                    list.removeAt(0)
                                    repeat()
                                }
                            } else {
                                Toast.makeText(context, "已完成，请注意检查", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        if (list.isNotEmpty()) {
                            Toast.makeText(context, "正在评教中...", Toast.LENGTH_SHORT).show()
                            repeat()
                        } else {
                            Toast.makeText(context, "列表为空", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Rounded.DoneAll, contentDescription = "DoneAll")
            }
        }
    ) {
        if (RuntimeVM.eduSystem == null) {
            EmptyScreen { Text(text = "还未登录") }
        } else {
            LaunchedEffect(key1 = Unit) {
                RuntimeVM.eduSystem?.let { eduSystem ->
                    isLoading = true
                    eduSystem.getTeachingEvaluationList().catch {
                        isLoading = false
                        Toast.makeText(
                            context,
                            "获取失败: ${it.message ?: it.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.collect {
                        isLoading = false
                        list.addAll(it)
                    }
                }
            }

            if (list.isEmpty()) {
                if (isLoading) EmptyScreen { Text(text = "加载中") } else EmptyScreen()
            } else {
                EvaluationList(list = list)
            }
        }

        // “帮你评”浮动按钮
        FloatingActionButton(
            onClick = { helpEvaluationBottomSheetVisible = true }, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 90.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SportsHandball,
                contentDescription = "SportsHandball"
            )
        }

        // “帮你评”弹窗
        HelpEvaluationBottomSheet(visible = helpEvaluationBottomSheetVisible) {
            helpEvaluationBottomSheetVisible = false
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EvaluationList(list: SnapshotStateList<TeachingEvaluationItem>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.padding(halfOfPadding)
    ) {
        items(list.size) { index ->
            ElevatedCard(
                onClick = {},
                modifier = Modifier.padding(halfOfPadding)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(twiceOfPadding)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = list[index].name,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = list[index].sort,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = padding)
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    RuntimeVM.eduSystem?.evaluateTeaching(list[index], true)
                                        ?.collect {
                                            list.removeAt(index)
                                            Toast.makeText(
                                                context,
                                                "已评价（良）",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ThumbDown,
                                contentDescription = "ThumbDown",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    RuntimeVM.eduSystem?.evaluateTeaching(list[index])?.collect {
                                        list.removeAt(index)
                                        Toast.makeText(
                                            context,
                                            "已评价（优）",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ThumbUp,
                                contentDescription = "ThumbUp",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * “帮你评”
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpEvaluationBottomSheet(visible: Boolean, onDismiss: () -> Unit) {
    if (visible) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var withWebVPN by remember { mutableStateOf(false) }
        var eduSystem by remember { mutableStateOf<EduSystem?>(null) }
        val list = remember { mutableStateListOf<TeachingEvaluationItem>() }
        val sheetState = rememberModalBottomSheetState()
        val height = 500.dp

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            modifier = Modifier.height(height)
        ) {
            Column(
                modifier = Modifier.height(height)
            ) {
                Text(
                    text = "帮你评",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = twiceOfPadding)
                )
                if (eduSystem == null) {
                    Column(
                        modifier = Modifier.padding(twiceOfPadding)
                    ) {
                        // 登录
                        AfterglowTextFiled(
                            value = username,
                            onValueChange = {
                                username = limitTextSize(it, 11)
                            },
                            label = { Text(text = "学号") },
                            numberOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = padding)
                        )
                        PasswordOutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                            },
                            label = { Text(text = "密码（门户）") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = padding)
                        )
                        RadioButton(
                            label = "WebVPN",
                            selected = withWebVPN,
                            onCheckedChange = { withWebVPN = it })
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(twiceOfPadding)
                        ) {
                            Button(
                                onClick = {
                                    username = ""
                                    password = ""
                                }
                            ) {
                                Text(text = "重置")
                            }
                            Spacer(modifier = Modifier.width(twiceOfPadding * 2))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        if (withWebVPN) {
                                            WebVPN.login(
                                                User(
                                                    username,
                                                    password,
                                                    username,
                                                    Waterfall.Session()
                                                )
                                            ).catch {
                                                Toast.makeText(
                                                    context,
                                                    it.message ?: it.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }.collect { webVPN ->
                                                EduSystem.login(webVPN).catch {
                                                    Toast.makeText(
                                                        context,
                                                        "登录失败: ${it.message ?: it.toString()}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }.collect {
                                                    Toast.makeText(
                                                        context,
                                                        "登录成功",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                    eduSystem = it
                                                    it.getTeachingEvaluationList().catch {
                                                        Toast.makeText(
                                                            context,
                                                            "获取失败: ${it.message ?: it.toString()}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }.collect {
                                                        list.addAll(it)
                                                    }
                                                }
                                            }
                                        } else {
                                            EduSystem.login(
                                                User(
                                                    username,
                                                    password,
                                                    username,
                                                    Waterfall.Session()
                                                )
                                            ).catch {
                                                Toast.makeText(
                                                    context,
                                                    "登录失败: ${it.message ?: it.toString()}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }.collect {
                                                Toast.makeText(
                                                    context,
                                                    "登录成功",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                                eduSystem = it
                                                it.getTeachingEvaluationList().catch {
                                                    Toast.makeText(
                                                        context,
                                                        "获取失败: ${it.message ?: it.toString()}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }.collect {
                                                    list.addAll(it)
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(text = "登录")
                            }
                        }
                    }
                } else {
                    // 评教
                    if (list.isEmpty()) {
                        EmptyScreen()
                    } else {
                        Column(
                            modifier = Modifier.height(500.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            suspend fun repeat() {
                                                if (list.isNotEmpty()) {
                                                    eduSystem?.evaluateTeaching(list[0])?.collect {
                                                        list.removeAt(0)
                                                        repeat()
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "已完成，请注意检查",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            }

                                            if (list.isNotEmpty()) {
                                                Toast.makeText(
                                                    context,
                                                    "正在评教中...",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                repeat()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "列表为空",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DoneAll,
                                        contentDescription = "DoneAll"
                                    )
                                }
                            }
                            EvaluationList(list = list)
                        }
                    }
                }
            }
        }
    }
}