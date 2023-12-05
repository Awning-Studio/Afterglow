package com.awning.afterglow.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.awning.afterglow.module.AreaMapper
import com.awning.afterglow.module.edusystem.api.CourseInfo
import com.awning.afterglow.module.edusystem.api.TimetableAllItem
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.borderOfSurfaceVariant
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.SearchBar
import com.awning.afterglow.ui.component.TabRowWithPager
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.SettingController
import com.awning.afterglow.viewmodel.controller.TimetableAllController
import java.time.LocalDate

val days = listOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BorrowCourseScreen(navController: NavHostController) {
    val dayOfWeek = LocalDate.now().dayOfWeek.value
    val tomorrowDayOfWeek = if (dayOfWeek == 7) 1 else dayOfWeek + 1
    val currentWeek by SettingController.currentWeekFlow().collectAsState(initial = 0)
    val tomorrowWeek by remember { derivedStateOf { if (dayOfWeek == 7) currentWeek + 1 else currentWeek } }

    val timetableAll by TimetableAllController.timetableAllFlow().collectAsState(initial = null)

    var courseName by remember { mutableStateOf("") }
    var filterKeyword by remember { mutableStateOf("") }

    var visibleCourseName by remember { mutableStateOf<String>("") }
    var visibleCourseInfoList by remember { mutableStateOf<List<CourseInfo>>(emptyList()) }
    var courseInfoBottomSheetVisible by remember { mutableStateOf(false) }

    val tabs = listOf("今天", "明天", "全部")
    val pagerState = rememberPagerState { tabs.size }

    MTopAppBar(
        title = ModuleRoute.ModuleBorrowCourse.title,
        navController = navController
    ) {
        if (timetableAll == null) {
            EmptyScreen()
        } else {
            Column {
                SearchBar(
                    value = courseName,
                    onValueChange = {
                        courseName = it
                        if (it.isBlank()) {
                            filterKeyword = ""
                        }
                    },
                    placeholder = { Text(text = "课程名/教师名") }
                ) {
                    filterKeyword = courseName
                }

                TabRowWithPager(pagerState = pagerState, tabs = tabs) { page ->
                    LazyColumn(
                        contentPadding = PaddingValues(padding),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        timetableAll?.list?.forEach {
                            if (filterKeyword.isBlank() || it.name.contains(filterKeyword) || it.list.let { list ->
                                    list.forEach {
                                        if (it.teacher.contains(filterKeyword)) {
                                            return@let true
                                        }
                                    }
                                    return@let false
                                }
                            ) {
                                when (page) {
                                    0 -> {
                                        rowItem(
                                            timetableAllItem = it,
                                            week = currentWeek,
                                            dayOfWeek = dayOfWeek
                                        ) { name, list ->
                                            visibleCourseName = name
                                            visibleCourseInfoList = list
                                            courseInfoBottomSheetVisible = true
                                        }
                                    }

                                    1 -> {
                                        rowItem(
                                            timetableAllItem = it,
                                            week = tomorrowWeek,
                                            dayOfWeek = tomorrowDayOfWeek
                                        ) { name, list ->
                                            visibleCourseName = name
                                            visibleCourseInfoList = list
                                            courseInfoBottomSheetVisible = true
                                        }
                                    }

                                    2 -> {
                                        rowItem(timetableAllItem = it) { name, list ->
                                            visibleCourseName = name
                                            visibleCourseInfoList = list
                                            courseInfoBottomSheetVisible = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    CourseInfoBottomSheet(
        visible = courseInfoBottomSheetVisible,
        name = visibleCourseName,
        list = visibleCourseInfoList
    ) {
        courseInfoBottomSheetVisible = false
    }
}


@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.rowItem(
    timetableAllItem: TimetableAllItem,
    week: Int? = null,
    dayOfWeek: Int? = null,
    onItemClick: (name: String, list: List<CourseInfo>) -> Unit
) {
    val filterList = if (week == null && dayOfWeek == null) {
        timetableAllItem.list
    } else {
        arrayListOf<CourseInfo>().also { arrayList ->
            timetableAllItem.list.forEach {
                if (it.dayOfWeek == dayOfWeek && it.weeks.contains(week)) {
                    arrayList.add(it)
                }
            }
        }
    }

    if (filterList.isNotEmpty()) {
        item {
            OutlinedCard(
                onClick = {
                    onItemClick(timetableAllItem.name, filterList)
                },
                border = borderOfSurfaceVariant(),
                modifier = Modifier.padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(twiceOfPadding)
                ) {
                    Text(
                        text = timetableAllItem.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(halfOfPadding))
                    Text(text = buildAnnotatedString {
                        val filter = mutableSetOf<String>()
                        filterList.forEach {
                            if (!filter.contains(it.teacher)) {
                                append(it.teacher + " ")
                                filter.add(it.teacher)
                            }
                        }
                    }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseInfoBottomSheet(
    visible: Boolean,
    name: String,
    list: List<CourseInfo>,
    onDismiss: () -> Unit
) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss
        ) {
            val style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)

            Column(
                modifier = Modifier.padding(twiceOfPadding)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn {
                    items(list) { courseInfo ->
                        Column {
                            Spacer(modifier = Modifier.height(twiceOfPadding))
                            ElevatedCard(
                                onClick = {}
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(twiceOfPadding)
                                ) {
                                    Text(
                                        text = "${courseInfo.sectionFirst} - ${courseInfo.sectionFirst + 1} 节",
                                        style = style
                                    )
                                    Text(text = courseInfo.teacher, style = style)
                                    Text(text = AreaMapper.mapArea(courseInfo.area), style = style)
                                    Text(text = courseInfo.clazz, style = style)
                                    Spacer(modifier = Modifier.height(halfOfPadding))
                                    Text(
                                        text = courseInfo.rawWeeks,
                                        style = style.copy(color = Color.Gray, fontSize = 12.sp)
                                    )
                                    Text(
                                        text = days[courseInfo.dayOfWeek - 1],
                                        style = style.copy(color = Color.Gray, fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}