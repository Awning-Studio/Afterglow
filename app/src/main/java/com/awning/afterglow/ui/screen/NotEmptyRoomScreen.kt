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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.module.AreaMapper
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.type.NotEmptyRoom
import com.awning.afterglow.ui.borderOfSurfaceVariant
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.ScrollableTabRowWithPager
import com.awning.afterglow.ui.component.TabRowWithPager
import com.awning.afterglow.ui.component.itemsGrouped
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.SettingController
import com.awning.afterglow.viewmodel.controller.TimetableAllController
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotEmptyRoomScreen(navController: NavHostController) {
    val dayOfWeek = LocalDate.now().dayOfWeek.value
    val tomorrowDayOfWeek = if (dayOfWeek == 7) 1 else dayOfWeek + 1
    val currentWeek by SettingController.currentWeekFlow().collectAsState(initial = 0)
    val tomorrowWeek by remember { derivedStateOf { if (dayOfWeek == 7) currentWeek + 1 else currentWeek } }

    val timetableAll by TimetableAllController.timetableAllFlow().collectAsState(initial = null)

    val todayNotEmptyRooms = remember { mutableStateListOf<NotEmptyRoom>() }
    val tomorrowNotEmptyRooms = remember { mutableStateListOf<NotEmptyRoom>() }

    var visibleNotEmptyRoom by remember { mutableStateOf<NotEmptyRoom?>(null) }
    var bottomSheetVisible by remember { mutableStateOf(false) }

    val schedule by SettingController.scheduleFlow().collectAsState(initial = 0)
    val areas by remember {
        derivedStateOf {
            if (schedule == 0) AreaMapper.guangzhou + AreaMapper.foshan
            else AreaMapper.foshan + AreaMapper.guangzhou
        }
    }

    // 统计非空教室
    LaunchedEffect(key1 = timetableAll) {
        timetableAll?.let { timetableAll ->
            todayNotEmptyRooms.clear()
            tomorrowNotEmptyRooms.clear()

            timetableAll.list.forEach { timetableAllItem ->
                for (timetableAllCourseInfo in timetableAllItem.list) {
                    // 今日非空教室
                    if (timetableAllCourseInfo.dayOfWeek == dayOfWeek &&
                        timetableAllCourseInfo.weeks.contains(currentWeek)
                    ) {
                        val formatArea = AreaMapper.formatArea(timetableAllCourseInfo.area)
                        val mappedAreaName = AreaMapper.getMappedAreaName(formatArea.first)

                        formatArea.second?.let {
                            var index = indexOfArea(todayNotEmptyRooms, mappedAreaName)
                            // 有房间号
                            if (index == -1) {
                                todayNotEmptyRooms.add(
                                    NotEmptyRoom(
                                        mappedAreaName,
                                        listOf(
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf()
                                        )
                                    )
                                )
                                index = todayNotEmptyRooms.size - 1
                            }

                            todayNotEmptyRooms[index].list[timetableAllCourseInfo.sectionFirst / 2]
                                .add(it)
                        }
                    } else if (timetableAllCourseInfo.dayOfWeek == tomorrowDayOfWeek &&
                        timetableAllCourseInfo.weeks.contains(tomorrowWeek)
                    ) {
                        val formatArea = AreaMapper.formatArea(timetableAllCourseInfo.area)
                        val mappedAreaName = AreaMapper.getMappedAreaName(formatArea.first)

                        formatArea.second?.let {
                            var index = indexOfArea(tomorrowNotEmptyRooms, mappedAreaName)
                            // 有房间号
                            if (index == -1) {
                                tomorrowNotEmptyRooms.add(
                                    NotEmptyRoom(
                                        mappedAreaName,
                                        listOf(
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf(),
                                            arrayListOf()
                                        )
                                    )
                                )
                                index = tomorrowNotEmptyRooms.size - 1
                            }

                            tomorrowNotEmptyRooms[index].list[timetableAllCourseInfo.sectionFirst / 2]
                                .add(it)
                        }
                    }
                }
            }
            todayNotEmptyRooms.forEach { notEmptyRoom ->
                notEmptyRoom.list.forEach {
                    it.sort()
                }
            }
            tomorrowNotEmptyRooms.forEach { notEmptyRoom ->
                notEmptyRoom.list.forEach {
                    it.sort()
                }
            }
        }
    }

    val tabs = listOf("今天", "明天")
    val pagerState = rememberPagerState { tabs.size }

    MTopAppBar(
        title = ModuleRoute.ModuleNotEmptyRoom.title,
        navController = navController
    ) {
        if (timetableAll == null) {
            EmptyScreen()
        } else {
            TabRowWithPager(pagerState = pagerState, tabs = tabs) {
                val notEmptyRooms = if (it == 0) todayNotEmptyRooms else tomorrowNotEmptyRooms

                LazyColumn(
                    contentPadding = PaddingValues(padding),
                    modifier = Modifier.fillMaxSize()
                ) {
                    areas.forEach { area ->
                        val index = indexOfArea(notEmptyRooms, area)
                        if (index != -1) {
                            item {
                                OutlinedCard(
                                    onClick = {
                                        visibleNotEmptyRoom = notEmptyRooms[index]
                                        bottomSheetVisible = true
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
                                            text = notEmptyRooms[index].name,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
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

    NotEmptyRoomSheet(
        visible = bottomSheetVisible,
        onDismiss = { bottomSheetVisible = false },
        notEmptyRoom = visibleNotEmptyRoom
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun NotEmptyRoomSheet(visible: Boolean, onDismiss: () -> Unit, notEmptyRoom: NotEmptyRoom?) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState()
        val tabs = listOf("1 - 2 节", "3 - 4 节", "5 - 6 节", "7 - 8 节", "9 - 10 节", "11 - 12 节")
        val pagerState = rememberPagerState { tabs.size }

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss
        ) {
            notEmptyRoom?.let { notEmptyRoom ->
                Column(
                    modifier = Modifier
                        .padding(twiceOfPadding)
                        .height(500.dp)
                ) {
                    Text(
                        text = "${notEmptyRoom.name} 非空教室",
                        modifier = Modifier.padding(bottom = twiceOfPadding),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    ScrollableTabRowWithPager(pagerState = pagerState, tabs = tabs) { page ->
                        Spacer(modifier = Modifier.height(twiceOfPadding))
                        if (notEmptyRoom.list[page].isEmpty()) {
                            Text(
                                text = "空的",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            LazyColumn {
                                itemsGrouped(5, notEmptyRoom.list[page]) {
                                    InputChip(
                                        selected = false,
                                        onClick = { },
                                        label = { Text(text = it.toString()) }
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


fun indexOfArea(list: List<NotEmptyRoom>, area: String): Int {
    list.forEachIndexed { index, notEmptyRoom ->
        if (notEmptyRoom.name == area) {
            return index
        }
    }
    return -1
}