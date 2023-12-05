package com.awning.afterglow.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.awning.afterglow.navroute.BottomRoute
import com.awning.afterglow.toolkit.Transformer
import com.awning.afterglow.viewmodel.controller.SettingController
import java.time.LocalDate
import java.util.Date

@Composable
fun MainScreen(navController: NavHostController) {
    val innerNavController = rememberNavController()
    val backStackEntry = innerNavController.currentBackStackEntryAsState()
    val currentRoute by remember { derivedStateOf { backStackEntry.value?.destination?.route } }
    val bottomRoutes = BottomRoute.list

    Scaffold(
        bottomBar = {
            AfterglowBottomBar(
                currentRoute = currentRoute,
                navController = innerNavController,
                bottomRoutes = bottomRoutes
            )
        }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomRoute.Timetable.route,
            modifier = Modifier.padding(padding)
        ) {
            bottomRoutes.forEach { bottomRoute ->
                composable(bottomRoute.route) {
                    bottomRoute.composable(navController, innerNavController)
                }
            }
        }
    }


    DatePickerBottomSheet()
}


@Composable
private fun AfterglowBottomBar(
    currentRoute: String?,
    navController: NavHostController,
    bottomRoutes: List<BottomRoute>
) {
    NavigationBar {
        bottomRoutes.forEach {
            NavigationBarItem(
                label = { Text(text = it.title) },
                selected = currentRoute == it.route,
                onClick = {
                    if (currentRoute != it.route) {
                        navController.popBackStack()
                        navController.navigate(it.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = it.title
                    )
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerBottomSheet() {
    if (datePickerBottomSheetVisible) {
        val sheetState = rememberModalBottomSheetState()
        val datePickerState = rememberDatePickerState()

        // 初始化日期
        LaunchedEffect(key1 = Unit) {
            SettingController.schoolStartFlow().collect {
                datePickerState.setSelection(Transformer.timeStampOf(LocalDate.parse(it)))
            }
        }

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                datePickerBottomSheetVisible = false
                datePickerState.selectedDateMillis?.let { timeStamp ->
                    val localDate = Transformer.localDateOf(Date(timeStamp)).let {
                        if (it.dayOfWeek.value == 1) {
                            it
                        }else {
                            it.plusDays((1 - it.dayOfWeek.value).toLong())
                        }
                    }
                    SettingController.setSchoolStart(localDate.toString())
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}