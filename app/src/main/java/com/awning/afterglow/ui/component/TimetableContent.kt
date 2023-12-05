package com.awning.afterglow.ui.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awning.afterglow.module.AreaMapper
import com.awning.afterglow.module.edusystem.api.Timetable
import com.awning.afterglow.module.edusystem.api.TimetableItem
import com.awning.afterglow.type.schoolSchedules
import com.awning.afterglow.ui.NoBorder
import com.awning.afterglow.ui.borderOfPrimary
import com.awning.afterglow.ui.borderOfSurfaceVariant
import com.awning.afterglow.ui.padding
import com.awning.afterglow.viewmodel.controller.SettingController

private val innerPadding = 2.dp
private val shape = RoundedCornerShape(3.dp)

val scheduleWidth = 30.dp
val itemMargin = 0.5.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableContent(
    timetable: Timetable?,
    week: Int? = null,
    onItemClick: (row: Int, column: Int, course: List<TimetableItem>) -> Unit
) {
    val context = LocalContext.current

    val schedule by SettingController.scheduleFlow().collectAsState(initial = 0)

    val height = 95.dp

    LazyColumn {
        schoolSchedules[schedule].list.forEachIndexed { index, time ->
            val section = index * 2 + 1
            val rowFirstItemIndex = index * 7
            val rowEndItemIndex = rowFirstItemIndex + 6

            item {
                Row(
                    modifier = Modifier.height(height)
                ) {
                    // 时间
                    OutlinedCard(
                        onClick = {
                            val option = if (schedule == 0) 1 else 0
                            SettingController.setSchedule(option)
                            Toast.makeText(
                                context,
                                "时间表切换至${schoolSchedules[option].area}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = shape,
                        modifier = Modifier
                            .width(scheduleWidth)
                            .padding(itemMargin),
                        border = NoBorder
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(scheduleWidth)
                                .fillMaxHeight()
                                .padding(innerPadding)
                        ) {
                            Text(
                                text = "$section-${section + 1}",
                                style = TextStyle(fontSize = 8.sp)
                            )
                            Spacer(modifier = Modifier.height(padding / 4))
                            Text(text = time[0], style = TextStyle(fontSize = 8.sp))
                            Text(text = time[1], style = TextStyle(fontSize = 8.sp))
                            Spacer(modifier = Modifier.height(padding))
                            Text(text = time[2], style = TextStyle(fontSize = 8.sp))
                            Text(text = time[3], style = TextStyle(fontSize = 8.sp))
                        }
                    }

                    // 课表内容
                    timetable?.let { timetable ->
                        for (i in rowFirstItemIndex..rowEndItemIndex) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(height)
                                    .padding(itemMargin)
                            ) {
                                timetable.list[i]?.let { list ->
                                    if (week == null) {
                                        TableItem(
                                            item = list[0],
                                            count = list.size,
                                            showArea = false
                                        ) {
                                            onItemClick(
                                                i - rowFirstItemIndex,
                                                index,
                                                list
                                            )
                                        }
                                    } else {
                                        for (courseIndex in list.indices) {
                                            if (list[courseIndex].weeks.binarySearch(week) > -1) {
                                                TableItem(
                                                    item = list[courseIndex],
                                                    count = list.size - 1
                                                ) {
                                                    onItemClick(
                                                        i - rowFirstItemIndex,
                                                        index,
                                                        list
                                                    )
                                                }
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (index % 2 != 0) {
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }

        item {
            // 课表备注
            timetable?.let {
                OutlinedCard(
                    onClick = {},
                    shape = shape,
                    border = borderOfSurfaceVariant(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(itemMargin)
                ) {
                    Text(
                        text = it.notes,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableItem(
    item: TimetableItem,
    count: Int,
    showArea: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                0.25f
            )
        ),
        border = borderOfPrimary(alpha = 0.2f)
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = item.name,
                    style = TextStyle(fontSize = 10.sp),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(
                    modifier = Modifier.height(padding)
                )
                Text(
                    text = if (showArea) AreaMapper.mapArea(item.area) else item.teacher,
                    style = TextStyle(fontSize = 10.sp),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 课程数量角标
            if (count > 0) {
                Text(
                    text = "+$count",
                    fontSize = 8.sp,
                    modifier = Modifier
                        .align(
                            Alignment.BottomEnd
                        )
                        .padding(innerPadding),
                    color = Color.Gray
                )
            }
        }
    }
}