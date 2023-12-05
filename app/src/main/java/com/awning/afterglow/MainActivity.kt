package com.awning.afterglow

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.request.waterfall.Waterfall
import com.awning.afterglow.ui.screen.MainScreen
import com.awning.afterglow.ui.theme.AfterglowTheme
import com.awning.afterglow.viewmodel.RuntimeVM
import com.awning.afterglow.viewmodel.controller.LoginController
import com.awning.afterglow.viewmodel.controller.SettingController
import com.awning.afterglow.viewmodel.controller.UserController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfterglowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    // 自动登录
                    LaunchedEffect(key1 = Unit) {
                        SettingController.autoLoginFlow().collect { autoLogin ->
                            if (autoLogin && RuntimeVM.eduSystem == null) {
                                SettingController.lastUsernameFlow().collect { username ->
                                    username?.let {
                                        UserController.userFlow().collect { list ->
                                            for (i in list.indices) {
                                                if (list[i].username == username) {
                                                    LoginController.login(list[i]).catch { }
                                                        .collect()
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val navController = rememberNavController()
                    val module = ModuleRoute.module
                    val moduleExtended = ModuleRoute.moduleExtended

                    NavHost(
                        navController = navController,
                        startDestination = "Main",
                        enterTransition = { slideInHorizontally { it } },
                        exitTransition = { slideOutHorizontally { -it } },
                        popEnterTransition = { fadeIn() },
                        popExitTransition = { slideOutHorizontally { it } }
                    ) {
                        composable("Main") {
                            MainScreen(navController)
                        }
                        module.forEach { navRoute ->
                            composable(navRoute.route) {
                                navRoute.composable(navController)
                            }
                        }
                        moduleExtended.forEach { navRoute ->
                            composable(navRoute.route) {
                                navRoute.composable(navController)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Waterfall.close()
    }
}