package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.awning.afterglow.Today
import com.awning.afterglow.module.AreaMapper
import com.awning.afterglow.module.edusystem.api.Timetable
import com.awning.afterglow.module.edusystem.api.TimetableItem
import com.awning.afterglow.navroute.BottomRoute
import com.awning.afterglow.store.Lighting
import com.awning.afterglow.ui.component.TimetableContent
import com.awning.afterglow.ui.component.itemMargin
import com.awning.afterglow.ui.component.itemsGrouped
import com.awning.afterglow.ui.component.scheduleWidth
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.ModuleController
import com.awning.afterglow.viewmodel.controller.SettingController
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val username by SettingController.lastUsernameFlow().collectAsState(initial = null)
    val timetable by ModuleController.timetableFlow(username).collectAsState(initial = null)

    val currentWeek by SettingController.currentWeekFlow().collectAsState(initial = 0)

    val weekPagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2) { Int.MAX_VALUE }
    val weekOffset by remember { derivedStateOf { weekPagerState.currentPage - weekPagerState.initialPage } }

    var weekPickerVisible by remember { mutableStateOf(false) }
    var showCourseInfo by remember { mutableStateOf(false) }
    var visibleCourseInfo by remember { mutableStateOf<List<TimetableItem>>(emptyList()) }
    var visibleCourseRow by remember { mutableIntStateOf(0) }
    var visibleCourseColumn by remember { mutableIntStateOf(0) }
    val days = listOf("一", "二", "三", "四", "五", "六", "日")

    Scaffold(
        topBar = {
            TimetableAppBar(
                currentWeek,
                weekOffset,
                {
                    coroutineScope.launch {
                        weekPagerState.scrollToPage(weekPagerState.initialPage)
                        Toast.makeText(context, "欢迎回来", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                weekPickerVisible = true
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 头
            WeekHeader(days, weekOffset)
            TableContent(
                currentWeek,
                timetable,
                weekPagerState
            ) { row, column, course ->
                visibleCourseRow = row
                visibleCourseColumn = column
                visibleCourseInfo = course
                showCourseInfo = true
            }
        }

        if (weekPickerVisible) {
            WeekPicker(
                selectedWeek = currentWeek + weekOffset,
                onDismiss = { weekPickerVisible = false }
            ) {
                weekPickerVisible = false
                coroutineScope.launch {
                    weekPagerState.scrollToPage(weekPagerState.initialPage + it - currentWeek)
                }
            }
        }


        if (showCourseInfo) {
            CourseInfoDialog(days[visibleCourseRow], visibleCourseColumn, visibleCourseInfo) {
                showCourseInfo = false
            }
        }
    }
}


fun generateDates(localDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()

    val dayOfWeek = localDate.dayOfWeek.value
    for (i in 0 until 7) {
        val date = localDate.plusDays((i + 1 - dayOfWeek).toLong())
        dates.add(date)
    }

    return dates
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekHeader(
    days: List<String>,
    weekOffset: Int
) {
    val offsetDate = remember(weekOffset) { Today.plusWeeks(weekOffset.toLong()) }
    var dates by remember { mutableStateOf<List<LocalDate>?>(null) }

    LaunchedEffect(key1 = weekOffset) {
        dates = generateDates(offsetDate)
    }

    Column {
        Row {
            // 空白
            Spacer(
                modifier = Modifier
                    .width(scheduleWidth)
                    .padding(itemMargin)
            )

            // 周几
            days.forEachIndexed { index, day ->
                val isToday = (weekOffset == 0 && index + 1 == Today.dayOfWeek.value)

                Card(
                    onClick = {},
                    colors = if (isToday) CardDefaults.elevatedCardColors() else CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    elevation = if (isToday) CardDefaults.cardElevation(3.dp) else CardDefaults.cardElevation(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(itemMargin)
                ) {
                    HeaderItem(day, dates?.get(index))
                }
            }
        }

        // 分割线
        Spacer(
            modifier = Modifier
                .padding(top = 3.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}


@Composable
fun HeaderItem(day: String, date: LocalDate?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(text = day)
        Text(
            text = "${date?.month?.value ?: ""}/${date?.dayOfMonth ?: ""}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TableContent(
    currentWeek: Int,
    timetable: Timetable?,
    pagerState: PagerState,
    onItemClick: (row: Int, column: Int, course: List<TimetableItem>) -> Unit
) {
    HorizontalPager(state = pagerState, beyondBoundsPageCount = 1) {
        val week = currentWeek + it - pagerState.initialPage

        TimetableContent(
            timetable = timetable,
            week = week,
            onItemClick = onItemClick
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPicker(selectedWeek: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var initial by remember { mutableIntStateOf(selectedWeek) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "关闭")
            }
        },
        title = { Text(text = "周次选择") },
        text = {
            LazyColumn {
                itemsGrouped(5, list = (1..20).toList()) {
                    InputChip(
                        selected = it == initial,
                        onClick = {
                            initial = it
                            onConfirm(initial)
                        },
                        label = { Text(text = it.toString()) }
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseInfoDialog(
    dayOfWeek: String,
    column: Int,
    course: List<TimetableItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "关闭")
            }
        },
        title = { Text(text = "课程详情 周$dayOfWeek ${column * 2 + 1}-${column * 2 + 2}节") },
        text = {
            LazyColumn {
                course.forEach { timetableItem ->
                    item {
                        ElevatedCard(
                            onClick = {},
                            modifier = Modifier.padding(halfOfPadding),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(twiceOfPadding)
                            ) {
                                Text(
                                    text = timetableItem.name,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = halfOfPadding)
                                )
                                Text(text = timetableItem.teacher)
                                Text(text = AreaMapper.mapArea(timetableItem.area))
                                Text(text = timetableItem.rawWeeks)
                            }
                        }
                    }
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableAppBar(
    currentWeek: Int,
    weekOffset: Int,
    onBackToday: () -> Unit,
    onShowWeekPicker: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    Lighting.Sapphire = !Lighting.Sapphire
                }
            ) {
                Icon(
                    imageVector = BottomRoute.Timetable.icon,
                    contentDescription = "NavIcon",
                    tint = if (Lighting.Sapphire) Lighting.MColor.Sapphire else Color.LightGray
                )
            }
        },
        actions = {
            IconButton(onClick = onBackToday) {
                Icon(
                    imageVector = Icons.Rounded.Today,
                    contentDescription = "Today"
                )
            }
            IconButton(onClick = onShowWeekPicker) {
                Icon(
                    imageVector = Icons.Rounded.ViewWeek,
                    contentDescription = "ViewWeek"
                )
            }
        },
        title = {
            Text(
                text = buildAnnotatedString {
                    append("$Today 第${currentWeek}周 🚴 ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("${currentWeek + weekOffset}")
                    }
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
}