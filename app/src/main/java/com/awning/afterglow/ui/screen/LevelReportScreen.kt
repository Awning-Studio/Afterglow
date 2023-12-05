package com.awning.afterglow.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DesktopMac
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.SelfImprovement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun LevelReportScreen(navController: NavHostController) {
    val username by SettingController.lastUsernameFlow().collectAsState(initial = null)
    val levelReport by ModuleController.levelReportFlow(username).collectAsState(initial = null)

    MTopAppBar(
        title = ModuleRoute.ModuleLevelReport.title,
        navController = navController
    ) {
        if (levelReport == null) {
            EmptyScreen()
        }else {
            LazyColumn(
                contentPadding = PaddingValues(halfOfPadding)
            ) {
                levelReport?.list?.forEach {
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
                                    .padding(twiceOfPadding)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = it.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight(600)
                                        )
                                        Text(
                                            text = it.time,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(text = it.score)
                                }

                                AnimatedVisibility(visible = visible) {
                                    Column {
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.PendingActions,
                                            contentDescription = "PendingActions",
                                            text = "考试时间: ${it.time}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.SelfImprovement,
                                            contentDescription = "SelfImprovement",
                                            text = "总分数: ${it.score}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Leaderboard,
                                            contentDescription = "Leaderboard",
                                            text = "等级: ${it.level}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.DesktopMac,
                                            contentDescription = "DesktopMac",
                                            text = "机试分数: ${it.machineScore}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.DesktopMac,
                                            contentDescription = "DesktopMac",
                                            text = "机试等级: ${it.machineScore}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Edit,
                                            contentDescription = "Edit",
                                            text = "笔试分数: ${it.writtenScore}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Edit,
                                            contentDescription = "Edit",
                                            text = "笔试等级: ${it.writtenScore}"
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