package com.awning.afterglow.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddChart
import androidx.compose.material.icons.rounded.AllOut
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.MergeType
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PlaylistAddCheckCircle
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Score
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.component.IconText
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.ModuleController
import com.awning.afterglow.viewmodel.controller.SettingController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolReportScreen(navController: NavHostController) {
    val username by SettingController.lastUsernameFlow().collectAsState(initial = null)
    val schoolReport by ModuleController.schoolReportFlow(username).collectAsState(initial = null)

    MTopAppBar(
        title = ModuleRoute.ModuleSchoolReport.title,
        navController = navController
    ) {
        if (schoolReport == null) {
            EmptyScreen()
        }else {
            LazyColumn(contentPadding = PaddingValues(halfOfPadding)) {
                schoolReport?.list?.forEach {
                    item {
                        var visible by remember { mutableStateOf(false) }

                        ElevatedCard(
                            onClick = {
                                visible = !visible
                            },
                            modifier = Modifier.padding(halfOfPadding)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(twiceOfPadding)
                            ) {
                                Text(
                                    text = it.name,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight(600)
                                )
                                Spacer(modifier = Modifier.height(halfOfPadding))
                                IconText(
                                    icon = Icons.Rounded.SelfImprovement,
                                    contentDescription = "SelfImprovement",
                                    text = "成绩: ${it.calculatedScore}"
                                )
                                Spacer(modifier = Modifier.height(halfOfPadding))
                                IconText(
                                    icon = Icons.Rounded.Timeline,
                                    contentDescription = "Timeline",
                                    text = "学期: ${it.semester}"
                                )

                                AnimatedVisibility(visible = visible) {
                                    Column {
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Numbers,
                                            contentDescription = "Numbers",
                                            text = "代码: ${it.courseId}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Score,
                                            contentDescription = "Score",
                                            text = "学分: ${it.point}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.AllOut,
                                            contentDescription = "AllOut",
                                            text = "考试性质: ${it.examType}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.AddChart,
                                            contentDescription = "AddChart",
                                            text = "考核方式: ${it.examMode}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.MergeType,
                                            contentDescription = "MergeType",
                                            text = "课程属性: ${it.type}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Sort,
                                            contentDescription = "Sort",
                                            text = "课程性质: ${it.sort}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.PlaylistAddCheckCircle,
                                            contentDescription = "PlaylistAddCheckCircle",
                                            text = "平时成绩: ${it.usualScore}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Explore,
                                            contentDescription = "Explore",
                                            text = "实验成绩: ${it.experimentScore}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.ReceiptLong,
                                            contentDescription = "ReceiptLong",
                                            text = "考试成绩: ${it.examScore}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.HourglassBottom,
                                            contentDescription = "HourglassBottom",
                                            text = "总学时: ${it.classHours}"
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
}