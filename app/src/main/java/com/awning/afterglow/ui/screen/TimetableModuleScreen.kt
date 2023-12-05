package com.awning.afterglow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.module.AreaMapper
import com.awning.afterglow.module.edusystem.api.Timetable
import com.awning.afterglow.module.edusystem.api.TimetableItem
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.TimetableContent
import com.awning.afterglow.ui.component.itemMargin
import com.awning.afterglow.ui.component.scheduleWidth
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.ModuleController
import com.awning.afterglow.viewmodel.controller.SettingController


@Composable
fun TimetableModuleScreen(navController: NavHostController) {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")
    var showCourseInfo by remember { mutableStateOf(false) }
    var visibleCourseInfo by remember { mutableStateOf<List<TimetableItem>>(emptyList()) }
    var visibleCourseRow by remember { mutableIntStateOf(0) }
    var visibleCourseColumn by remember { mutableIntStateOf(0) }

    val username by SettingController.lastUsernameFlow().collectAsState(initial = null)
    val timetable by ModuleController.timetableFlow(username).collectAsState(initial = null)

    MTopAppBar(
        title = ModuleRoute.ModuleTimetable.title,
        navController = navController
    ) {
        if (timetable == null) {
            EmptyScreen {
                Text(text = "空空的")
            }
        } else {
            Column {
                WeekHeader(days)
                TableContent(timetable) { row, column, course ->
                    visibleCourseRow = row
                    visibleCourseColumn = column
                    visibleCourseInfo = course
                    showCourseInfo = true
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekHeader(days: List<String>) {
    Column(
        modifier = Modifier.height(35.dp)
    ) {
        Row {
            // 空白
            Spacer(
                modifier = Modifier
                    .width(scheduleWidth)
                    .padding(itemMargin)
            )

            // 周几
            days.forEach { day ->
                Card(
                    onClick = {},
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .weight(1f)
                        .padding(itemMargin)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(text = day)
                    }
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
private fun TableContent(
    timetable: Timetable?,
    onItemClick: (row: Int, column: Int, course: List<TimetableItem>) -> Unit
) {
    TimetableContent(timetable = timetable, onItemClick = onItemClick)
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