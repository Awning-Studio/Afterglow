package com.awning.afterglow.navroute

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CatchingPokemon
import androidx.compose.material.icons.rounded.CircleNotifications
import androidx.compose.material.icons.rounded.CoPresent
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FindInPage
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.MapsHomeWork
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Recommend
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.awning.afterglow.ui.screen.BorrowCourseScreen
import com.awning.afterglow.ui.screen.CourseSelectionScreen
import com.awning.afterglow.ui.screen.ExamPlanScreen
import com.awning.afterglow.ui.screen.LevelReportScreen
import com.awning.afterglow.ui.screen.NotEmptyRoomScreen
import com.awning.afterglow.ui.screen.NoticeScreen
import com.awning.afterglow.ui.screen.PresentationScreen
import com.awning.afterglow.ui.screen.QuickNetworkScreen
import com.awning.afterglow.ui.screen.RandomScreen
import com.awning.afterglow.ui.screen.RequestScreen
import com.awning.afterglow.ui.screen.SchoolReportScreen
import com.awning.afterglow.ui.screen.SecondClassScreen
import com.awning.afterglow.ui.screen.TeacherInfoScreen
import com.awning.afterglow.ui.screen.TeachingEvaluationScreen
import com.awning.afterglow.ui.screen.TimetableModuleScreen

/**
 * 模组导航路由
 * @property route 路由唯一标识
 * @property title 路由名
 * @property icon 图标
 * @property composable 路由页面的 [Composable] 函数，需要传入外部的 [NavHostController]
 * @constructor
 */
sealed class ModuleRoute(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val composable: @Composable (navController: NavHostController) -> Unit
) {
    companion object {
        /**
         * 基础模组
         */
        val module = listOf(
            ModuleTimetable,
            ModuleTeachingEvaluation,
            ModuleCourseSelection,
            ModuleExamPlan,
            ModuleSchoolReport,
            ModuleLevelReport,
            ModuleSecondClass,
            ModuleTeacherInfo,
            ModuleNotice,
            ModuleQuickNetwork,
            ModuleNotEmptyRoom,
            ModuleBorrowCourse,
            ModulePresentation
        )

        /**
         * 扩展模组
         */
        val moduleExtended = listOf(
            ModuleRandom,
            ModuleRequest
        )
    }

    object ModuleTimetable : ModuleRoute(
        "ModuleTimetable",
        "课表",
        Icons.Rounded.CalendarMonth,
        { TimetableModuleScreen(it) }
    )

    object ModuleTeachingEvaluation : ModuleRoute(
        "TeachingEvaluation",
        "评教",
        Icons.Rounded.Recommend,
        { TeachingEvaluationScreen(it) }
    )

    object ModuleCourseSelection : ModuleRoute(
        "ModuleCourseSelection",
        "选课",
        Icons.Rounded.AutoStories,
        { CourseSelectionScreen(it) }
    )

    object ModuleExamPlan : ModuleRoute(
        "ModuleExamPlan",
        "考试安排",
        Icons.Rounded.AccessTime,
        { ExamPlanScreen(it) }
    )

    object ModuleSchoolReport : ModuleRoute(
        "ModuleSchoolReport",
        "课程成绩",
        Icons.Rounded.ReceiptLong,
        { SchoolReportScreen(it) }
    )

    object ModuleLevelReport : ModuleRoute(
        "ModuleLevelReport",
        "等级考试",
        Icons.Rounded.Leaderboard,
        { LevelReportScreen(it) }
    )

    object ModuleSecondClass : ModuleRoute(
        "ModuleSecondClass",
        "第二课堂",
        Icons.Rounded.CatchingPokemon,
        { SecondClassScreen(it) }
    )

    object ModuleTeacherInfo : ModuleRoute(
        "TeacherInfo",
        "教师信息",
        Icons.Rounded.FindInPage,
        { TeacherInfoScreen(it) }
    )

    object ModuleNotice : ModuleRoute(
        "Notice",
        "教务通知",
        Icons.Rounded.CircleNotifications,
        { NoticeScreen(it) }
    )

    object ModuleQuickNetwork : ModuleRoute(
        "QuickNetwork",
        "校园网连接",
        Icons.Rounded.Wifi,
        { QuickNetworkScreen(it) }
    )

    object ModuleNotEmptyRoom :
        ModuleRoute("NotEmptyRoom", "非空教室", Icons.Rounded.MapsHomeWork, { NotEmptyRoomScreen(it) })

    object ModuleBorrowCourse :
        ModuleRoute("BorrowCourse", "蹭课", Icons.Rounded.MeetingRoom, { BorrowCourseScreen(it) })

    object ModulePresentation :
        ModuleRoute("Presentation", "宣讲会", Icons.Rounded.CoPresent, { PresentationScreen(it) })

    object ModuleRandom :
        ModuleRoute("Random", "随机数", Icons.Rounded.Numbers, { RandomScreen(it) })

    object ModuleRequest :
        ModuleRoute("Request", "网络请求", Icons.Rounded.Code, { RequestScreen(it) })
}