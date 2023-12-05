package com.awning.afterglow.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.component.MTopAppBar

@Composable
fun TeachingEvaluationScreen(navController: NavHostController) {
    MTopAppBar(title = ModuleRoute.ModuleTeachingEvaluation.title, navController = navController) {

    }
}