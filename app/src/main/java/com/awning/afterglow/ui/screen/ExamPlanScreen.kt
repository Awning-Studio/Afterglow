package com.awning.afterglow.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AreaChart
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PermIdentity
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun ExamPlanScreen(navController: NavHostController) {
    val username by SettingController.lastUsernameFlow().collectAsState(initial = null)
    val examPlan by ModuleController.examPlanFlow(username).collectAsState(initial = null)

    MTopAppBar(title = ModuleRoute.ModuleExamPlan.title, navController = navController) {
        if (examPlan == null) {
            EmptyScreen()
        }else if (examPlan?.list?.isEmpty() == true) {
            EmptyScreen {
                Text(text = "没有安排")
            }
        }else {
            LazyColumn(
                contentPadding = PaddingValues(halfOfPadding)
            ) {
                examPlan?.list?.forEach {
                    item{
                        ElevatedCard(
                            onClick = {},
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
                                    icon = Icons.Rounded.AccessTime,
                                    contentDescription = "AccessTime",
                                    text = "时间: ${it.time}"
                                )
                                Spacer(modifier = Modifier.height(halfOfPadding))
                                IconText(
                                    icon = Icons.Rounded.LocationOn,
                                    contentDescription = "LocationOn",
                                    text = "地点: ${it.area}"
                                )
                                Spacer(modifier = Modifier.height(halfOfPadding))
                                IconText(
                                    icon = Icons.Rounded.Numbers,
                                    contentDescription = "Numbers",
                                    text = "代码: ${it.courseId}"
                                )
                                Spacer(modifier = Modifier.height(halfOfPadding))
                                IconText(
                                    icon = Icons.Rounded.AreaChart,
                                    contentDescription = "AreaChart",
                                    text = "校区: ${it.campus}"
                                )
                                Spacer(modifier = Modifier.height(halfOfPadding))
                                IconText(
                                    icon = Icons.Rounded.PermIdentity,
                                    contentDescription = "PermIdentity",
                                    text = "考号: ${it.id ?: "无"}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}