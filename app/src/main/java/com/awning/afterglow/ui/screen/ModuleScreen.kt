package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudCircle
import androidx.compose.material.icons.rounded.SyncLock
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.module.Snapshot
import com.awning.afterglow.navroute.BottomRoute
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.store.Lighting
import com.awning.afterglow.toolkit.Trigger
import com.awning.afterglow.ui.component.TabRowWithPager
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.theme.ErrorColor
import com.awning.afterglow.ui.theme.SuccessColor
import com.awning.afterglow.viewmodel.controller.ModuleController
import com.awning.afterglow.viewmodel.controller.NoticeController
import com.awning.afterglow.viewmodel.RuntimeVM
import com.awning.afterglow.viewmodel.controller.SettingController
import com.awning.afterglow.viewmodel.controller.TimetableAllController


@Composable
fun ModuleScreen(navController: NavHostController, innerNavController: NavHostController) {
    Scaffold(
        topBar = { ModuleTopAppBar(innerNavController) }
    ) {
        ModuleContent(navController = navController, modifier = Modifier.padding(it))
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModuleContent(navController: NavHostController, modifier: Modifier = Modifier) {
    val tabs = listOf("基础", "扩展")
    val route = listOf(ModuleRoute.module, ModuleRoute.moduleExtended)
    val pagerState = rememberPagerState { tabs.size }

    val context = LocalContext.current

    val username by SettingController.lastUsernameFlow().collectAsState(initial = null)
    val updateTime = listOf(
        ModuleController.timetableFlow(username).collectAsState(initial = null),
        ModuleController.examPlanFlow(username).collectAsState(initial = null),
        ModuleController.schoolReportFlow(username).collectAsState(initial = null),
        ModuleController.levelReportFlow(username).collectAsState(initial = null),
        ModuleController.secondClassFlow(username).collectAsState(initial = null)
    )

    val trigger = remember { Trigger() }

    TabRowWithPager(pagerState = pagerState, tabs = tabs, modifier = modifier.fillMaxSize()) {
        ModuleBox(modules = route[it], updateTime) { route ->
            trigger.touch {
                when (route) {
                    ModuleRoute.ModuleTeacherInfo.route -> {
                        if (RuntimeVM.eduSystem == null) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                        } else {
                            navController.navigate(route)
                        }
                    }

                    else -> {
                        navController.navigate(route)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleBox(
    modules: List<ModuleRoute>,
    updateTime: List<State<Snapshot?>>,
    onItemClick: (route: String) -> Unit
) {
    val timetableAllUpdateTime by TimetableAllController.updateTimeFlow().collectAsState(initial = null)

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(padding),
        modifier = Modifier.fillMaxSize()
    ) {
        modules.forEach {
            val note = getNote(it, updateTime, timetableAllUpdateTime)

            item {
                val enabled = it != ModuleRoute.ModuleCourseSelection

                ElevatedCard(
                    onClick = { onItemClick(it.route) },
                    modifier = Modifier.padding(halfOfPadding),
                    enabled = enabled
                ) {
                    val color =
                        if (enabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    val tipColor = if (enabled) Color.Gray else LocalContentColor.current

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding + halfOfPadding)
                    ) {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.title,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = padding, bottom = halfOfPadding),
                            color = color
                        )
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = tipColor
                        )
                    }
                }
            }
        }
    }
}


fun getNote(navRoute: ModuleRoute, updateTime: List<State<Snapshot?>>, timetableAllUpdateTime: String?) = when (navRoute) {
    ModuleRoute.ModuleTimetable -> {
        updateTime[0].value?.updateTime ?: "未同步"
    }

    ModuleRoute.ModuleTeachingEvaluation -> {
        "现已开放"
    }

    ModuleRoute.ModuleCourseSelection -> {
        "下次选课开放"
    }

    ModuleRoute.ModuleExamPlan -> {
        updateTime[1].value?.updateTime ?: "未同步"
    }

    ModuleRoute.ModuleSchoolReport -> {
        updateTime[2].value?.updateTime ?: "未同步"
    }

    ModuleRoute.ModuleLevelReport -> {
        updateTime[3].value?.updateTime ?: "未同步"
    }

    ModuleRoute.ModuleSecondClass -> {
        updateTime[4].value?.updateTime ?: "未同步"
    }

    ModuleRoute.ModuleTeacherInfo -> {
        RuntimeVM.eduSystem?.let { "可使用" } ?: "需登录"
    }

    ModuleRoute.ModuleNotice -> {
        "${NoticeController.newNoticeCount} 条新通知"
    }

    ModuleRoute.ModuleQuickNetwork -> {
        "连网真麻烦"
    }

    ModuleRoute.ModuleNotEmptyRoom -> {
        if(TimetableAllController.isUpdating) "同步中" else timetableAllUpdateTime ?: "未同步"
    }

    ModuleRoute.ModuleBorrowCourse -> {
        if(TimetableAllController.isUpdating) "同步中" else timetableAllUpdateTime ?: "未同步"
    }

    ModuleRoute.ModulePresentation -> {
        "来自指导中心"
    }

    ModuleRoute.ModuleRandom -> {
        "抽一位幸运者"
    }

    ModuleRoute.ModuleRequest -> {
        "便捷Http"
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleTopAppBar(innerNavController: NavHostController) {
    val trigger = remember { Trigger() }

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    Lighting.Amethyst = !Lighting.Amethyst
                }
            ) {
                Icon(
                    imageVector = BottomRoute.Module.icon,
                    contentDescription = "NavIcon",
                    tint = if (Lighting.Amethyst) Lighting.MColor.Amethyst else Color.LightGray
                )
            }
        },
        actions = {
            IconButton(onClick = {
                trigger.touch {
                    if (RuntimeVM.eduSystem == null) innerNavController.navigate(
                        BottomRoute.Me.route
                    )
                }
            }) {
                Icon(
                    imageVector = if (RuntimeVM.isLoggingIn) Icons.Rounded.SyncLock else Icons.Rounded.CloudCircle,
                    contentDescription = "CloudCircle",
                    tint = RuntimeVM.eduSystem?.let { SuccessColor } ?: ErrorColor
                )
            }
        },
        title = {}
    )
}