package com.awning.afterglow.navroute

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.Token
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.awning.afterglow.ui.screen.MeScreen
import com.awning.afterglow.ui.screen.ModuleScreen
import com.awning.afterglow.ui.screen.PlanScreen
import com.awning.afterglow.ui.screen.TimetableScreen

/**
 * 底部导航路由
 * @property route 路由唯一标识
 * @property title 路由名
 * @property icon 图标
 * @property composable 路由页面的 [Composable] 函数
 */
sealed class BottomRoute(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val composable: @Composable (navController: NavHostController, innerNavController: NavHostController) -> Unit
) {
    companion object {
        /**
         * 导航项
         */
        val list = listOf(Timetable, Plan, Module, Me)
    }

    object Timetable :
        BottomRoute("Timetable", "日程", Icons.Rounded.AvTimer, { navController, innerNavController ->
            TimetableScreen()
        })

    object Plan : BottomRoute("Plan", "计划", Icons.Rounded.RocketLaunch, { navController, innerNavController ->
        PlanScreen()
    })
    object Module : BottomRoute("Module", "模组", Icons.Rounded.Token, { navController, innerNavController ->
        ModuleScreen(navController, innerNavController)
    })
    object Me : BottomRoute("Me", "我", Icons.Rounded.SentimentSatisfied, { navController, innerNavController ->
        MeScreen()
    })
}