package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.awning.afterglow.module.courseselection.Course
import com.awning.afterglow.module.courseselection.CourseSelectionSystem
import com.awning.afterglow.module.courseselection.CourseSort
import com.awning.afterglow.module.courseselection.SelectionInfo
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.ScrollableTabRowWithPager
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("必修课", "选修课", "通识课", "专业课", "跨年级", "跨专业")
    val pagerState = rememberPagerState { tabs.size }

    var infoAlertDialogVisible by remember { mutableStateOf(false) }

    // TODO("一轮选课需要弹出志愿")
    var selectionInfo by remember { mutableStateOf<SelectionInfo?>(null) }
    var courseSelectionSystem by remember { mutableStateOf<CourseSelectionSystem?>(null) }

    var basicCourseTotal by remember { mutableIntStateOf(0) }
    var optionalCourseTotal by remember { mutableIntStateOf(0) }
    var generalCourseTotal by remember { mutableIntStateOf(0) }
    var specializedCourseTotal by remember { mutableIntStateOf(0) }
    var crossYearCourseTotal by remember { mutableIntStateOf(0) }
    var crossMajorCourseTotal by remember { mutableIntStateOf(0) }

    val basicCourseList = remember { mutableStateListOf<Course>() }
    val optionalCourseList = remember { mutableStateListOf<Course>() }
    val generalCourseList = remember { mutableStateListOf<Course>() }
    val specializedCourseList = remember { mutableStateListOf<Course>() }
    val crossYearCourseList = remember { mutableStateListOf<Course>() }
    val crossMajorCourseList = remember { mutableStateListOf<Course>() }

    LaunchedEffect(key1 = Unit) {
        RuntimeVM.eduSystem?.let { eduSystem ->
            CourseSelectionSystem.enter(eduSystem).catch {
                Toast.makeText(context, it.message ?: it.toString(), Toast.LENGTH_SHORT).show()
            }.collect {
                courseSelectionSystem = it.first
                selectionInfo = it.second
            }
        }
    }

    MTopAppBar(
        title = ModuleRoute.ModuleCourseSelection.title,
        actions = {
            IconButton(onClick = { infoAlertDialogVisible = true }) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = "Info")
            }
            IconButton(onClick = { /*TODO("选课历史")*/ }) {
                Icon(imageVector = Icons.Outlined.History, contentDescription = "History")
            }
            IconButton(onClick = { /*TODO("选课课表")*/ }) {
                Icon(imageVector = Icons.Rounded.TableView, contentDescription = "TableView")
            }
            IconButton(onClick = { /*TODO("刷新")*/ }) {
                Icon(imageVector = Icons.Rounded.Sync, contentDescription = "Sync")
            }
        },
        navController = navController
    ) {
        ScrollableTabRowWithPager(pagerState = pagerState, tabs = tabs) { page ->
            val list = remember {
                when (page) {
                    0 -> basicCourseList
                    1 -> optionalCourseList
                    2 -> generalCourseList
                    3 -> specializedCourseList
                    4 -> crossYearCourseList
                    5 -> crossMajorCourseList
                    else -> arrayListOf()  // 无法到达
                }
            }

            // 过滤项
            var name by remember { mutableStateOf("") }
            var teacher by remember { mutableStateOf("") }
            var campus by remember { mutableStateOf<String?>(null) }
            var section by remember { mutableIntStateOf(-1) }
            var dayOfWeek by remember { mutableIntStateOf(0) }

            val filteredList by remember {
                derivedStateOf {
                    arrayListOf<Course>().also { arrayList ->
                        if (page < 2) {
                            list.forEach {
                                if (name.isNotBlank() && it.name.contains(name)) {
                                    arrayList.add(it)
                                } else if (teacher.isNotBlank() && it.teacher.contains(name)) {
                                    arrayList.add(it)
                                }
                            }
                        }
                    }
                }
            }

            // 加载更多功能
            val lazyListState = rememberLazyListState()
            val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
            var isLoading by remember { mutableStateOf(false) }

            LaunchedEffect(key1 = firstVisibleItemIndex) {
                if (!isLoading && firstVisibleItemIndex != 0 && firstVisibleItemIndex + lazyListState.layoutInfo.visibleItemsInfo.size + 5 >= lazyListState.layoutInfo.totalItemsCount) {
                    when (page) {
                        0, 1 -> {
                            if (list.size < if (page == 0) basicCourseTotal else optionalCourseTotal) {
                                // 用 coroutineScope launch 才不会卡住不加载
                                coroutineScope.launch {
                                    courseSelectionSystem?.let { courseSelectionSystem ->
                                        isLoading = true
                                        courseSelectionSystem.getCourseList(
                                            if (page == 0) CourseSort.Basic else CourseSort.Optional,
                                            list.size / 15
                                        ).catch {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                it.message ?: it.toString(),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.collect {
                                            isLoading = false
                                            list.addAll(it.second)
                                            when (page) {
                                                0 -> basicCourseTotal = it.first
                                                1 -> optionalCourseTotal = it.first
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            TODO("加载更多")
                        }
                    }
                }
            }

            LaunchedEffect(key1 = courseSelectionSystem) {
                if (page < 3) {
                    if (list.isEmpty()) {
                        // 当列表为空时加载列表
                        courseSelectionSystem?.let { courseSelectionSystem ->
                            (if (page == 2)
                                courseSelectionSystem.searchCourse(CourseSort.General)
                            else courseSelectionSystem.getCourseList(
                                if (page == 0) CourseSort.Basic else CourseSort.Optional
                            )).catch {
                                Toast.makeText(
                                    context,
                                    it.message ?: it.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.collect {
                                list.addAll(it.second)
                                when (page) {
                                    0 -> basicCourseTotal = it.first
                                    1 -> optionalCourseTotal = it.first
                                    2 -> generalCourseTotal = it.first
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // 过滤
                InputChip(
                    selected = name.isNotBlank() && teacher.isNotBlank() && campus != null && section != -1 && dayOfWeek != 0,
                    onClick = {
                        // TODO("开启 过滤界面")
                    },
                    label = { Text(text = "过滤") },
                    modifier = Modifier.padding(horizontal = twiceOfPadding, vertical = padding)
                )
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(padding)
                ) {
                    items(list.size) {
                        ElevatedCard(
                            onClick = {},
                            modifier = Modifier.padding(padding)
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
                                        text = list[it].name,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${list[it].selectedCount}/${list[it].providedRoom}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    if (list[it].conflict.isNotBlank()) {
                                        Text(
                                            text = list[it].conflict,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = list[it].teacher,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = list[it].campus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = list[it].time,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                TextButton(
                                    enabled = !list[it].isSelected.value,
                                    onClick = { /*TODO(选课)*/ }
                                ) {
                                    Text(text = if (list[it].isSelected.value) "已选" else "选课")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    InfoAlertDialog(visible = infoAlertDialogVisible, selectionInfo = selectionInfo) {
        infoAlertDialogVisible = false
    }
}


@Composable
fun InfoAlertDialog(visible: Boolean, selectionInfo: SelectionInfo?, onDismiss: () -> Unit) {
    if (visible) {
        AlertDialog(
            title = { Text(text = "选课信息") },
            text = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(text = "轮次")
                        Text(text = selectionInfo?.name ?: "未知")
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(text = "开始")
                        Text(text = selectionInfo?.startTime ?: "未知")
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(text = "结束")
                        Text(text = selectionInfo?.endTime ?: "未知")
                    }
                }
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "关闭")
                }
            }
        )
    }
}